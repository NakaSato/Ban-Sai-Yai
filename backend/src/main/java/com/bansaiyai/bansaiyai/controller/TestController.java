package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.seeder.DataSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
// @Profile("dev") // Optionally restrict to dev profile
public class TestController {

    private final DataSeeder dataSeeder;

    @PostMapping("/seed")
    public ResponseEntity<String> seedData() {
        try {
            dataSeeder.seed();
            return ResponseEntity.ok("Database seeded successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Seeding failed: " + e.getMessage());
        }
    }
}
