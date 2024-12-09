package com.example.homeAway.controllers;

import com.example.homeAway.exception.ResourceNotFoundException;
import com.example.homeAway.models.Properties;
import com.example.homeAway.models.User;
import com.example.homeAway.repositories.PropertiesRepository;
import com.example.homeAway.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertiesController {

    @Autowired
    private PropertiesRepository propertiesRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all properties
    @GetMapping("/property/all")
    public List<Properties> getAllProperties() {
        return propertiesRepository.findAll();
    }

    // Get property by ID
    @GetMapping("/property/{id}")
    public Properties getPropertyById(@PathVariable Long id) {
        return propertiesRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }

    // Get properties by city
    @GetMapping("/property/city/{city}")
    public List<Properties> getPropertyByCity(@PathVariable String city) {
        return propertiesRepository.findByCity(city);
    }

    // Get properties by name
    @GetMapping("/property/name/{name}")
    public List<Properties> getPropertyByName(@PathVariable String name) {
        return propertiesRepository.findByName(name);
    }

    // Get properties by host
    @GetMapping("/property/host/{hostId}")
    public List<Properties> getPropertyByHost(@PathVariable long hostId) {
        // Fetch the host by ID
        User host = userRepository.findById(hostId).orElseThrow(() -> new ResourceNotFoundException("Host not found with id: " + hostId));
        return propertiesRepository.findByHost(host); // Query by the host
    }

    // Add a new property
    @PostMapping("/property/add")
    public Properties addProperty(@RequestBody Properties property) {
        // Validate that the host object is provided in the request
        if (property.getHost() == null) {
            throw new ResourceNotFoundException("Host is required");
        }

        // Get the host ID from the provided host object
        long hostId = property.getHost().getId();  // Assuming the User object has getId() method

        // Fetch the host (User) from the database
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Host not found with id: " + hostId));

        if(host.getRole().equals("customer")) {
            throw new IllegalArgumentException("Host must be a host");
        }
        // Set the host for the property
        property.setHost(host);  // Ensure the host is properly set

        // Validate required fields
        if (property.getName() == null || property.getName().isEmpty()) {
            throw new IllegalArgumentException("Property name is required");
        }
        if (property.getDescription() == null || property.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Property description is required");
        }
        if (property.getAddress() == null || property.getAddress().isEmpty()) {
            throw new IllegalArgumentException("Property address is required");
        }
        if (property.getCity() == null || property.getCity().isEmpty()) {
            throw new IllegalArgumentException("Property city is required");
        }
        if (property.getPricePerNight() == null || property.getPricePerNight() <= 0) {
            throw new IllegalArgumentException("Property price per night is required and must be greater than zero");
        }
        if (property.getMaxGuests() == null || property.getMaxGuests() <= 0) {
            throw new IllegalArgumentException("Property max guests is required and must be greater than zero");
        }
        if (property.getPropertyType() == null || property.getPropertyType().isEmpty()) {
            throw new IllegalArgumentException("Property type is required");
        }

        // Save property with host properly set
        return propertiesRepository.save(property);
    }




    // Update an existing property
    @PutMapping("/property/{id}")
    public Properties updateProperty(@PathVariable Long id, @RequestBody Properties property) {
        Properties existingProperty = propertiesRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        // Update fields
        if (property.getName() != null) {
            existingProperty.setName(property.getName());
        }
        if (property.getDescription() != null) {
            existingProperty.setDescription(property.getDescription());
        }
        if (property.getAddress() != null) {
            existingProperty.setAddress(property.getAddress());
        }
        if (property.getCity() != null) {
            existingProperty.setCity(property.getCity());
        }
        if (property.getPricePerNight() != null) {
            existingProperty.setPricePerNight(property.getPricePerNight());
        }
        if (property.getMaxGuests() != null) {
            existingProperty.setMaxGuests(property.getMaxGuests());
        }
        if (property.getPropertyType() != null) {
            existingProperty.setPropertyType(property.getPropertyType());
        }

        return propertiesRepository.save(existingProperty);
    }

    // Delete a property
    @DeleteMapping("/property/{id}")
    public String deleteProperty(@PathVariable Long id) {
        propertiesRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        propertiesRepository.deleteById(id);
        return "Property deleted successfully!";
    }

}
