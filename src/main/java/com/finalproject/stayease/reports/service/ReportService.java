package com.finalproject.stayease.reports.service;

import com.finalproject.stayease.bookings.entity.dto.BookingDTO;
import com.finalproject.stayease.reports.dto.overview.MonthlySalesDTO;
import com.finalproject.stayease.reports.dto.overview.SummaryDTO;
import com.finalproject.stayease.reports.dto.properties.DailySummaryDTO;
import com.finalproject.stayease.reports.dto.properties.PopularRoomDTO;
import com.finalproject.stayease.reports.dto.properties.PropertiesSalesDTO;
import com.finalproject.stayease.reports.dto.user.UserStatsDTO;
import com.finalproject.stayease.users.entity.Users;

import java.util.List;

public interface ReportService {
    SummaryDTO getReportOverviewSummary(Users user);
    List<MonthlySalesDTO> monthlySales(Users user);
    List<BookingDTO> recentCompletedBookings(Users user);

    List<DailySummaryDTO> propertiesDailySalesSummary(Users user, Long propertyId, String year, String month);
    List<PopularRoomDTO> popularRooms(Users user);
    PropertiesSalesDTO getSalesReport(Users user, Long propertyId, String month);

    UserStatsDTO getUserStats(Users user);
}
