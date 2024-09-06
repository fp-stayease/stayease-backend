package com.finalproject.stayease.helpers;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingItem;
import com.finalproject.stayease.users.entity.Users;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HtmlDataMap {
    public Map<String, String> dataGenerator(Booking booking) {
        var bookingItems = booking.getBookingItems();
        Users user = booking.getUser();
        Map<String, String> data = new HashMap<>();
        data.put("userEmail", user.getEmail());
        data.put("userName", user.getFirstName() + " " + user.getLastName());
        data.put("checkInDate", booking.getCheckInDate().toString());
        data.put("checkOutDate", booking.getCheckOutDate().toString());
        data.put("bookingId", booking.getId().toString());
        data.put("totalAdults", Integer.toString(booking.getTotalAdults()));
        if (booking.getTotalChildren() != 0) {
            data.put("totalChildren", "<p>" + booking.getTotalChildren() + " Children</p>");
        } else {
            data.put("totalChildren", "");
        }
        if (booking.getTotalInfants() != 0) {
            data.put("totalInfants", "<p>" + booking.getTotalInfants() + " Infants</p>");
        } else {
            data.put("totalInfants", "");
        }

        StringBuilder rooms = new StringBuilder();
        for (BookingItem bookingItem : bookingItems) {
            rooms.append("<p>").append(bookingItem.getRoom().getName()).append("</p>");
        }
        data.put("roomName", rooms.toString());

        var bookingRequest = booking.getBookingRequest();
        if (bookingRequest.getCheckInTime() != null) {
            data.put("checkInTime", bookingRequest.getCheckInTime().toString());
        } else {
            data.put("checkInTime", "No request");
        }
        if (bookingRequest.getCheckOutTime() != null) {
            data.put("checkOutTime", bookingRequest.getCheckOutTime().toString());
        } else {
            data.put("checkOutTime", "No request");
        }
        if (bookingRequest.isNonSmoking()) {
            data.put("smokingRoom", "Yes");
        } else {
            data.put("smokingRoom", "No");
        }
        if (bookingRequest.getOther() != null) {
            data.put("other", bookingRequest.getOther());
        } else {
            data.put("other", "No request");
        }

        data.put("propertyName", booking.getProperty().getName());
        data.put("propertyAddress", booking.getProperty().getAddress());

        return data;
    }
}
