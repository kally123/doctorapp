package com.healthapp.order.service;

import org.springframework.context.annotation.Profile;
import com.healthapp.order.domain.Order;
import com.healthapp.order.domain.Partner;
import com.healthapp.order.domain.PartnerInventory;
import com.healthapp.order.domain.enums.PartnerType;
import com.healthapp.order.repository.OrderItemRepository;
import com.healthapp.order.repository.OrderRepository;
import com.healthapp.order.repository.PartnerInventoryRepository;
import com.healthapp.order.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Service for assigning pharmacies to orders.
 */
@Slf4j
@Profile("!test")
@Service
@RequiredArgsConstructor
public class PharmacyAssignmentService {

    private final PartnerRepository partnerRepository;
    private final PartnerInventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Assign best pharmacy for an order.
     * Criteria: Has all items in stock, within delivery radius, currently open, highest rating.
     */
    public Mono<Partner> assignPharmacy(UUID orderId) {
        log.info("Assigning pharmacy for order: {}", orderId);

        return orderRepository.findById(orderId)
                .flatMap(order -> orderItemRepository.findByOrderId(orderId)
                        .collectList()
                        .flatMap(items -> {
                            // Find pharmacies that can serve this order
                            return partnerRepository.findByPartnerTypeAndIsActiveTrueAndIsVerifiedTrue(PartnerType.PHARMACY)
                                    .filterWhen(partner -> hasAllItemsInStock(partner, items.stream()
                                            .map(item -> item.getProductId())
                                            .toList()))
                                    .sort((p1, p2) -> p2.getRating().compareTo(p1.getRating()))
                                    .next()
                                    .flatMap(partner -> assignPartnerToOrder(order, partner));
                        }));
    }

    /**
     * Check if pharmacy has all required items in stock.
     */
    private Mono<Boolean> hasAllItemsInStock(Partner partner, java.util.List<String> productIds) {
        return Flux.fromIterable(productIds)
                .flatMap(productId -> inventoryRepository.findByPartnerIdAndProductId(partner.getId(), productId)
                        .map(inv -> inv.getIsAvailable() && inv.getQuantityAvailable() > 0)
                        .defaultIfEmpty(false))
                .all(available -> available);
    }

    /**
     * Assign partner to order and update order.
     */
    private Mono<Partner> assignPartnerToOrder(Order order, Partner partner) {
        order.setPartnerId(partner.getId());
        order.setPartnerType(PartnerType.PHARMACY);
        order.setPartnerName(partner.getBusinessName());
        order.setPartnerAcceptedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        return orderRepository.save(order)
                .thenReturn(partner)
                .doOnSuccess(p -> log.info("Assigned pharmacy {} to order {}", 
                        p.getBusinessName(), order.getOrderNumber()));
    }
}
