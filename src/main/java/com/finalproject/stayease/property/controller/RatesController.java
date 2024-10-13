package com.finalproject.stayease.property.controller;

import com.finalproject.stayease.property.entity.dto.PeakSeasonRateDTO;
import com.finalproject.stayease.property.entity.dto.PropertyRateSettingDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPropertyRateSettingsDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.DailyPriceDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyRateSettingsService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
  private final PropertyRateSettingsService propertyRateSettingsService;
  private final UsersService usersService;

  @GetMapping
  @PreAuthorize("hasRole('TENANT')")
  public ResponseEntity<Response<List<PeakSeasonRateDTO>>> getTenantCurrentRates() {
    Users tenant = usersService.getLoggedUser();
    log.info("Tenant: {}", tenant.getTenantInfo().getBusinessName());
    List<PeakSeasonRateDTO> currentRatesDTO = rateService
        .getTenantCurrentRates(tenant)
        .stream()
        .map(PeakSeasonRateDTO::new)
        .sorted(Comparator.comparing(PeakSeasonRateDTO::getRateId))
        .toList();
    log.info("Current Rates: {}", !currentRatesDTO.isEmpty() ? currentRatesDTO.getFirst() : null);
    return Response.successfulResponse(200,
        "Listing all current rates for tenant: " + tenant.getTenantInfo().getBusinessName(), currentRatesDTO);
  }

  @GetMapping(params = {"propertyId"})
  public ResponseEntity<Response<List<RoomAdjustedRatesDTO>>> getAdjustedRates(
      @RequestParam("propertyId") Long propertyId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    if (date == null) {
      date = LocalDate.now();
    }
    return Response.successfulResponse(200, "Listing all adjusted rates for property ID: " + propertyId
                                            + " on date: " + date,
        rateService.findAvailableRoomRates(propertyId, date));
  }

  @GetMapping(value = "/daily", params = {"propertyId", "startDate", "endDate"})
  public ResponseEntity<Response<List<DailyPriceDTO>>> getDailyRates(@RequestParam Long propertyId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return Response.successfulResponse(200, "Listing all lowest daily rates for property ID: " + propertyId
                                            + " from date: " + startDate + " to date: " + endDate,
        rateService.findLowestDailyRoomRates(propertyId, startDate, endDate));
  }

  @GetMapping(value = "/daily/cumulative", params = {"propertyId"})
  public ResponseEntity<Response<List<DailyPriceDTO>>> getLowestCumulativeRates(@RequestParam Long propertyId,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate endDate) {
    return Response.successfulResponse(200, "Listing all lowest cumulative rates for property ID: " + propertyId
                                            + " from date: " + startDate + " to date: " + endDate,
        rateService.findCumulativeRoomRates(propertyId, startDate, endDate));
  }

  // Region - POST Requests

  @PostMapping(params = {"propertyId"})
  public ResponseEntity<Response<PeakSeasonRateDTO>> setPeakSeasonRate(@RequestParam Long propertyId,
      @RequestBody SetPeakSeasonRateRequestDTO requestDTO) {
    log.info("request:" + requestDTO.toString());
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Adjustment Rate Successfully Set!",
        new PeakSeasonRateDTO(rateService.setPeakSeasonRate(tenant, propertyId, requestDTO)));
  }

  // Region - PUT Requests

  @PutMapping("/{rateId}")
  public ResponseEntity<Response<PeakSeasonRateDTO>> updatePeakSeasonRate(
      @PathVariable Long rateId,
      @RequestBody SetPeakSeasonRateRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    PeakSeasonRateDTO response = new PeakSeasonRateDTO(rateService.updatePeakSeasonRate(tenant, rateId, requestDTO));
    log.info("Updated Rate: " + response);
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Adjustment Rate Successfully Updated!",
        response);
  }

  // Region - DELETE Requests
  @DeleteMapping("/{rateId}")
  public ResponseEntity<Response<String>> deletePeakSeasonRate(@PathVariable Long rateId) {
    Users tenant = usersService.getLoggedUser();
    rateService.removePeakSeasonRate(tenant, rateId);
    return Response.successfulResponse(HttpStatus.OK.value(), "Adjustment Rate Successfully Deleted!",
        "Deleted rate ID: " + rateId);
  }


  // End - Property Rates Settings
  @GetMapping(value = "/auto", params = {"propertyId"})
  @PreAuthorize("hasRole('TENANT')")
  public ResponseEntity<Response<PropertyRateSettingDTO>> getPropertyRateSettings(@RequestParam Long propertyId) {
    return Response.successfulResponse(HttpStatus.OK.value(), "Property Rate Settings Retrieved Successfully!",
        new PropertyRateSettingDTO(propertyRateSettingsService.getOrCreatePropertyRateSettings(propertyId)));
  }

  @PutMapping(value = "/auto", params = {"propertyId"})
  @PreAuthorize("hasRole('TENANT')")
  public ResponseEntity<Response<PropertyRateSettingDTO>> updatePropertyRateSettings(@RequestParam Long propertyId,
      @RequestBody SetPropertyRateSettingsDTO request) {
    log.info("Updating property rate settings for property ID: " + propertyId);
    return Response.successfulResponse(HttpStatus.OK.value(), "Property Rate Settings Updated Successfully!",
        new PropertyRateSettingDTO(propertyRateSettingsService.updatePropertyRateSettings(propertyId, request)));
  }

  @DeleteMapping(value = "/auto", params = {"propertyId"})
  @PreAuthorize("hasRole('TENANT')")
  public ResponseEntity<Response<String>> deactivateAutoRates(@RequestParam Long propertyId) {
    propertyRateSettingsService.deactivateAutoRates(propertyId);
    log.info("Deactivated auto rates for property ID: " + propertyId);
    return Response.successfulResponse(HttpStatus.OK.value(), "Property Rate Settings Deactivated Successfully!",
        "Deactivated settings for property ID: " + propertyId);
  }

}
