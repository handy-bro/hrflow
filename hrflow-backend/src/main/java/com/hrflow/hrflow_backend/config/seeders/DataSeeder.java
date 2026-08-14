package com.hrflow.hrflow_backend.config.seeders;

import com.hrflow.hrflow_backend.entity.*;
import com.hrflow.hrflow_backend.enums.*;
import com.hrflow.hrflow_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

/**
 * Development-only seeder. Generates a sizeable, internally-consistent dataset
 * (departments, users, employees, leave requests/balances, attendance, payslips)
 * covering roughly 18 months of history, using the real repositories and the
 * real PasswordEncoder — so every generated account behaves exactly like one
 * created through the actual application flows.
 *
 * All seeded accounts share the password: Password123!
 * Admin login: admin@hrflow.local / Password123!
 *
 * Never runs outside the "dev" profile.
 */
@Component
//@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final PayslipRepository payslipRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SEED_PASSWORD = "Password123!";
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate WINDOW_START = TODAY.minusMonths(18);
    private static final Random RANDOM = new Random(42);

    private static final Map<LeaveType, Integer> LEAVE_POLICY = Map.of(
            LeaveType.ANNUAL, 25,
            LeaveType.SICK, 10,
            LeaveType.MATERNITY_PATERNITY, 90
    );

    private static final String[] MALE_FIRST_NAMES = {
            "Chidi", "Emeka", "Oluwaseun", "Kwame", "Kofi", "Kwabena", "Sipho", "Thabo",
            "Mandla", "Njoroge", "Kamau", "Otieno", "Amadou", "Moussa", "Ibrahima",
            "Franck", "Serge", "Patrice", "Herve", "Junior", "Yannick", "Blaise",
            "Armand", "Cyrille", "Landry", "Aristide", "Boubacar", "Cheikh", "Mamadou"
    };
    private static final String[] FEMALE_FIRST_NAMES = {
            "Amara", "Ngozi", "Chiamaka", "Adaeze", "Ama", "Akosua", "Efua", "Zanele",
            "Nomvula", "Wanjiru", "Achieng", "Fatou", "Aissatou", "Mariama", "Awa",
            "Larissa", "Carine", "Sandrine", "Nadege", "Estelle", "Gwladys",
            "Pulcherie", "Aurelie", "Christelle", "Divine", "Marlyse", "Odette", "Solange"
    };
    private static final String[] LAST_NAMES = {
            "Okafor", "Okonkwo", "Adeyemi", "Balogun", "Mensah", "Owusu", "Boateng",
            "Asante", "Nkosi", "Dlamini", "Mokoena", "Kamau", "Njoroge", "Odhiambo",
            "Diallo", "Diop", "Ndiaye", "Sow", "Fofana", "Kane", "Nguemo", "Fotso",
            "Kamdem", "Njikam", "Talla", "Ngassa", "Mbarga", "Onana", "Etoundi",
            "Nkeng", "Feudjio", "Djoumessi", "Kenmogne"
    };

    private record DeptDef(String name, String description) {}

    private static final List<DeptDef> DEPARTMENTS = List.of(
            new DeptDef("Engineering", "Product development and technical operations"),
            new DeptDef("Sales", "Revenue generation and client acquisition"),
            new DeptDef("Human Resources", "People operations and talent management"),
            new DeptDef("Finance", "Accounting, budgeting and financial planning"),
            new DeptDef("Marketing", "Brand, content and demand generation"),
            new DeptDef("Customer Support", "Post-sale customer assistance")
    );

    private static final Map<String, String[]> POSITIONS = Map.of(
            "Engineering", new String[]{"Software Engineer", "Senior Software Engineer", "DevOps Engineer", "QA Engineer"},
            "Sales", new String[]{"Sales Representative", "Account Executive", "Business Development Rep"},
            "Human Resources", new String[]{"HR Officer", "Recruiter", "Payroll Specialist"},
            "Finance", new String[]{"Accountant", "Financial Analyst", "Bookkeeper"},
            "Marketing", new String[]{"Marketing Specialist", "Content Strategist", "SEO Analyst"},
            "Customer Support", new String[]{"Support Agent", "Support Team Lead"}
    );

    private final Set<String> usedNamePairs = new HashSet<>();
    // Per-employee bookkeeping shared across generation steps
    private final Map<Long, Set<LocalDate>> approvedLeaveDaysByEmployee = new HashMap<>();
    private final Map<String, Integer> usedDaysTracker = new HashMap<>(); // key: employeeId|leaveType|year
    private final Map<String, Integer> monthlyAbsences = new HashMap<>(); // key: employeeId|year|month

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() > 0) {
            log.info("Seed data already present — skipping DataSeeder.");
            return;
        }

        log.info("Seeding development data (this may take a few seconds)...");

        ensureAdminExists();
        Map<String, Department> departments = seedDepartments();
        List<Employee> roster = seedUsersAndEmployees(departments);
        assignDepartmentManagers(roster, departments);
        seedLeaveRequestsAndBalances(roster);
        seedAttendance(roster);
        seedPayslips(roster);

        log.info("Seed data created: {} departments, {} employees (+1 admin).",
                departments.size(), roster.size());
    }

    // =====================================================================
    // Admin
    // =====================================================================

    private void ensureAdminExists() {
        if (userRepository.existsByEmail("admin@hrflow.local")) {
            return;
        }
        User admin = User.builder()
                .email("admin@hrflow.local")
                .password(passwordEncoder.encode(SEED_PASSWORD))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        log.info("Seed admin account created: admin@hrflow.local / {}", SEED_PASSWORD);
    }

    // =====================================================================
    // Departments
    // =====================================================================

    private Map<String, Department> seedDepartments() {
        Map<String, Department> saved = new LinkedHashMap<>();
        for (DeptDef def : DEPARTMENTS) {
            Department department = Department.builder()
                    .name(def.name())
                    .description(def.description())
                    .build();
            departmentRepository.save(department);
            saved.put(def.name(), department);
        }
        return saved;
    }

    // =====================================================================
    // Users + Employees
    // =====================================================================

    private record RosterEntry(Employee employee, Role role, String departmentName) {}

    private List<Employee> seedUsersAndEmployees(Map<String, Department> departments) {
        List<RosterEntry> entries = new ArrayList<>();

        // One manager per department
        for (DeptDef def : DEPARTMENTS) {
            entries.add(buildEntry(Role.MANAGER, def.name(), def.name() + " Manager"));
        }
        // Regular employees, spread across departments
        for (int i = 0; i < 24; i++) {
            DeptDef def = DEPARTMENTS.get(RANDOM.nextInt(DEPARTMENTS.size()));
            String[] positions = POSITIONS.get(def.name());
            String position = positions[RANDOM.nextInt(positions.length)];
            entries.add(buildEntry(Role.EMPLOYEE, def.name(), position));
        }

        List<Employee> savedEmployees = new ArrayList<>();

        // Pick a few employees to archive, a few with expiring contracts, a few birthdays today
        List<RosterEntry> employeeOnly = entries.stream().filter(e -> e.role() == Role.EMPLOYEE).toList();
        Set<RosterEntry> toArchive = pickRandomSubset(employeeOnly, 3);
        Set<RosterEntry> toExpireSoon = pickRandomSubset(
                employeeOnly.stream().filter(e -> !toArchive.contains(e)).toList(), 4);
        Set<RosterEntry> withBirthdayToday = pickRandomSubset(entries, 3);

        for (RosterEntry entry : entries) {
            Employee e = entry.employee();
            Department dept = departments.get(entry.departmentName());

            LocalDate hireDate = randomHireDate(entry.role());
            LocalDate archivedDate = null;
            EmployeeStatus status = EmployeeStatus.ACTIVE;
            if (toArchive.contains(entry)) {
                long minTenure = 90;
                LocalDate earliest = hireDate.plusDays(minTenure);
                LocalDate latest = TODAY.minusDays(20);
                if (earliest.isBefore(latest)) {
                    archivedDate = randomDateBetween(earliest, latest);
                    status = EmployeeStatus.ARCHIVED;
                }
            }
            LocalDate contractEndDate = toExpireSoon.contains(entry)
                    ? TODAY.plusDays(3 + RANDOM.nextInt(26))
                    : null;
            LocalDate birthDate = withBirthdayToday.contains(entry)
                    ? LocalDate.of(TODAY.getYear() - (24 + RANDOM.nextInt(22)), TODAY.getMonth(), TODAY.getDayOfMonth())
                    : LocalDate.of(TODAY.getYear() - (22 + RANDOM.nextInt(33)), 1 + RANDOM.nextInt(12), 1 + RANDOM.nextInt(28));

            String email = (e.getFirstName() + "." + e.getLastName() + "@hrflow.local").toLowerCase();

            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(SEED_PASSWORD))
                    .role(entry.role())
                    .enabled(true)
                    .build();
            userRepository.save(user);

            Employee employee = Employee.builder()
                    .firstName(e.getFirstName())
                    .lastName(e.getLastName())
                    .email(email)
                    .phone(randomPhone())
                    .birthDate(birthDate)
                    .gender(e.getGender())
                    .position(e.getPosition())
                    .salary(salaryFor(entry.role()))
                    .hireDate(hireDate)
                    .status(status)
                    .deleted(false)
                    .department(dept)
                    .user(user)
                    .contractEndDate(contractEndDate)
                    .archivedAt(archivedDate != null ? archivedDate.atTime(17, 30) : null)
                    .expectedStartTime(LocalTime.of(8, 30))
                    .contractualHoursPerDay(java.math.BigDecimal.valueOf(8))
                    .build();

            employeeRepository.save(employee);
            savedEmployees.add(employee);
        }

        // Assign each regular employee's manager = the manager of their own department
        Map<String, Employee> managerByDept = new HashMap<>();
        for (Employee emp : savedEmployees) {
            if (emp.getUser().getRole() == Role.MANAGER) {
                managerByDept.put(emp.getDepartment().getName(), emp);
            }
        }
        for (Employee emp : savedEmployees) {
            if (emp.getUser().getRole() == Role.EMPLOYEE) {
                emp.setManager(managerByDept.get(emp.getDepartment().getName()));
                employeeRepository.save(emp);
            }
        }

        return savedEmployees;
    }

    private RosterEntry buildEntry(Role role, String departmentName, String position) {
        String[] first, gender;
        boolean isMale = RANDOM.nextBoolean();
        String firstName;
        String lastName;
        String genderValue;
        while (true) {
            firstName = isMale
                    ? MALE_FIRST_NAMES[RANDOM.nextInt(MALE_FIRST_NAMES.length)]
                    : FEMALE_FIRST_NAMES[RANDOM.nextInt(FEMALE_FIRST_NAMES.length)];
            lastName = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
            String key = firstName + "|" + lastName;
            if (!usedNamePairs.contains(key)) {
                usedNamePairs.add(key);
                break;
            }
        }
        genderValue = isMale ? "MALE" : "FEMALE";

        Employee partial = Employee.builder()
                .firstName(firstName)
                .lastName(lastName)
                .gender(Gender.valueOf(genderValue))
                .position(position)
                .build();

        return new RosterEntry(partial, role, departmentName);
    }

    private void assignDepartmentManagers(List<Employee> roster, Map<String, Department> departments) {
        Map<String, Employee> managerByDept = new HashMap<>();
        for (Employee emp : roster) {
            if (emp.getUser().getRole() == Role.MANAGER) {
                managerByDept.put(emp.getDepartment().getName(), emp);
            }
        }
        for (Map.Entry<String, Department> entry : departments.entrySet()) {
            Employee manager = managerByDept.get(entry.getKey());
            if (manager != null) {
                Department dept = entry.getValue();
                dept.setManager(manager);
                departmentRepository.save(dept);
            }
        }
    }

    // =====================================================================
    // Leave requests + balances
    // =====================================================================

    private void seedLeaveRequestsAndBalances(List<Employee> roster) {
        for (Employee emp : roster) {
            LocalDate activeEnd = emp.getArchivedAt() != null ? emp.getArchivedAt().toLocalDate() : TODAY;
            LocalDate windowStart = emp.getHireDate().plusDays(14);
            if (!windowStart.isBefore(activeEnd)) {
                continue;
            }

            Employee reviewer = emp.getManager(); // null for managers themselves

            int yearsEmployed = Math.max(1, (int) java.time.temporal.ChronoUnit.DAYS.between(emp.getHireDate(), activeEnd) / 365 + 1);
            int nAnnual = (1 + RANDOM.nextInt(3)) * yearsEmployed;
            int nSick = (1 + RANDOM.nextInt(2)) * yearsEmployed;
            int nUnpaid = RANDOM.nextInt(2) * yearsEmployed;
            boolean maternity = RANDOM.nextDouble() < 0.12;

            List<LocalDate[]> takenRanges = new ArrayList<>();

            for (int i = 0; i < nAnnual; i++) {
                createLeaveIfPossible(emp, reviewer, LeaveType.ANNUAL, 2, 8, windowStart, activeEnd, takenRanges, false);
            }
            for (int i = 0; i < nSick; i++) {
                createLeaveIfPossible(emp, reviewer, LeaveType.SICK, 1, 4, windowStart, activeEnd, takenRanges, false);
            }
            for (int i = 0; i < nUnpaid; i++) {
                createLeaveIfPossible(emp, reviewer, LeaveType.UNPAID, 1, 5, windowStart, activeEnd, takenRanges, false);
            }
            if (maternity) {
                createLeaveIfPossible(emp, reviewer, LeaveType.MATERNITY_PATERNITY, 30, 90, windowStart, activeEnd, takenRanges, false);
            }
            if (emp.getStatus() == EmployeeStatus.ACTIVE && RANDOM.nextDouble() < 0.4) {
                LocalDate start = nextWeekday(TODAY.plusDays(2 + RANDOM.nextInt(14)));
                LocalDate end = addWorkingDays(start, 1 + RANDOM.nextInt(3));
                createLeaveRequest(emp, reviewer, LeaveType.ANNUAL, start, end, true);
            }
        }

        // Leave balances derived from tracked usage
        for (Employee emp : roster) {
            int hireYear = emp.getHireDate().getYear();
            int endYear = emp.getArchivedAt() != null ? emp.getArchivedAt().getYear() : TODAY.getYear();
            for (int year = hireYear; year <= endYear; year++) {
                for (Map.Entry<LeaveType, Integer> policy : LEAVE_POLICY.entrySet()) {
                    int used = usedDaysTracker.getOrDefault(
                            emp.getId() + "|" + policy.getKey() + "|" + year, 0);
                    used = Math.min(used, policy.getValue());
                    LeaveBalance balance = LeaveBalance.builder()
                            .employee(emp)
                            .leaveType(policy.getKey())
                            .year(year)
                            .allocatedDays(policy.getValue())
                            .usedDays(used)
                            .build();
                    leaveBalanceRepository.save(balance);
                }
            }
        }
    }

    private void createLeaveIfPossible(Employee emp, Employee reviewer, LeaveType type,
                                        int minDays, int maxDays, LocalDate windowStart, LocalDate activeEnd,
                                        List<LocalDate[]> takenRanges, boolean forcePending) {
        long spanDays = java.time.temporal.ChronoUnit.DAYS.between(windowStart, activeEnd);
        if (spanDays <= 0) return;

        LocalDate start = nextWeekday(windowStart.plusDays(RANDOM.nextInt((int) spanDays + 1)));
        LocalDate end = addWorkingDays(start, minDays + RANDOM.nextInt(Math.max(1, maxDays - minDays + 1)) - 1);
        if (end.isAfter(activeEnd)) return;

        for (LocalDate[] range : takenRanges) {
            if (!(end.isBefore(range[0]) || start.isAfter(range[1]))) {
                return; // overlap, skip
            }
        }
        takenRanges.add(new LocalDate[]{start, end});
        createLeaveRequest(emp, reviewer, type, start, end, false);
    }

    private void createLeaveRequest(Employee emp, Employee reviewer, LeaveType type,
                                     LocalDate start, LocalDate end, boolean forcePending) {
        int requestedDays = countWorkingDays(start, end);
        if (requestedDays == 0) return;

        LocalDate submittedDate = start.minusDays(3 + RANDOM.nextInt(12));
        if (submittedDate.isBefore(emp.getHireDate())) {
            submittedDate = emp.getHireDate().plusDays(1);
        }
        LocalDateTime submittedAt = submittedDate.atTime(9, 15);

        LeaveStatus status;
        Employee reviewedBy = null;
        LocalDateTime reviewedAt = null;
        String comment = null;

        if (forcePending) {
            status = LeaveStatus.PENDING;
        } else {
            double roll = RANDOM.nextDouble();
            if (roll < 0.82) {
                status = LeaveStatus.APPROVED;
            } else if (roll < 0.92) {
                status = LeaveStatus.REJECTED;
            } else {
                status = LeaveStatus.CANCELLED;
            }

            if (status == LeaveStatus.APPROVED || status == LeaveStatus.REJECTED) {
                reviewedBy = reviewer;
                reviewedAt = submittedAt.plusDays(RANDOM.nextInt(3)).plusHours(3);
            }
            if (status == LeaveStatus.REJECTED) {
                String[] comments = {
                        "Team is short-staffed during this period",
                        "Please choose alternate dates",
                        "Insufficient notice given workload"
                };
                comment = comments[RANDOM.nextInt(comments.length)];
            }
            if (status == LeaveStatus.APPROVED) {
                String key = emp.getId() + "|" + type + "|" + start.getYear();
                usedDaysTracker.merge(key, requestedDays, Integer::sum);
                Set<LocalDate> days = approvedLeaveDaysByEmployee.computeIfAbsent(emp.getId(), k -> new HashSet<>());
                for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                    if (isWeekday(d)) days.add(d);
                }
            }
        }

        String[] reasonsPool = reasonsFor(type);
        LeaveRequest request = LeaveRequest.builder()
                .employee(emp)
                .leaveType(type)
                .startDate(start)
                .endDate(end)
                .requestedDays(requestedDays)
                .reason(reasonsPool[RANDOM.nextInt(reasonsPool.length)])
                .status(status)
                .managerComment(comment)
                .reviewedBy(reviewedBy)
                .reviewedAt(reviewedAt)
                .build();

        leaveRequestRepository.save(request);
    }

    private String[] reasonsFor(LeaveType type) {
        return switch (type) {
            case ANNUAL -> new String[]{"Family vacation", "Personal trip", "Rest and recovery", "Visiting family"};
            case SICK -> new String[]{"Flu", "Medical appointment", "Recovering from illness", "Doctor's advice"};
            case MATERNITY_PATERNITY -> new String[]{"Maternity leave", "Paternity leave", "Newborn care"};
            case UNPAID -> new String[]{"Personal matters", "Extended family visit", "Unpaid leave request"};
        };
    }

    // =====================================================================
    // Attendance
    // =====================================================================

    private void seedAttendance(List<Employee> roster) {
        for (Employee emp : roster) {
            LocalDate activeEnd = emp.getArchivedAt() != null ? emp.getArchivedAt().toLocalDate() : TODAY;
            Set<LocalDate> leaveDays = approvedLeaveDaysByEmployee.getOrDefault(emp.getId(), Set.of());

            for (LocalDate d = emp.getHireDate(); !d.isAfter(activeEnd); d = d.plusDays(1)) {
                if (!isWeekday(d) || leaveDays.contains(d)) continue;

                String monthKey = emp.getId() + "|" + d.getYear() + "|" + d.getMonthValue();
                double roll = RANDOM.nextDouble();

                if (roll < 0.06) {
                    monthlyAbsences.merge(monthKey, 1, Integer::sum);
                    continue; // unjustified absence: no record persisted
                }

                boolean late = roll < 0.16;
                int jitter = late ? 11 + RANDOM.nextInt(35) : -10 + RANDOM.nextInt(21);
                LocalDateTime checkIn = d.atTime(8, 30).plusMinutes(jitter);
                int workedMinutes = 8 * 60 + (-20 + RANDOM.nextInt(61));
                LocalDateTime checkOut = checkIn.plusMinutes(workedMinutes);

                AttendanceRecord record = AttendanceRecord.builder()
                        .employee(emp)
                        .workDate(d)
                        .checkInAt(checkIn)
                        .checkOutAt(checkOut)
                        .status(late ? AttendanceStatus.LATE : AttendanceStatus.PRESENT)
                        .workedMinutes(workedMinutes)
                        .build();

                attendanceRecordRepository.save(record);
            }
        }
    }

    // =====================================================================
    // Payslips
    // =====================================================================

    private void seedPayslips(List<Employee> roster) {
        YearMonth currentMonth = YearMonth.from(TODAY);

        for (Employee emp : roster) {
            LocalDate activeEnd = emp.getArchivedAt() != null ? emp.getArchivedAt().toLocalDate() : TODAY;
            YearMonth cursor = YearMonth.from(emp.getHireDate());
            YearMonth lastEligible = YearMonth.from(activeEnd);

            while (cursor.isBefore(currentMonth) && !cursor.isAfter(lastEligible)) {
                int workingDays = countWorkingDays(cursor.atDay(1), cursor.atEndOfMonth());
                java.math.BigDecimal baseSalary = emp.getSalary();
                java.math.BigDecimal dailyRate = workingDays > 0
                        ? baseSalary.divide(java.math.BigDecimal.valueOf(workingDays), 2, java.math.RoundingMode.HALF_UP)
                        : java.math.BigDecimal.ZERO;

                int absences = monthlyAbsences.getOrDefault(
                        emp.getId() + "|" + cursor.getYear() + "|" + cursor.getMonthValue(), 0);
                java.math.BigDecimal deduction = dailyRate.multiply(java.math.BigDecimal.valueOf(absences));
                java.math.BigDecimal netSalary = baseSalary.subtract(deduction);

                Payslip payslip = Payslip.builder()
                        .employee(emp)
                        .year(cursor.getYear())
                        .month(cursor.getMonthValue())
                        .baseSalary(baseSalary)
                        .unjustifiedAbsenceDays(absences)
                        .dailyRate(dailyRate)
                        .deductionAmount(deduction)
                        .netSalary(netSalary)
                        .status(PayslipStatus.GENERATED)
                        .build();

                payslipRepository.save(payslip);
                cursor = cursor.plusMonths(1);
            }
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private LocalDate randomHireDate(Role role) {
        int maxOffset = role == Role.MANAGER ? 200 : 500;
        return WINDOW_START.plusDays(RANDOM.nextInt(maxOffset + 1));
    }

    private LocalDate randomDateBetween(LocalDate start, LocalDate end) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        return start.plusDays(RANDOM.nextInt((int) days + 1));
    }

    private java.math.BigDecimal salaryFor(Role role) {
        int base = switch (role) {
            case MANAGER -> 650_000;
            case ADMIN -> 800_000;
            case EMPLOYEE -> 280_000;
        };
        int variance = -20_000 + RANDOM.nextInt(80_001);
        return java.math.BigDecimal.valueOf(base + variance);
    }

    private String randomPhone() {
        return String.format("+237 6%d %03d %03d",
                50 + RANDOM.nextInt(50), RANDOM.nextInt(1000), RANDOM.nextInt(1000));
    }

    private boolean isWeekday(LocalDate d) {
        DayOfWeek dow = d.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    private LocalDate nextWeekday(LocalDate d) {
        while (!isWeekday(d)) d = d.plusDays(1);
        return d;
    }

    private LocalDate addWorkingDays(LocalDate start, int totalWorkingDays) {
        LocalDate d = start;
        int count = 1;
        while (count < totalWorkingDays) {
            d = d.plusDays(1);
            if (isWeekday(d)) count++;
        }
        return d;
    }

    private int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (isWeekday(d)) count++;
        }
        return count;
    }

    private <T> Set<T> pickRandomSubset(List<T> source, int count) {
        List<T> copy = new ArrayList<>(source);
        Collections.shuffle(copy, RANDOM);
        return new LinkedHashSet<>(copy.subList(0, Math.min(count, copy.size())));
    }
}
