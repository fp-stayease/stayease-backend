package com.finalproject.stayease.property.service.impl;

import java.time.LocalDate;
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

  public boolean isHoliday(LocalDate date) {
    // Call the API to get the holidays
    // Check if the date is a holiday
    // return true if it is a holiday
    // return false if it is not a holiday
    return Boolean.TRUE.equals(webClient.get()
        .uri("/PublicHolidays/{year}/ID", date.getYear())
        .retrieve()
        .bodyToFlux(Holiday.class)
        .any(holiday -> holiday.getDate().equals(date))
        .block());
  }

  public boolean isLongWeekend(LocalDate date) {
    // Check if the date is a long weekend
    // return true if date is >= startDate and date is <= endDate
    // return false if it is not a long weekend
    return Boolean.TRUE.equals(webClient.get()
        .uri("/LongWeekend/{year}/ID", date.getYear())
        .retrieve()
        .bodyToFlux(LongWeekend.class)
        .any(longWeekend -> !date.isBefore(longWeekend.getStartDate()) && !date.isAfter(longWeekend.getEndDate()))
        .block());
  }


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
  }

}
