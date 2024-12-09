package com.example.homeAway.repositories;

import com.example.homeAway.models.Properties;
import com.example.homeAway.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertiesRepository extends JpaRepository<Properties, Long> {
        List<Properties> findByCity(String city); // Find properties by city
        List<Properties> findByName(String name); // Find properties by name

        // Updated to use the relationship with User
        List<Properties> findByHost(User host);
}
