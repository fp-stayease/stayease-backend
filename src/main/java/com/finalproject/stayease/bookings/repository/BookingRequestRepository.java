package com.finalproject.stayease.bookings.repository;

import com.finalproject.stayease.bookings.entity.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {

}
