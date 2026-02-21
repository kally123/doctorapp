package com.healthapp.order.controller;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.dto.AddressRequest;
import com.healthapp.order.dto.AddressResponse;
import com.healthapp.order.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for delivery address management.
 */
@Slf4j
@Profile("!test")
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Delivery address management APIs")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Get user addresses", description = "Get all delivery addresses for the current user")
    public Flux<AddressResponse> getUserAddresses(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Getting addresses for user: {}", userId);
        return addressService.getUserAddresses(userId);
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "Get address", description = "Get a specific delivery address by ID")
    public Mono<ResponseEntity<AddressResponse>> getAddress(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID addressId) {
        log.info("Getting address: {} for user: {}", addressId, userId);
        return addressService.getAddress(addressId)
                .filter(addr -> addr.getUserId().equals(userId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add address", description = "Add a new delivery address")
    public Mono<ResponseEntity<AddressResponse>> addAddress(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody AddressRequest request) {
        log.info("Adding address for user: {}", userId);
        return addressService.addAddress(userId, request)
                .map(addr -> ResponseEntity.status(HttpStatus.CREATED).body(addr));
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Update address", description = "Update an existing delivery address")
    public Mono<ResponseEntity<AddressResponse>> updateAddress(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request) {
        log.info("Updating address: {} for user: {}", addressId, userId);
        return addressService.getAddress(addressId)
                .filter(addr -> addr.getUserId().equals(userId))
                .flatMap(addr -> addressService.updateAddress(addressId, request))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete address", description = "Delete a delivery address")
    public Mono<ResponseEntity<Void>> deleteAddress(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID addressId) {
        log.info("Deleting address: {} for user: {}", addressId, userId);
        return addressService.getAddress(addressId)
                .filter(addr -> addr.getUserId().equals(userId))
                .flatMap(addr -> addressService.deleteAddress(addressId))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{addressId}/default")
    @Operation(summary = "Set default address", description = "Set an address as the default delivery address")
    public Mono<ResponseEntity<AddressResponse>> setDefaultAddress(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID addressId) {
        log.info("Setting default address: {} for user: {}", addressId, userId);
        return addressService.getAddress(addressId)
                .filter(addr -> addr.getUserId().equals(userId))
                .flatMap(addr -> addressService.setDefaultAddress(userId, addressId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/default")
    @Operation(summary = "Get default address", description = "Get the default delivery address for the current user")
    public Mono<ResponseEntity<AddressResponse>> getDefaultAddress(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Getting default address for user: {}", userId);
        return addressService.getDefaultAddress(userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
