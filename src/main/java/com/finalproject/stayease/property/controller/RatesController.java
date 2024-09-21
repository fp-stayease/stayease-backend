package com.finalproject.stayease.property.controller;

import com.finalproject.stayease.property.entity.dto.PeakSeasonRateDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.DailyPriceDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rates")
@Slf4j
@Data
public class RatesController {
  
  private final PeakSeasonRateService rateService;
  private final UsersService usersService;

  @GetMapping("/{propertyId}")
  public ResponseEntity<Response<List<RoomAdjustedRatesDTO>>> getAdjustedRates(@PathVariable Long propertyId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return Response.successfulResponse(200, "Listing all adjusted rates for property ID: " + propertyId
                                            + " on date: " + date, rateService.findAvailableRoomRates(propertyId, date));
  }

  @GetMapping("/{propertyId}/daily")
  public ResponseEntity<Response<List<DailyPriceDTO>>> getDailyRates(@PathVariable Long propertyId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return Response.successfulResponse(200, "Listing all lowest daily rates for property ID: " + propertyId
                                            + " from date: " + startDate + " to date: " + endDate,
        rateService.findLowestDailyRoomRates(propertyId, startDate, endDate));
  }

  @GetMapping("/{propertyId}/daily/cumulative")
  public ResponseEntity<Response<List<DailyPriceDTO>>> getLowestCumulativeRates(@PathVariable Long propertyId,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate endDate) {
    return Response.successfulResponse(200, "Listing all lowest cumulative rates for property ID: " + propertyId
                                            + " from date: " + startDate + " to date: " + endDate,
        rateService.findCumulativeRoomRates(propertyId, startDate, endDate));
  }

  @PostMapping("/properties/{propertyId}")
  public ResponseEntity<Response<PeakSeasonRateDTO>> setPeakSeasonRate(@PathVariable Long propertyId,
      @RequestBody SetPeakSeasonRateRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Adjustment Rate Successfully Set!",
        new PeakSeasonRateDTO(rateService.setPeakSeasonRate(tenant, propertyId, requestDTO)));
  }

  @PutMapping("/{rateId}")
  public ResponseEntity<Response<PeakSeasonRateDTO>> updatePeakSeasonRate(
      @PathVariable Long rateId,
      @RequestBody SetPeakSeasonRateRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Adjustment Rate Successfully Updated!",
        new PeakSeasonRateDTO(rateService.updatePeakSeasonRate(tenant, rateId, requestDTO)));
  }

}
