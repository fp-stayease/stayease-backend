package com.finalproject.stayease.bookings.service.impl;

import com.finalproject.stayease.bookings.dto.BookingItemReqDto;
import com.finalproject.stayease.bookings.dto.BookingReqDto;
import com.finalproject.stayease.bookings.dto.BookingRequestReqDto;
import com.finalproject.stayease.bookings.dto.BookingResDto;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingItem;
import com.finalproject.stayease.bookings.entity.BookingRequest;
import com.finalproject.stayease.bookings.repository.BookingItemRepository;
import com.finalproject.stayease.bookings.repository.BookingRepository;
import com.finalproject.stayease.bookings.repository.BookingRequestRepository;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.mail.model.MailTemplate;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.users.dto.TenantInfoResDto;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final UsersService usersService;
    private final TenantInfoService tenantInfoService;
    private final RoomService roomService;
    private final MailService mailService;

    public BookingServiceImpl(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository, BookingRequestRepository bookingRequestRepository, UsersService usersService, TenantInfoService tenantInfoService, RoomService roomService, MailService mailService) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.bookingRequestRepository = bookingRequestRepository;
        this.usersService = usersService;
        this.tenantInfoService = tenantInfoService;
        this.roomService = roomService;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    public Booking createBooking(BookingReqDto reqDto, Long userId, Long roomId, Double amount) {
        Booking newBooking = new Booking();
        var user = usersService.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));

        var room = roomService.findById(roomId);
        var tenantAccount = room.getProperty().getTenant();
        var property = room.getProperty();
        var tenant = tenantInfoService.findTenantByUserId(tenantAccount.getId());

        newBooking.setUser(user);
        newBooking.setTotalPrice(amount);
        newBooking.setStatus("In progress");
        newBooking.setCheckInDate(reqDto.getCheckInDate());
        newBooking.setCheckOutDate(reqDto.getCheckOutDate());
        newBooking.setTotalAdults(reqDto.getTotalAdults());
        newBooking.setTotalChildren(reqDto.getTotalChildren());
        newBooking.setTotalInfants(reqDto.getTotalInfants());
        newBooking.setTenant(tenant);
        newBooking.setProperty(property);

        bookingRepository.save(newBooking);

        createBookingItem(reqDto.getBookingItem(), newBooking, room);

        if (reqDto.getBookingRequest() != null) {
            createBookingRequest(reqDto.getBookingRequest(), newBooking);
        }

        return bookingRepository.save(newBooking);
    }

    @Override
    public void createBookingItem(BookingItemReqDto bookingItemDto, Booking newBooking, Room room) {
        BookingItem bookingItem = new BookingItem();
        bookingItem.setBooking(newBooking);
        bookingItem.setRoom(room);
        if (bookingItemDto.getExtendingUntil() != null) {
            bookingItem.setExtendingUntil(bookingItemDto.getExtendingUntil());
        }

        bookingItemRepository.save(bookingItem);
    }

    @Override
    public void createBookingRequest(BookingRequestReqDto reqDto, Booking newBooking) {
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
    public BookingResDto getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Booking not found"));
        return booking.toResDto();
    }

    @Override
    public Page<BookingResDto> getUserBookings(Long userId, Pageable pageable) {
        // TO DO: find and validate user
        var user = usersService.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));

        return bookingRepository.findByUserIdAndStatusNotExpired(user.getId(), pageable).map(Booking::toResDto);
    }

    @Override
    public Booking updateBooking(UUID bookingId, String bookingStatus) {
        Booking booking = findById(bookingId);
        booking.setStatus(bookingStatus);
        return bookingRepository.save(booking);
    }

    @Override
    public Page<BookingResDto> getTenantBookings(Long userId, Pageable pageable) {
        Users user = usersService.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        TenantInfo tenant = tenantInfoService.findTenantByUserId(user.getId());

        return bookingRepository.findByTenantId(tenant.getId(), pageable).map(Booking::toResDto);
    }

    @Override
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
}
