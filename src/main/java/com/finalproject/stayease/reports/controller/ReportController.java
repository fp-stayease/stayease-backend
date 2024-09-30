package com.finalproject.stayease.reports.controller;

import com.finalproject.stayease.reports.service.ReportService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;
    private final UsersService usersService;

    public ReportController(ReportService reportService, UsersService usersService) {
        this.reportService = reportService;
        this.usersService = usersService;
    }

    // Overview sections

    @GetMapping("/overview")
    public ResponseEntity<?> getReportsOverview() {
        Users tenant = usersService.getLoggedUser();
        var response = reportService.getReportOverviewSummary(tenant);
        return Response.successfulResponse("Report overview fetched", response);
    }

    @GetMapping("/overview/monthly-sales")
    public ResponseEntity<?> getReportsOverviewMonthlySales() {
        Users tenant = usersService.getLoggedUser();
        var response = reportService.monthlySales(tenant);
        return Response.successfulResponse("Monthly sales fetched", response);
    }

    // Properties Report
}
