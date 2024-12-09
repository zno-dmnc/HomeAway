package com.example.homeAway.repositories;

import com.example.homeAway.models.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingsRepository extends JpaRepository<Bookings, Long> {

    // Custom query to find bookings by property ID
    List<Bookings> findByPropertyId(Long propertyId);

    // Custom query to find bookings by user ID
    List<Bookings> findByUserId(Long userId);
}
