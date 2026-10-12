package com.smarttimetable.service;

import com.smarttimetable.config.JwtTokenProvider;
import com.smarttimetable.config.UserPrincipal;
import com.smarttimetable.dto.*;
import com.smarttimetable.entity.Department;
import com.smarttimetable.entity.Role;
import com.smarttimetable.entity.User;
import com.smarttimetable.enums.RoleName;
import com.smarttimetable.repository.DepartmentRepository;
import com.smarttimetable.repository.RoleRepository;
import com.smarttimetable.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostConstruct
    public void initDefaultRolesAndUsers() {
        // Initial roles, departments, users, faculty, classrooms, batches, and mappings
        // are deterministically managed via data.sql to guarantee consistent IDs and prevent collisions.
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        User user = userDetails.getUser();

        return JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName().name())
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .build();
    }

    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        RoleName roleEnum = RoleName.ROLE_FACULTY;
        if (registerRequest.getRole() != null) {
            try {
                roleEnum = RoleName.valueOf(registerRequest.getRole());
            } catch (Exception ignored) {}
        }

        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        Department dept = null;
        if (registerRequest.getDepartmentId() != null) {
            dept = departmentRepository.findById(registerRequest.getDepartmentId()).orElse(null);
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .role(role)
                .department(dept)
                .build();

        return userRepository.save(user);
    }
}
