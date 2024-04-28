package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        String name = payload.getName();
        String email = payload.getEmail();

        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setCreditCards(new HashSet<>());
        userRepository.save(user);
        return ResponseEntity.ok(user.getId());
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        userRepository.delete(user.get());
        return ResponseEntity.ok("User deleted");
    }
}
