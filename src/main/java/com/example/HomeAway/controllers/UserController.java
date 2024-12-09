package com.example.HomeAway.controllers;

import java.util.List;
import java.util.Objects;

import com.example.HomeAway.exception.ResourceNotFoundException;
import com.example.HomeAway.models.User;
import com.example.HomeAway.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    //get all users
    @GetMapping("/users")
    public List<User> getALlUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {

        if(userRepository.findByEmail(user.getEmail()) != null) {
            throw new ResourceNotFoundException("User already exists with email: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User loginUser(@RequestBody User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());

        if(existingUser == null) {
            throw new ResourceNotFoundException("User not found with email: " + user.getEmail());
        }

        if(!Objects.equals(user.getPassword(), existingUser.getPassword())) {
            throw new ResourceNotFoundException("Wrong password");
        }
        return existingUser;
    }

    @PutMapping("/user/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhone(user.getPhone());
        existingUser.setUsername(user.getUsername());
        return userRepository.save(existingUser);
    }

    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
        return "User deleted successfully!";
    }



}
