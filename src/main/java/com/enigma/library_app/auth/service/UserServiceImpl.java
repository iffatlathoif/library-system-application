package com.enigma.library_app.auth.service;

import com.enigma.library_app.auth.constant.Role;
import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.auth.security.JwtService;
import com.enigma.library_app.auth.security.UserInfoDetails;
import com.enigma.library_app.common.ValidationService;
import com.enigma.library_app.dto.auth.request.LoginRequest;
import com.enigma.library_app.dto.auth.request.RegisterRequest;
import com.enigma.library_app.dto.auth.response.LoginResponse;
import com.enigma.library_app.dto.auth.response.UserResponse;
import com.enigma.library_app.exception.ResourceNotFoundException;
import com.enigma.library_app.model.master.location.entity.Location;
import com.enigma.library_app.repository.LocationRepository;
import com.enigma.library_app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Transactional
    @Override
    public UserResponse create(RegisterRequest request) {
        //validationService.validate(request);
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRoles(request.getRole());
        user.setCreatedAt(LocalDateTime.now());
        if(request.getRole().equals(Role.STAFF)){
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));

            user.setLocation(location);
        }

        userRepository.save(user);
        return toUserResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        validationService.validate(loginRequest);

        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Username not found")
        );

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or password is invalid");
        } else {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            LoginResponse response = new LoginResponse();
            response.setUsername(authentication.getName());
            response.setRole(user.getRoles().toString());
            response.setToken(jwtService.generateToken(user.getUsername()));
            return response;
        }
    }

    private UserResponse toUserResponse(User user){
        return UserResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRoles())
                .locationId(user.getLocation() != null ? user.getLocation().getLocationId() : null)
                .locationName(user.getLocation() != null ? user.getLocation().getName() : null)
                .build();
    }
    @Transactional
    @Override
    public String delete(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found!"));
        String kode = user.getUsername();
        userRepository.delete(user);
        return kode;
    }

    @Override
    public User getEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found!"));
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserInfoDetails userDetails = (UserInfoDetails) authentication.getPrincipal();
        String username = userDetails.getUsername(); // atau getEmail() kalau kamu tambahkan

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    @Transactional
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);
        logoutHandler.setClearAuthentication(true);
        logoutHandler.setInvalidateHttpSession(true);
        SecurityContextHolder.clearContext();

        String token = jwtService.extractToken(request);
        if (token != null) {
            jwtService.blacklistToken(token);
        }

        return "Logged out successfully";
    }


    @Override
    public User getId(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user;
    }
}
