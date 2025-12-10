package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.seeder.StressTestSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/seed")
@RequiredArgsConstructor
// Optional: @Profile("dev") or similar to restrict access in prod
public class SeederController {

    private final StressTestSeeder stressTestSeeder;

    @PostMapping("/stress")
    public ResponseEntity<String> triggerStressSeed() {
        stressTestSeeder.seedStressData();
        return ResponseEntity.ok("Stress test data seeded successfully.");
    }
}
