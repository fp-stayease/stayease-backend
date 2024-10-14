package com.finalproject.stayease.bookings.service.impl;

import com.finalproject.stayease.bookings.entity.BookingStatus;
import com.finalproject.stayease.bookings.entity.dto.request.BookingItemReqDTO;
import com.finalproject.stayease.bookings.entity.dto.request.BookingReqDTO;
import com.finalproject.stayease.bookings.entity.dto.request.BookingRequestReqDTO;
import com.finalproject.stayease.bookings.entity.dto.BookingDTO;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingItem;
import com.finalproject.stayease.bookings.entity.BookingRequest;
import com.finalproject.stayease.bookings.repository.BookingItemRepository;
import com.finalproject.stayease.bookings.repository.BookingRepository;
import com.finalproject.stayease.bookings.repository.BookingRequestRepository;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.exceptions.utils.DataNotFoundException;
import com.finalproject.stayease.mail.model.MailTemplate;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomAvailabilityService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.reports.dto.properties.DailySummaryDTO;
import com.finalproject.stayease.reports.dto.properties.PopularRoomDTO;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Data
@Log
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final UsersService usersService;
    private final TenantInfoService tenantInfoService;
    private final RoomService roomService;
    private final MailService mailService;
    private final RoomAvailabilityService roomAvailabilityService;
    private final PropertyService propertyService;

    // General Booking Section

    @Override
    @Transactional
    public Booking createBooking(BookingReqDTO reqDto, Long userId, Long roomId, Double amount) {
        Booking newBooking = new Booking();
        var user = usersService.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        var room = roomService.findRoomById(roomId).orElseThrow(() -> new DataNotFoundException("Room not found"));
        var property = room.getProperty();
        var tenantAccount = room.getProperty().getTenant();
        var tenant = tenantInfoService.findTenantByUserId(tenantAccount.getId());

        var availableRoom = roomAvailabilityService.setUnavailability(room.getId(), reqDto.getCheckInDate(), reqDto.getCheckOutDate());

        newBooking.setUser(user);
        newBooking.setStatus(BookingStatus.IN_PROGRESS);
        newBooking.setCheckInDate(reqDto.getCheckInDate());
        newBooking.setCheckOutDate(reqDto.getCheckOutDate());
        newBooking.setTotalAdults(reqDto.getTotalAdults());
        newBooking.setTotalChildren(reqDto.getTotalChildren());
        newBooking.setTotalInfants(reqDto.getTotalInfants());
        newBooking.setTenant(tenant);
        newBooking.setProperty(property);

        Double serviceFee = amount * 0.10;
        Double taxFee = amount * 0.11;
        Double finalPrice = serviceFee + taxFee + amount;

        newBooking.setTotalBasePrice(amount);
        newBooking.setTotalPrice(finalPrice);
        newBooking.setTaxFee(taxFee);
        newBooking.setServiceFee(serviceFee);

        bookingRepository.save(newBooking);

        createBookingItem(reqDto.getBookingItem(), newBooking, availableRoom.getRoom());

        if (reqDto.getBookingRequest() != null) {
            createBookingRequest(reqDto.getBookingRequest(), newBooking);
        }

        return bookingRepository.save(newBooking);
    }

    private void createBookingItem(BookingItemReqDTO bookingItemDto, Booking newBooking, Room room) {
        BookingItem bookingItem = new BookingItem();
        bookingItem.setBooking(newBooking);
        bookingItem.setRoom(room);
        if (bookingItemDto.getExtendingUntil() != null) {
            bookingItem.setExtendingUntil(bookingItemDto.getExtendingUntil());
        }

        bookingItemRepository.save(bookingItem);
    }

    private void createBookingRequest(BookingRequestReqDTO reqDto, Booking newBooking) {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setBooking(newBooking);
        if (reqDto.getCheckInTime() != null) {
            bookingRequest.setCheckInTime(reqDto.getCheckInTime());
        }
        if (reqDto.getCheckOutTime() != null) {
            bookingRequest.setCheckOutTime(reqDto.getCheckOutTime());
        }
        bookingRequest.setNonSmoking(reqDto.isNonSmoking());
        if (reqDto.getOther() != null) {
            bookingRequest.setOther(reqDto.getOther());
        }
        bookingRequestRepository.save(bookingRequest);
    }

    @Override
    public Booking findById(UUID bookingId) {
        return bookingRepository.findById(bookingId).
                orElseThrow(() -> new DataNotFoundException("Booking not found"));
    }

    @Override
    public BookingDTO getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Booking not found"));
        return new BookingDTO(booking);
    }

    @Override
    public Booking updateBooking(UUID bookingId, BookingStatus bookingStatus) {
        Booking booking = findById(bookingId);
        booking.setStatus(bookingStatus);
        return bookingRepository.save(booking);
    }

    // User Booking Section

    @Override
    public Page<BookingDTO> getUserBookings(Long userId, String search, Pageable pageable) {
        var user = usersService.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));

        return bookingRepository.findByUserIdAndStatusNotExpired(user.getId(), search, pageable).map(BookingDTO::new);
    }

    // Tenant Booking Section

    @Override
    public List<BookingDTO> getTenantBookings(Long userId) {
        Users user = usersService.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        TenantInfo tenant = tenantInfoService.findTenantByUserId(user.getId());
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        return bookingRepository.findByTenantId(tenant.getId(), sort).stream().map(BookingDTO::new).toList();
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void userBookingReminder() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Booking> bookings = bookingRepository.findBookingsWithCheckInTomorrow(tomorrow);
        for (Booking booking : bookings) {
            Users user = usersService.findById(booking.getUser().getId()).orElseThrow(() -> new DataNotFoundException("User not found"));
            String message = "Dear " + user.getFirstName() + "\n" +
                    "Don't forget about your booking to stay at " + booking.getProperty().getName() + ", tomorrow is the day for you to refresh your soul! \n" +
                    "Thank you very much for trusting Stay Ease \n" +
                    "Stay Ease Admin";
            MailTemplate template = new MailTemplate(user.getEmail(), "Booking Reminder", message);
            mailService.sendMail(template);
        }
    }

    @Override
    public Double countCompletedBookingsByTenantId(Long userId, Month month) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(userId);
        return bookingRepository.countCompletedBookingsByTenantId(tenant.getId(), month.getValue());
    }

    @Override
    public Double countUsersTrxByTenantId(Long userId, Month month) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(userId);
        return bookingRepository.countUserBookingsByTenantId(tenant.getId(), month.getValue());
    }

    @Override
    public List<BookingDTO> findTenantRecentCompletedBookings(Long userId) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(userId);
        return bookingRepository.findRecentCompletedBookingsByTenantId(tenant.getId())
                .stream().map(BookingDTO::new).toList();
    }

    @Override
    public List<DailySummaryDTO> getDailySummaryForMonth(Long userId, Long propertyId, Instant startDate, Instant endDate) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(userId);
        if (propertyId != null) {
            propertyService.findPropertyById(propertyId)
                    .orElseThrow(() -> new DataNotFoundException("Property not found"));
        }

        List<Object[]> results = bookingRepository.getDailySummaryForMonth(tenant.getId(), propertyId, startDate, endDate);
        return results.stream()
                .map(row -> new DailySummaryDTO(
                        (String) row[0],
                        ((Number) row[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<PopularRoomDTO> findMostPopularBookings(Long userId) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(userId);
        return bookingItemRepository.findMostBookedRoomByTenantId(tenant.getId());
    }

    @Override
    public Double getTotalRevenueByMonth(Long userId, Long propertyId, Month month) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(userId);
        double marginFromAdjustmentPrice = 0.0;
        Double totalServiceFee = 0.0;

        if (propertyId != null) {
            propertyService.findPropertyById(propertyId)
                    .orElseThrow(() -> new DataNotFoundException("Property not found"));
        }

        List<Booking> bookings = bookingRepository.findCompletedPaymentBookings(tenant.getId(), propertyId, month.getValue());
        for (Booking booking : bookings) {
            long stayingTime = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            List<BookingItem> bookingItems = booking.getBookingItems();
            log.info("Booking ID: " + booking.getId() + " Staying time: " + stayingTime);

            for (BookingItem bookingItem : bookingItems) {
                BigDecimal basePrice = bookingItem.getRoom().getBasePrice();
                double baseTotalPrice = basePrice.doubleValue() * stayingTime;

                if (booking.getTotalBasePrice() == baseTotalPrice) {
                    marginFromAdjustmentPrice += 0.0;
                    break;
                }
                marginFromAdjustmentPrice += booking.getTotalBasePrice() - baseTotalPrice;
            }

            totalServiceFee += booking.getServiceFee();
        }

        return marginFromAdjustmentPrice + totalServiceFee;
    }

    @Override
    public Double getTaxByMonthAndProperty(Long userId, Long propertyId, Month month) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(userId);
        Double totalTaxFee = 0.0;

        if (propertyId != null) {
            propertyService.findPropertyById(propertyId)
                    .orElseThrow(() -> new DataNotFoundException("Property not found"));
        }

        List<Booking> bookings = bookingRepository.findCompletedPaymentBookings(tenant.getId(), propertyId, month.getValue());
        for (Booking booking : bookings) {
            totalTaxFee += booking.getTaxFee();
        }

        return totalTaxFee;
    }

    @Override
    public Double upcomingBookingsByUserId(Long userId) {
        return bookingRepository.countUserUpcomingBookings(userId);
    }

    @Override
    public Double pastBookingsByUserId(Long userId) {
        return bookingRepository.countUserPastBookings(userId);
    }

    @Override
    public List<Booking> findFinishedBookings() {
        List<Booking> bookings = bookingRepository.findFinishedBookings();

        for (Booking booking : bookings) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
        }
        return bookings;
    }

    @Override
    public List<BookingDTO> findUpcomingUserBookings(Long userId) {
        return bookingRepository.findUpcomingUserBookings(userId)
                .stream().map(BookingDTO::new).toList();
    }
}