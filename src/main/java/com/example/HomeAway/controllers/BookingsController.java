package com.example.HomeAway.controllers;

import com.example.HomeAway.exception.ResourceNotFoundException;
import com.example.HomeAway.models.Bookings;
import com.example.HomeAway.models.Properties;
import com.example.HomeAway.models.User;
import com.example.HomeAway.repositories.BookingsRepository;
import com.example.HomeAway.repositories.PropertiesRepository;
import com.example.HomeAway.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/bookings")
public class BookingsController {

    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private PropertiesRepository propertiesRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all bookings
    @GetMapping("/all")
    public List<Bookings> getAllBookings() {
        return bookingsRepository.findAll();
    }

    // Get bookings by property ID
    @GetMapping("/property/{propertyId}")
    public List<Bookings> getBookingsByPropertyId(@PathVariable Long propertyId) {
        return bookingsRepository.findByPropertyId(propertyId);
    }

    // Get bookings by user ID
    @GetMapping("/user/{userId}")
    public List<Bookings> getBookingsByUserId(@PathVariable Long userId) {
        return bookingsRepository.findByUserId(userId);
    }

    // Create a new booking
    @PostMapping("/add")
    public Bookings createBooking(@RequestBody Bookings booking) {
        // Validate the user
        User user = userRepository.findById(booking.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + booking.getUser().getId()));

        if (!"customer".equals(user.getRole())){
            throw new ResourceNotFoundException("User does not have role of customer");
        }

        // Validate the property
        Properties property = propertiesRepository.findById(booking.getProperty().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + booking.getProperty().getId()));

        if (booking.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("The check-in date cannot be in the past.");
        }

        // Check for conflicting bookings
        List<Bookings> existingBookings = bookingsRepository.findByPropertyId(property.getId());
        boolean hasConflict = existingBookings.stream().anyMatch(existing ->
                booking.getCheckInDate().isBefore(existing.getCheckOutDate()) &&
                        booking.getCheckOutDate().isAfter(existing.getCheckInDate())
        );

        if (hasConflict) {
            throw new IllegalStateException("The property is already booked for the selected dates.");
        }

        // Set property and user objects
        booking.setProperty(property);
        booking.setUser(user);


        if (booking.getNoOfGuest() > property.getMaxGuests()){
            throw new IllegalStateException("The number of guests exceeded the maximum allowed.");
        }


        booking.setStatus("Pending");

        booking.setTotalPrice(property.getPricePerNight() * booking.getCheckInDate().until(booking.getCheckOutDate()).getDays());

        return bookingsRepository.save(booking);
    }


    // Update a booking
    @PutMapping("/{id}")
    public Bookings updateBooking(@PathVariable Long id, @RequestBody Bookings bookingDetails) {
        Bookings booking = bookingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        if (bookingDetails.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("The check-in date cannot be in the past.");
        }
        if (bookingDetails.getCheckInDate() != null || bookingDetails.getCheckOutDate() != null) {
            List<Bookings> conflictingBookings = bookingsRepository.findConflictingBookings(
                    booking.getProperty().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getId());  // Pass current booking's ID to exclude it

            if (!conflictingBookings.isEmpty()) {
                throw new IllegalStateException("The property is already booked for the selected dates.");
            }
        }

        if (bookingDetails.getCheckInDate() != null) {
            booking.setCheckInDate(bookingDetails.getCheckInDate());
        }
        if (bookingDetails.getCheckOutDate() != null) {
            booking.setCheckOutDate(bookingDetails.getCheckOutDate());
        }

        Properties property = propertiesRepository.findById(booking.getProperty().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + booking.getProperty().getId()));


        if (bookingDetails.getTotalPrice() != null) {
            booking.setTotalPrice(bookingDetails.getTotalPrice());
        }
        if (bookingDetails.getStatus() != null) {
            booking.setStatus(bookingDetails.getStatus());
        }
        booking.setTotalPrice(property.getPricePerNight() * booking.getCheckInDate().until(booking.getCheckOutDate()).getDays());

        return bookingsRepository.save(booking);
    }

    @PutMapping("/status/{id}")
    public Bookings updateBookingStatus(@PathVariable Long id, @RequestBody Bookings bookingDetails) {
        Bookings booking = bookingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (bookingDetails.getStatus() != null) {
            booking.setStatus(bookingDetails.getStatus());
        }

        return bookingsRepository.save(booking);
    }

    // Delete a booking
    @DeleteMapping("/{id}")
    public String deleteBooking(@PathVariable Long id) {
        bookingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        bookingsRepository.deleteById(id);
        return "Booking deleted successfully!";
    }
}
