package com.finalproject.stayease.reports.service.impl;

import com.finalproject.stayease.bookings.entity.dto.BookingDTO;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.payment.service.PaymentService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.reports.dto.overview.MonthlySalesDTO;
import com.finalproject.stayease.reports.dto.overview.SummaryDTO;
import com.finalproject.stayease.reports.dto.overview.TrxDiffDTO;
import com.finalproject.stayease.reports.dto.overview.UsersDiffDTO;
import com.finalproject.stayease.reports.dto.properties.DailySummaryDTO;
import com.finalproject.stayease.reports.dto.properties.PopularRoomDTO;
import com.finalproject.stayease.reports.service.ReportService;
import com.finalproject.stayease.users.entity.Users;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
@Log
public class ReportServiceImpl implements ReportService {
    private final BookingService bookingService;
    private final PropertyService propertyService;
    private final PaymentService paymentService;

    public ReportServiceImpl(BookingService bookingService, PropertyService propertyService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.propertyService = propertyService;
        this.paymentService = paymentService;
    }

    // Overview Sections

    @Override
    public SummaryDTO getReportOverviewSummary(Users user) {
        LocalDate today = LocalDate.now();
        Month thisMonth = Month.from(today);
        Month prevMonth = Month.from(today.minusMonths(1));

        // Total Completed Transaction
        TrxDiffDTO trxDiff = trxDiffGen(user.getId(), thisMonth, prevMonth);

        // Total Properties
        Long totalProperties = propertyService.tenantPropertyCount(user);

        // Total Users made Transactions
        UsersDiffDTO usersDiff = usersDiffGen(user.getId(), thisMonth, prevMonth);

        // TO DO: Revenue Diff

        return new SummaryDTO(trxDiff, usersDiff, totalProperties);
    }

    @Override
    public List<MonthlySalesDTO> monthlySales(Users user) {
        return paymentService.getMonthlySalesByTenantId(user.getId());
    }

    @Override
    public List<BookingDTO> recentCompletedBookings(Users user) {
        return bookingService.findTenantRecentCompletedBookings(user.getId());
    }

    // Properties Report

    @Override
    public List<PopularRoomDTO> popularRooms(Users user) {
        return bookingService.findMostPopularBookings(user.getId());
    }

    @Override
    public List<DailySummaryDTO> propertiesDailySalesSummary(Users user, String year, String month) {
        LocalDate startDate;
        LocalDate endDate;

        if (year == null) {
            year = String.valueOf(LocalDate.now().getYear());
        }

        if (month == null) {
            month = LocalDate.now().getMonth().name();
        }

        int selectedYear = Integer.parseInt(year);
        Month selectedMonth = Month.valueOf(month.toUpperCase());

        startDate = LocalDate.of(selectedYear, selectedMonth, 1);
        endDate = startDate.plusMonths(1).minusDays(1);

        Instant startInstant = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = endDate.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);

        log.info("start date " + startInstant);
        log.info("end date " + endInstant);

        return bookingService.getDailySummaryForMonth(user.getId(), startInstant, endInstant);
    }

    private TrxDiffDTO trxDiffGen(Long userId, Month thisMonth, Month prevMonth) {
        Long thisMonthTrx = bookingService.countCompletedBookingsByTenantId(userId, thisMonth);
        Long prevMonthTrx = bookingService.countCompletedBookingsByTenantId(userId, prevMonth);
        if (prevMonthTrx == 0) {
            Long trxDiffPercent = (thisMonthTrx - prevMonthTrx) * 100;
            return new TrxDiffDTO(thisMonthTrx, trxDiffPercent);
        }
        Long trxDiffPercent = ((thisMonthTrx - prevMonthTrx) / prevMonthTrx) * 100;

        return new TrxDiffDTO(thisMonthTrx, trxDiffPercent);
    }

    private UsersDiffDTO usersDiffGen(Long userId, Month thisMonth, Month prevMonth) {
        Long totalUsersThisMonth = bookingService.countUsersTrxByTenantId(userId, thisMonth);
        Long totalUsersPrevMonth = bookingService.countUsersTrxByTenantId(userId, prevMonth);
        if (totalUsersPrevMonth == 0) {
            Long trxDiffPercent = (totalUsersThisMonth - totalUsersPrevMonth) * 100;
            return new UsersDiffDTO(totalUsersThisMonth, trxDiffPercent);
        }
        Long usersDiffPercent = ((totalUsersThisMonth - totalUsersPrevMonth) / totalUsersPrevMonth) * 100;

        return new UsersDiffDTO(totalUsersThisMonth, usersDiffPercent);
    }
}
