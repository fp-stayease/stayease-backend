package com.finalproject.stayease.property.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Data
@Slf4j
public class HolidayService {

  private final WebClient webClient;

  public HolidayService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.baseUrl("https://date.nager.at/api/v3")
        .build();
  }

  // Region - Fetch data

  public List<Holiday> getHolidaysInDateRange(LocalDate startDate, LocalDate endDate) {
    List<Holiday> holidays = new ArrayList<>();
    for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
      List<Holiday> holidaysInYear = getHolidaysInYear(year);
      holidays.addAll(holidaysInYear);
    }
    return holidays.stream()
        .filter(holiday -> holiday.getDate().isAfter(startDate) && holiday.getDate().isBefore(endDate))
        .toList();
  }
  public List<LongWeekend> getLongWeekendsInDateRange(LocalDate startDate, LocalDate endDate) {
    List<LongWeekend> longWeekends = new ArrayList<>();
    for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
      List<LongWeekend> longWeekendsInYear = getLongWeekendsInYear(year);
      longWeekends.addAll(longWeekendsInYear);
    }
    return longWeekends.stream()
        .filter(longWeekend -> longWeekend.getStartDate().isAfter(startDate) && longWeekend.getEndDate().isBefore(endDate))
        .toList();
  }

  public List<Holiday> getHolidaysInYear(int year) {
    return webClient.get()
        .uri("/PublicHolidays/{year}/ID", year)
        .retrieve()
        .bodyToFlux(Holiday.class)
        .collectList()
        .block();
  }


  public List<LongWeekend> getLongWeekendsInYear(int year) {
    return webClient.get()
        .uri("/LongWeekend/{year}/ID", year)
        .retrieve()
        .bodyToFlux(LongWeekend.class)
        .collectList()
        .block();
  }

  // Region - BOOLEAN checks

  public boolean isHoliday(LocalDate date) {
    List<Holiday> holidays = getHolidaysInYear(date.getYear());
    return holidays.stream().anyMatch(holiday -> holiday.getDate().equals(date));
  }

  public boolean isLongWeekend(LocalDate date) {
    List<LongWeekend> longWeekends = getLongWeekendsInYear(date.getYear());
    return longWeekends.stream()
        .anyMatch(longWeekend -> longWeekend.getStartDate().equals(date) || longWeekend.getEndDate().equals(date));
  }

// Region - Inner classes

  @Data
  public static class Holiday {

    private LocalDate date;
    private String localName;
    private String name;
  }

  @Data
  public static class LongWeekend {

    private LocalDate startDate;
    private LocalDate endDate;
    private int dayCount;
    private boolean needBridgeDay;

    public Stream<LocalDate> getDateStream() {
      return Stream.iterate(startDate, date -> date.plusDays(1))
          .limit(startDate.until(endDate).getDays());
    }

  }

}
