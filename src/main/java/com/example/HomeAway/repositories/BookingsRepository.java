package com.example.HomeAway.repositories;

import com.example.HomeAway.models.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingsRepository extends JpaRepository<Bookings, Long> {

    // Custom query to find bookings by property ID
    List<Bookings> findByPropertyId(Long propertyId);

    // Custom query to find bookings by user ID
    List<Bookings> findByUserId(Long userId);

    @Query("SELECT b FROM Bookings b WHERE b.property.id = :propertyId " +
            "AND (:checkInDate < b.checkOutDate AND :checkOutDate > b.checkInDate) AND b.id != :bookingId")
    List<Bookings> findConflictingBookings(@Param("propertyId") Long propertyId,
                                           @Param("checkInDate") LocalDate checkInDate,
                                           @Param("checkOutDate") LocalDate checkOutDate,
                                           @Param("bookingId") Long bookingId);



}
