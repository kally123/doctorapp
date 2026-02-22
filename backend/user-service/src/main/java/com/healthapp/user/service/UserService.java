package com.healthapp.user.service;

import com.healthapp.common.exception.ConflictException;
import com.healthapp.common.exception.NotFoundException;
import com.healthapp.common.exception.UnauthorizedException;
import com.healthapp.common.util.StringUtils;
import com.healthapp.user.event.UserEventPublisher;
import com.healthapp.user.mapper.UserMapper;
import com.healthapp.user.model.dto.*;
import com.healthapp.user.model.entity.User;
import com.healthapp.user.model.entity.UserRole;
import com.healthapp.user.model.entity.UserStatus;
import com.healthapp.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for user management operations.
 */
@Slf4j
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final TokenService tokenService;

    @Autowired(required = false)
    private UserEventPublisher eventPublisher;

    public UserService(UserRepository userRepository, UserMapper userMapper,
                       PasswordEncoder passwordEncoder, OtpService otpService,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.tokenService = tokenService;
    }

    /**
     * Registers a new user.
     */
    @Transactional
    public Mono<RegisterResponse> register(RegisterRequest request) {
        return validateUniqueEmail(request.getEmail())
                .then(validateUniquePhone(request.getPhone()))
                .then(Mono.defer(() -> {
                    User user = User.builder()
                            .email(request.getEmail())
                            .phone(request.getPhone())
                            .passwordHash(passwordEncoder.encode(request.getPassword()))
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .role(request.getRole() != null ? request.getRole() : UserRole.PATIENT)
                            .status(UserStatus.PENDING_VERIFICATION)
                            .createdAt(Instant.now())
                            .build();
                    
                    return userRepository.save(user);
                }))
                .flatMap(user -> {
                    String identifier = user.getPhone() != null ? user.getPhone() : user.getEmail();
                    return otpService.generateOtp(identifier)
                            .flatMap(otp -> otpService.sendOtp(identifier, otp))
                            .thenReturn(user);
                })
                .doOnSuccess(user -> {
                    if (eventPublisher != null) {
                        eventPublisher.publishUserRegistered(user);
                    }
                })
                .map(user -> RegisterResponse.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .otpSent(true)
                        .message("Registration successful. Please verify with OTP.")
                        .build());
    }
    
    /**
     * Verifies user account with OTP.
     */
    @Transactional
    public Mono<LoginResponse> verifyOtp(VerifyOtpRequest request) {
        return userRepository.findById(UUID.fromString(request.getUserId()))
                .switchIfEmpty(Mono.error(new NotFoundException("User", request.getUserId())))
                .flatMap(user -> {
                    String identifier = user.getPhone() != null ? user.getPhone() : user.getEmail();
                    return otpService.verifyOtp(identifier, request.getOtp())
                            .flatMap(verified -> {
                                if (!verified) {
                                    return Mono.error(new UnauthorizedException("Invalid OTP"));
                                }
                                
                                // Activate user
                                user.setStatus(UserStatus.ACTIVE);
                                if (user.getPhone() != null) {
                                    user.setPhoneVerified(true);
                                }
                                if (user.getEmail() != null) {
                                    user.setEmailVerified(true);
                                }
                                user.setUpdatedAt(Instant.now());
                                
                                return userRepository.save(user);
                            });
                })
                .doOnSuccess(user -> {
                    if (eventPublisher != null) {
                        eventPublisher.publishUserVerified(user);
                    }
                })
                .flatMap(user -> tokenService.generateTokens(user)
                        .map(tokens -> LoginResponse.of(tokens, userMapper.toDto(user))));
    }
    
    /**
     * Authenticates a user with email/phone and password.
     */
    @Transactional
    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByEmailOrPhone(request.getIdentifier())
                .switchIfEmpty(Mono.error(UnauthorizedException.invalidCredentials()))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                        return Mono.error(UnauthorizedException.invalidCredentials());
                    }
                    
                    if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
                        return Mono.error(UnauthorizedException.accountNotVerified());
                    }
                    
                    if (user.getStatus() == UserStatus.SUSPENDED) {
                        return Mono.error(UnauthorizedException.accountSuspended());
                    }
                    
                    // Update last login
                    user.setLastLoginAt(Instant.now());
                    return userRepository.save(user);
                })
                .doOnSuccess(user -> {
                    if (eventPublisher != null) {
                        eventPublisher.publishUserLogin(user);
                    }
                })
                .flatMap(user -> tokenService.generateTokens(user)
                        .map(tokens -> LoginResponse.of(tokens, userMapper.toDto(user))));
    }
    
    /**
     * Refreshes access token using refresh token.
     */
    public Mono<TokenPair> refreshToken(RefreshTokenRequest request) {
        return tokenService.validateRefreshToken(request.getRefreshToken())
                .switchIfEmpty(Mono.error(UnauthorizedException.invalidToken()))
                .flatMap(userRepository::findById)
                .switchIfEmpty(Mono.error(UnauthorizedException.invalidToken()))
                .flatMap(user -> tokenService.refreshTokens(request.getRefreshToken(), user));
    }
    
    /**
     * Initiates password reset flow.
     */
    public Mono<Void> forgotPassword(ForgotPasswordRequest request) {
        return userRepository.findByEmailOrPhone(request.getIdentifier())
                .flatMap(user -> {
                    String identifier = user.getPhone() != null ? user.getPhone() : user.getEmail();
                    return otpService.generateOtp(identifier)
                            .flatMap(otp -> otpService.sendOtp(identifier, otp));
                });
    }
    
    /**
     * Resets password with OTP verification.
     */
    @Transactional
    public Mono<Void> resetPassword(ResetPasswordRequest request) {
        return userRepository.findByEmailOrPhone(request.getIdentifier())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    String identifier = user.getPhone() != null ? user.getPhone() : user.getEmail();
                    return otpService.verifyOtp(identifier, request.getOtp())
                            .flatMap(verified -> {
                                if (!verified) {
                                    return Mono.error(new UnauthorizedException("Invalid OTP"));
                                }
                                
                                user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                                user.setUpdatedAt(Instant.now());
                                
                                return userRepository.save(user)
                                        .then(tokenService.revokeAllUserTokens(user.getId()));
                            });
                })
                .doOnSuccess(v -> log.info("Password reset for: {}", StringUtils.maskEmail(request.getIdentifier())));
    }
    
    /**
     * Gets the current user profile.
     */
    public Mono<UserDto> getCurrentUser(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User", userId.toString())))
                .map(userMapper::toDto);
    }
    
    /**
     * Updates the current user profile.
     */
    @Transactional
    public Mono<UserDto> updateProfile(UUID userId, UpdateProfileRequest request) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User", userId.toString())))
                .flatMap(user -> {
                    userMapper.updateFromDto(request, user);
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user);
                })
                .doOnSuccess(user -> {
                    if (eventPublisher != null) {
                        eventPublisher.publishUserUpdated(user);
                    }
                })
                .map(userMapper::toDto);
    }
    
    /**
     * Logs out user by revoking refresh token.
     */
    public Mono<Void> logout(String refreshToken) {
        return tokenService.revokeToken(refreshToken);
    }
    
    // Helper methods
    
    private Mono<Void> validateUniqueEmail(String email) {
        if (email == null) {
            return Mono.empty();
        }
        return userRepository.existsByEmail(email)
                .flatMap(exists -> exists 
                        ? Mono.error(ConflictException.duplicateEmail(email))
                        : Mono.empty());
    }
    
    private Mono<Void> validateUniquePhone(String phone) {
        if (phone == null) {
            return Mono.empty();
        }
        return userRepository.existsByPhone(phone)
                .flatMap(exists -> exists 
                        ? Mono.error(ConflictException.duplicatePhone(phone))
                        : Mono.empty());
    }
}
