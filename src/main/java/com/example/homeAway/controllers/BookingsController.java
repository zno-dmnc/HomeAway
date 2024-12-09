package com.example.homeAway.controllers;

import com.example.homeAway.exception.ResourceNotFoundException;
import com.example.homeAway.models.Bookings;
import com.example.homeAway.models.Properties;
import com.example.homeAway.models.User;
import com.example.homeAway.repositories.BookingsRepository;
import com.example.homeAway.repositories.PropertiesRepository;
import com.example.homeAway.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        // Validate the property
        Properties property = propertiesRepository.findById(booking.getProperty().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + booking.getProperty().getId()));

        // Validate the user
        User user = userRepository.findById(booking.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + booking.getUser().getId()));

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

        booking.setStatus("Pending");

        booking.setTotalPrice(property.getPricePerNight() * booking.getCheckInDate().until(booking.getCheckOutDate()).getDays());

        return bookingsRepository.save(booking);
    }


    // Update a booking
    @PutMapping("/{id}")
    public Bookings updateBooking(@PathVariable Long id, @RequestBody Bookings bookingDetails) {
        Bookings booking = bookingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (bookingDetails.getCheckInDate() != null) {
            booking.setCheckInDate(bookingDetails.getCheckInDate());
        }
        if (bookingDetails.getCheckOutDate() != null) {
            booking.setCheckOutDate(bookingDetails.getCheckOutDate());
        }
        if (bookingDetails.getTotalPrice() != null) {
            booking.setTotalPrice(bookingDetails.getTotalPrice());
        }
        if (bookingDetails.getStatus() != null) {
            booking.setStatus(bookingDetails.getStatus());
        }

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
