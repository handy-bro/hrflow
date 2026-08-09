package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.config.LeavePolicyProperties;
import com.hrflow.hrflow_backend.dto.leaves.*;
import com.hrflow.hrflow_backend.entity.*;
import com.hrflow.hrflow_backend.enums.LeaveStatus;
import com.hrflow.hrflow_backend.enums.LeaveType;
import com.hrflow.hrflow_backend.enums.Role;
import com.hrflow.hrflow_backend.exceptionHandler.*;
import com.hrflow.hrflow_backend.exceptionHandler.employees.EmployeeNotFoundException;
import com.hrflow.hrflow_backend.exceptionHandler.leaves.*;
import com.hrflow.hrflow_backend.repository.*;
import com.hrflow.hrflow_backend.utils.WorkingDaysCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final LeavePolicyProperties policy;

    // ==================================================================
    // submit a leave request
    // ==================================================================

    @Transactional
    public LeaveRequestResponse submitRequest(Long employeeId, CreateLeaveRequest request) {

        Employee employee = getEmployeeOrThrow(employeeId);

        validateDates(request.startDate(), request.endDate(), request.leaveType());

        int requestedDays = WorkingDaysCalculator.countWorkingDays(request.startDate(), request.endDate());
        if (requestedDays == 0) {
            throw new InvalidLeaveDatesException("Selected range contains no working days");
        }

        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlapping(
                employeeId, request.startDate(), request.endDate());
        if (!overlapping.isEmpty()) {
            throw new OverlappingLeaveException("This period overlaps with an existing leave request");
        }

        if (request.leaveType() != LeaveType.UNPAID) {
            int year = request.startDate().getYear();
            LeaveBalance balance = getOrCreateBalance(employee, request.leaveType(), year);
            int pendingOrApprovedDays = sumPendingAndApprovedDays(employeeId, request.leaveType(), year);

            if (pendingOrApprovedDays + requestedDays > balance.getAllocatedDays()) {
                throw new InsufficientLeaveBalanceException(
                        "Insufficient leave balance: %d day(s) remaining, %d requested"
                                .formatted(balance.getAllocatedDays() - pendingOrApprovedDays, requestedDays));
            }
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.leaveType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .requestedDays(requestedDays)
                .reason(request.reason())
                .status(LeaveStatus.PENDING)
                .build();

        leaveRequestRepository.save(leaveRequest);

        if (employee.getManager() != null) {
            emailService.sendLeaveRequestSubmittedEmail(
                    employee.getManager().getEmail(), employee, leaveRequest);
        }

        return toResponse(leaveRequest);
    }

    private void validateDates(LocalDate start, LocalDate end, LeaveType type) {
        if (end.isBefore(start)) {
            throw new InvalidLeaveDatesException("End date must be after start date");
        }

        boolean isSick = type == LeaveType.SICK;
        LocalDate earliestAllowed = isSick ? LocalDate.now().minusDays(7) : LocalDate.now();

        if (start.isBefore(earliestAllowed)) {
            throw new InvalidLeaveDatesException(
                    isSick
                            ? "Sick leave cannot be backdated more than 7 days"
                            : "Start date cannot be in the past");
        }
    }

    // ==================================================================
    // Validation / rejection by the manager
    // ==================================================================

    @Transactional
    public LeaveRequestResponse review(Long leaveRequestId, ReviewLeaveRequest request, User reviewerUser) {

        if (request.decision() != LeaveStatus.APPROVED && request.decision() != LeaveStatus.REJECTED) {
            throw new InvalidLeaveStatusException("Decision must be APPROVED or REJECTED");
        }
        if (request.decision() == LeaveStatus.REJECTED
                && (request.comment() == null || request.comment().isBlank())) {
            throw new InvalidLeaveStatusException("A comment is required when rejecting a request");
        }

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStatusException("Only pending requests can be reviewed");
        }

        boolean isPrivileged = reviewerUser.getRole() == Role.ADMIN;

        Employee reviewerEmployee = employeeRepository.findByUserId(reviewerUser.getId()).orElse(null);

        boolean isDirectManager = reviewerEmployee != null
                && leaveRequest.getEmployee().getManager() != null
                && leaveRequest.getEmployee().getManager().getId().equals(reviewerEmployee.getId());

        if (!isPrivileged && !isDirectManager) {
            throw new UnauthorizedLeaveActionException("Not authorized to review this request");
        }

        leaveRequest.setStatus(request.decision());
        leaveRequest.setManagerComment(request.comment());
        leaveRequest.setReviewedBy(reviewerEmployee); // peut être null si l'admin n'a pas de fiche employé
        leaveRequest.setReviewedAt(LocalDateTime.now());

        if (request.decision() == LeaveStatus.APPROVED
                && leaveRequest.getLeaveType() != LeaveType.UNPAID) {
            LeaveBalance balance = getOrCreateBalance(
                    leaveRequest.getEmployee(), leaveRequest.getLeaveType(), leaveRequest.getStartDate().getYear());
            balance.setUsedDays(balance.getUsedDays() + leaveRequest.getRequestedDays());
            leaveBalanceRepository.save(balance);
        }

        leaveRequestRepository.save(leaveRequest);

        emailService.sendLeaveRequestReviewedEmail(
                leaveRequest.getEmployee().getEmail(), leaveRequest);

        return toResponse(leaveRequest);
    }
    // ==================================================================
    // Cancellation
    // ==================================================================

    @Transactional
    public LeaveRequestResponse cancel(Long leaveRequestId, Long requesterUserId) {

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));

        Employee requester = employeeRepository.findByUserId(requesterUserId)
                .orElseThrow(() -> new UnauthorizedLeaveActionException("Requester has no employee profile"));

        boolean isOwner = leaveRequest.getEmployee().getId().equals(requester.getId());
        boolean isPrivileged = requester.getUser().getRole() == Role.ADMIN;

        if (!isOwner && !isPrivileged) {
            throw new UnauthorizedLeaveActionException("Not authorized to cancel this request");
        }

        if (leaveRequest.getStatus() != LeaveStatus.PENDING && leaveRequest.getStatus() != LeaveStatus.APPROVED) {
            throw new InvalidLeaveStatusException("Only pending or approved requests can be cancelled");
        }
        if (leaveRequest.getStatus() == LeaveStatus.APPROVED && leaveRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new InvalidLeaveStatusException("Cannot cancel a leave that has already started");
        }

        if (leaveRequest.getStatus() == LeaveStatus.APPROVED
                && leaveRequest.getLeaveType() != LeaveType.UNPAID) {
            LeaveBalance balance = getOrCreateBalance(
                    leaveRequest.getEmployee(), leaveRequest.getLeaveType(), leaveRequest.getStartDate().getYear());
            balance.setUsedDays(Math.max(0, balance.getUsedDays() - leaveRequest.getRequestedDays()));
            leaveBalanceRepository.save(balance);
        }

        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        leaveRequestRepository.save(leaveRequest);

        return toResponse(leaveRequest);
    }

    // ==================================================================
    // Balance
    // ==================================================================

    @Transactional
    public List<LeaveBalanceResponse> getBalances(Long employeeId, int year) {
        Employee employee = getEmployeeOrThrow(employeeId);

        return List.of(LeaveType.ANNUAL, LeaveType.SICK, LeaveType.MATERNITY_PATERNITY).stream()
                .map(type -> {
                    LeaveBalance balance = getOrCreateBalance(employee, type, year);
                    return new LeaveBalanceResponse(
                            type, year, balance.getAllocatedDays(), balance.getUsedDays(), balance.getRemainingDays());
                })
                .toList();
    }

    private LeaveBalance getOrCreateBalance(Employee employee, LeaveType type, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(employee.getId(), type, year)
                .orElseGet(() -> leaveBalanceRepository.save(LeaveBalance.builder()
                        .employee(employee)
                        .leaveType(type)
                        .year(year)
                        .allocatedDays(allocatedDaysFor(type))
                        .usedDays(0)
                        .build()));
    }

    private int allocatedDaysFor(LeaveType type) {
        return switch (type) {
            case ANNUAL -> policy.annualDays();
            case SICK -> policy.sickDays();
            case MATERNITY_PATERNITY -> policy.maternityPaternityDays();
            case UNPAID -> Integer.MAX_VALUE;
        };
    }

    private int sumPendingAndApprovedDays(Long employeeId, LeaveType type, int year) {
        return leaveRequestRepository.findOverlapping(
                        employeeId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
                .stream()
                .filter(lr -> lr.getLeaveType() == type)
                .mapToInt(LeaveRequest::getRequestedDays)
                .sum();
    }

    // ==================================================================
    // History
    // ==================================================================

    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> getHistory(Long employeeId, Pageable pageable) {
        return leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId, pageable)
                .map(this::toResponse);
    }

    // ==================================================================
    // Team calendar
    // ==================================================================

    @Transactional(readOnly = true)
    public List<CalendarEntryResponse> getTeamCalendar(Long departmentId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        return leaveRequestRepository.findApprovedForDepartmentInRange(departmentId, monthStart, monthEnd)
                .stream()
                .map(lr -> new CalendarEntryResponse(
                        lr.getEmployee().getId(),
                        lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName(),
                        lr.getEmployee().getDepartment() != null ? lr.getEmployee().getDepartment().getName() : null,
                        lr.getLeaveType(),
                        lr.getStartDate(),
                        lr.getEndDate()))
                .toList();
    }

    // ==================================================================
    // Utils
    // ==================================================================

    private Employee getEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
    }

    private LeaveRequestResponse toResponse(LeaveRequest lr) {
        return new LeaveRequestResponse(
                lr.getId(),
                lr.getEmployee().getId(),
                lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName(),
                lr.getLeaveType(),
                lr.getStartDate(),
                lr.getEndDate(),
                lr.getRequestedDays(),
                lr.getReason(),
                lr.getStatus(),
                lr.getManagerComment(),
                lr.getReviewedBy() != null
                        ? lr.getReviewedBy().getFirstName() + " " + lr.getReviewedBy().getLastName()
                        : null,
                lr.getReviewedAt(),
                lr.getCreatedAt()
        );
    }
}