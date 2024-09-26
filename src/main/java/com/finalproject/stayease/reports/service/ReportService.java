package com.finalproject.stayease.reports.service;

import com.finalproject.stayease.reports.dto.overview.MonthlySalesDTO;
import com.finalproject.stayease.reports.dto.overview.SummaryDTO;
import com.finalproject.stayease.users.entity.Users;

import java.util.List;

public interface ReportService {
    SummaryDTO getReportOverviewSummary(Users user);
    List<MonthlySalesDTO> monthlySales(Users user);
}
