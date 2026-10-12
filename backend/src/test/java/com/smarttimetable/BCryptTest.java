package com.smarttimetable;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BCryptTest {
    @Test
    void testPasswordMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$iJ5jnJVGlT9c5KM0Y41GLeumSLmBtUjeOtKhg8lUsSGKRwZJO2ati";
        System.out.println("Matches Password@123: " + encoder.matches("Password@123", hash));
        assertTrue(encoder.matches("Password@123", hash));
    }
}
