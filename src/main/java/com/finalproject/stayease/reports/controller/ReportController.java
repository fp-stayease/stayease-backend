package com.finalproject.stayease.reports.controller;

import com.finalproject.stayease.bookings.entity.dto.BookingDTO;
import com.finalproject.stayease.reports.dto.properties.DailySummaryDTO;
import com.finalproject.stayease.reports.dto.properties.PopularRoomDTO;
import com.finalproject.stayease.reports.dto.properties.PropertiesSalesDTO;
import com.finalproject.stayease.reports.service.ReportService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Log
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

    @GetMapping("/overview/recent-completed-transactions")
    public ResponseEntity<Response<List<BookingDTO>>> getTenantRecentCompletedBookings() {
        Users tenant = usersService.getLoggedUser();
        var response = reportService.recentCompletedBookings(tenant);
        return Response.successfulResponse("Recent bookings fetched", response);
    }

    // Properties Report

    @GetMapping("/properties")
    public ResponseEntity<Response<List<DailySummaryDTO>>> getDailySalesReportByMonth(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) Long propertyId
    ) {
        Users tenant = usersService.getLoggedUser();
        var response = reportService.propertiesDailySalesSummary(tenant, propertyId, year, month);

        return Response.successfulResponse("Daily sales report fetched", response);
    }

    @GetMapping("/properties/popular")
    public ResponseEntity<Response<List<PopularRoomDTO>>> getPopularRooms() {
        Users tenant = usersService.getLoggedUser();
        var response = reportService.popularRooms(tenant);
        return Response.successfulResponse("Popular rooms fetched", response);
    }

    @GetMapping("/properties/sales")
    public ResponseEntity<Response<PropertiesSalesDTO>> getPropertiesSales(
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) String month
    ) {
        Users tenant = usersService.getLoggedUser();
        var response = reportService.getSalesReport(tenant, propertyId, month);

        return Response.successfulResponse("Sales report fetched", response);
    }
}
