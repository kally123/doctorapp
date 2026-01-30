package com.healthapp.order.service;

import com.healthapp.order.domain.DeliveryAddress;
import com.healthapp.order.dto.AddressRequest;
import com.healthapp.order.dto.AddressResponse;
import com.healthapp.order.repository.DeliveryAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for managing delivery addresses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final DeliveryAddressRepository addressRepository;

    /**
     * Get all addresses for a user.
     */
    public Flux<AddressResponse> getUserAddresses(UUID userId) {
        return addressRepository.findByUserIdAndIsActiveTrue(userId)
                .map(this::toResponse);
    }

    /**
     * Get address by ID.
     */
    public Mono<AddressResponse> getAddress(UUID addressId) {
        return addressRepository.findById(addressId)
                .map(this::toResponse);
    }

    /**
     * Get default address for user.
     */
    public Mono<AddressResponse> getDefaultAddress(UUID userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(this::toResponse);
    }

    /**
     * Create a new address.
     */
    public Mono<AddressResponse> createAddress(UUID userId, AddressRequest request) {
        log.info("Creating address for user: {}", userId);

        DeliveryAddress address = DeliveryAddress.builder()
                .userId(userId)
                .addressType(request.getAddressType())
                .label(request.getLabel())
                .recipientName(request.getRecipientName())
                .phone(request.getPhone())
                .alternatePhone(request.getAlternatePhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .landmark(request.getLandmark())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isDefault(request.getIsDefault())
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Mono<Void> clearDefault = Mono.empty();
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault = addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .flatMap(existing -> {
                        existing.setIsDefault(false);
                        return addressRepository.save(existing);
                    })
                    .then();
        }

        return clearDefault
                .then(addressRepository.save(address))
                .map(this::toResponse);
    }

    /**
     * Alias for createAddress for compatibility.
     */
    public Mono<AddressResponse> addAddress(UUID userId, AddressRequest request) {
        return createAddress(userId, request);
    }

    /**
     * Update an existing address.
     */
    public Mono<AddressResponse> updateAddress(UUID addressId, AddressRequest request) {
        log.info("Updating address: {}", addressId);

        return addressRepository.findById(addressId)
                .flatMap(address -> {
                    address.setAddressType(request.getAddressType());
                    address.setLabel(request.getLabel());
                    address.setRecipientName(request.getRecipientName());
                    address.setPhone(request.getPhone());
                    address.setAlternatePhone(request.getAlternatePhone());
                    address.setAddressLine1(request.getAddressLine1());
                    address.setAddressLine2(request.getAddressLine2());
                    address.setLandmark(request.getLandmark());
                    address.setCity(request.getCity());
                    address.setState(request.getState());
                    address.setPostalCode(request.getPostalCode());
                    address.setCountry(request.getCountry());
                    address.setLatitude(request.getLatitude());
                    address.setLongitude(request.getLongitude());
                    address.setUpdatedAt(Instant.now());

                    if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
                        address.setIsDefault(true);
                        return addressRepository.clearDefaultForUser(address.getUserId(), addressId)
                                .then(addressRepository.save(address));
                    }

                    return addressRepository.save(address);
                })
                .map(this::toResponse);
    }

    /**
     * Delete (deactivate) an address.
     */
    public Mono<Void> deleteAddress(UUID addressId) {
        log.info("Deleting address: {}", addressId);

        return addressRepository.findById(addressId)
                .flatMap(address -> {
                    address.setIsActive(false);
                    address.setUpdatedAt(Instant.now());
                    return addressRepository.save(address);
                })
                .then();
    }

    /**
     * Set address as default.
     */
    public Mono<AddressResponse> setDefaultAddress(UUID userId, UUID addressId) {
        log.info("Setting default address: {} for user: {}", addressId, userId);

        return addressRepository.clearDefaultForUser(userId, addressId)
                .then(addressRepository.findById(addressId))
                .flatMap(address -> {
                    address.setIsDefault(true);
                    address.setUpdatedAt(Instant.now());
                    return addressRepository.save(address);
                })
                .map(this::toResponse);
    }

    private AddressResponse toResponse(DeliveryAddress address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUserId())
                .addressType(address.getAddressType())
                .label(address.getLabel())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .alternatePhone(address.getAlternatePhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.getIsDefault())
                .build();
    }
}
