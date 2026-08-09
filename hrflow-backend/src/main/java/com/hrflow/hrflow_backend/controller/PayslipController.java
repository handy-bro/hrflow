package com.hrflow.hrflow_backend.controller;

import com.hrflow.hrflow_backend.dto.payslip.GeneratePayslipRequest;
import com.hrflow.hrflow_backend.dto.payslip.PayslipResponse;
import com.hrflow.hrflow_backend.entity.Payslip;
import com.hrflow.hrflow_backend.entity.User;
import com.hrflow.hrflow_backend.exceptionHandler.employees.EmployeeNotFoundException;
import com.hrflow.hrflow_backend.repository.EmployeeRepository;
import com.hrflow.hrflow_backend.service.PayslipService;
import com.hrflow.hrflow_backend.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "Payslips", description = "Endpoints for managing employee payslips")
@RestController
@RequestMapping("/api/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final PayslipService payslipService;
    private final EmployeeRepository employeeRepository;
    private final StorageService storageService;

    @Operation(
            summary = "Generate a payslip for an employee",
            description = "Generates a new payslip for the specified employee."
    )
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/employee/{employeeId}/generate")
    public ResponseEntity<PayslipResponse> generate(
            @PathVariable("employeeId") Long employeeId, @Valid @RequestBody GeneratePayslipRequest request) {
        return ResponseEntity.ok(payslipService.generate(employeeId, request));
    }

    @Operation(
            summary = "Regenerate a payslip for an employee",
            description = "Regenerates an existing payslip for the specified employee."
    )
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/employee/{employeeId}/regenerate")
    public ResponseEntity<PayslipResponse> regenerate(
            @PathVariable("employeeId") Long employeeId, @Valid @RequestBody GeneratePayslipRequest request) {
        return ResponseEntity.ok(payslipService.regenerate(employeeId, request));
    }

    @Operation(
            summary = "Get payslip history for the current user",
            description = "Fetches the payslip history for the authenticated user."
    )
    @GetMapping("/me")
    public ResponseEntity<List<PayslipResponse>> myHistory(@AuthenticationPrincipal User currentUser) {
        Long employeeId = currentEmployeeId(currentUser);
        return ResponseEntity.ok(payslipService.getHistory(employeeId));
    }

    @Operation(
            summary = "Get payslip history for an employee",
            description = "Fetches the payslip history for the specified employee. Requires ADMIN role."
    )
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayslipResponse>> employeeHistory(@PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(payslipService.getHistory(employeeId));
    }

    @Operation(
            summary = "Download a payslip as PDF",
            description = "Downloads the specified payslip as a PDF file. Ensures the user has the necessary permissions."
    )
    @GetMapping("/{id}/download")
    public void downloadPdf(
            @PathVariable("id") Long id, HttpServletResponse response,
            @AuthenticationPrincipal User currentUser) throws IOException {

        Payslip payslip = payslipService.getForDownload(id, currentUser); // verifies permissions internally

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=payslip-" + payslip.getYear() + "-" + payslip.getMonth() + ".pdf");

        storageService.download(payslip.getPdfKey(), response.getOutputStream());
    }

    private Long currentEmployeeId(User currentUser) {
        return employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EmployeeNotFoundException("No employee profile linked to this account"))
                .getId();
    }
}
