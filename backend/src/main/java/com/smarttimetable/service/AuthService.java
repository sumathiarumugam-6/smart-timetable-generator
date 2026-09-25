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
        try {
            // Ensure roles exist
            for (RoleName roleName : RoleName.values()) {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    roleRepository.save(Role.builder().name(roleName).description(roleName.name()).build());
                }
            }

            Role superAdminRole = roleRepository.findByName(RoleName.ROLE_SUPER_ADMIN).orElse(null);
            Role hodRole = roleRepository.findByName(RoleName.ROLE_HOD).orElse(null);
            Role facultyRole = roleRepository.findByName(RoleName.ROLE_FACULTY).orElse(null);

            Department cse = departmentRepository.findByCode("CSE").orElseGet(() ->
                    departmentRepository.save(Department.builder().code("CSE").name("Computer Science & Engineering").hodName("Dr. Aris Thorne").building("Turing Block").build())
            );

            // Auto-create/reset superadmin and admin with guaranteed password encoding
            if (userRepository.findByUsername("superadmin").isEmpty()) {
                userRepository.save(User.builder()
                        .username("superadmin")
                        .password(passwordEncoder.encode("Password@123"))
                        .email("admin@enggcollege.edu")
                        .fullName("System Super Admin")
                        .role(superAdminRole)
                        .department(cse)
                        .build());
            }

            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Password@123"))
                        .email("admin@timetable.edu")
                        .fullName("Administrator")
                        .role(superAdminRole)
                        .department(cse)
                        .build());
            }

            if (userRepository.findByUsername("hod_cse").isEmpty()) {
                userRepository.save(User.builder()
                        .username("hod_cse")
                        .password(passwordEncoder.encode("Password@123"))
                        .email("hod.cse@enggcollege.edu")
                        .fullName("Dr. Aris Thorne")
                        .role(hodRole)
                        .department(cse)
                        .build());
            }

            if (userRepository.findByUsername("prof_john").isEmpty()) {
                userRepository.save(User.builder()
                        .username("prof_john")
                        .password(passwordEncoder.encode("Password@123"))
                        .email("john.doe@enggcollege.edu")
                        .fullName("Prof. John Doe")
                        .role(facultyRole)
                        .department(cse)
                        .build());
            }
        } catch (Exception e) {
            System.err.println("Init default users warning: " + e.getMessage());
        }
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
