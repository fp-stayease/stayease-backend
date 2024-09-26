package com.finalproject.stayease.reports.service.impl;

import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.payment.service.PaymentService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.reports.dto.overview.MonthlySalesDTO;
import com.finalproject.stayease.reports.dto.overview.SummaryDTO;
import com.finalproject.stayease.reports.dto.overview.TrxDiffDTO;
import com.finalproject.stayease.reports.dto.overview.UsersDiffDTO;
import com.finalproject.stayease.reports.service.ReportService;
import com.finalproject.stayease.users.entity.Users;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    private final BookingService bookingService;
    private final PropertyService propertyService;
    private final PaymentService paymentService;

    public ReportServiceImpl(BookingService bookingService, PropertyService propertyService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.propertyService = propertyService;
        this.paymentService = paymentService;
    }

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

    private TrxDiffDTO trxDiffGen(Long userId, Month thisMonth, Month prevMonth) {
        Long thisMonthTrx = bookingService.countCompletedBookingsByTenantId(userId, thisMonth);
        Long prevMonthTrx = bookingService.countCompletedBookingsByTenantId(userId, prevMonth);
        Long trxDiffPercent = ((thisMonthTrx - prevMonthTrx) / prevMonthTrx) * 100;

        return new TrxDiffDTO(thisMonthTrx, trxDiffPercent);
    }

    private UsersDiffDTO usersDiffGen(Long userId, Month thisMonth, Month prevMonth) {
        Long totalUsersThisMonth = bookingService.countUsersTrxByTenantId(userId, thisMonth);
        Long totalUsersPrevMonth = bookingService.countUsersTrxByTenantId(userId, prevMonth);
        Long usersDiffPercent = ((totalUsersThisMonth - totalUsersPrevMonth) / totalUsersPrevMonth) * 100;

        return new UsersDiffDTO(totalUsersThisMonth, usersDiffPercent);
    }
}
