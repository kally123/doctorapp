package com.healthapp.consultation.service;

import org.springframework.context.annotation.Profile;
import com.healthapp.consultation.config.TwilioConfig;
import com.healthapp.consultation.dto.VideoTokenResponse;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;
import com.twilio.rest.video.v1.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/**
 * Service for Twilio Video SDK integration.
 * Handles video room creation, management, and access token generation.
 */
@Slf4j
@Profile("!test")
@Service
@RequiredArgsConstructor
public class TwilioVideoService {
    
    private final TwilioConfig twilioConfig;
    
    /**
     * Creates a new Twilio video room.
     *
     * @param roomName Unique name for the room
     * @return Mono with room details
     */
    public Mono<RoomInfo> createRoom(String roomName) {
        if (twilioConfig.getAccountSid().startsWith("test_")) {
            // Test mode - simulate room creation
            return Mono.just(RoomInfo.builder()
                    .roomSid("RM_test_" + UUID.randomUUID())
                    .roomName(roomName)
                    .status("in-progress")
                    .build());
        }
        
        return Mono.fromCallable(() -> {
            Room.RoomType type = "peer-to-peer".equals(twilioConfig.getRoomType()) 
                    ? Room.RoomType.PEER_TO_PEER 
                    : Room.RoomType.GROUP;
            
            // Using Room.creator() which returns a RoomCreator
            var creator = Room.creator()
                    .setUniqueName(roomName)
                    .setType(type)
                    .setMaxParticipants(twilioConfig.getMaxParticipants())
                    .setRecordParticipantsOnConnect(twilioConfig.isRecordParticipantsOnConnect());
            
            if (twilioConfig.getStatusCallbackUrl() != null && !twilioConfig.getStatusCallbackUrl().isEmpty()) {
                creator.setStatusCallback(URI.create(twilioConfig.getStatusCallbackUrl()));
            }
            
            Room room = creator.create();
            
            log.info("Created Twilio room: {} with SID: {}", roomName, room.getSid());
            
            return RoomInfo.builder()
                    .roomSid(room.getSid())
                    .roomName(room.getUniqueName())
                    .status(room.getStatus().toString())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Gets details of an existing room.
     *
     * @param roomSid The room SID
     * @return Mono with room details
     */
    public Mono<RoomInfo> getRoom(String roomSid) {
        if (twilioConfig.getAccountSid().startsWith("test_")) {
            return Mono.just(RoomInfo.builder()
                    .roomSid(roomSid)
                    .roomName("test-room")
                    .status("in-progress")
                    .build());
        }
        
        return Mono.fromCallable(() -> {
            Room room = Room.fetcher(roomSid).fetch();
            return RoomInfo.builder()
                    .roomSid(room.getSid())
                    .roomName(room.getUniqueName())
                    .status(room.getStatus().toString())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Ends an active room.
     *
     * @param roomSid The room SID to end
     * @return Mono<Void>
     */
    public Mono<Void> endRoom(String roomSid) {
        if (twilioConfig.getAccountSid().startsWith("test_")) {
            log.info("Test mode: Simulating room end for SID: {}", roomSid);
            return Mono.empty();
        }
        
        return Mono.fromRunnable(() -> {
            Room.updater(roomSid, Room.RoomStatus.COMPLETED).update();
            log.info("Ended Twilio room with SID: {}", roomSid);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
    
    /**
     * Generates an access token for a participant to join a room.
     *
     * @param identity Unique identity for the participant
     * @param roomName Room name to grant access to
     * @return VideoTokenResponse with token details
     */
    public Mono<VideoTokenResponse> generateAccessToken(String identity, String roomName) {
        return Mono.fromCallable(() -> {
            VideoGrant grant = new VideoGrant();
            grant.setRoom(roomName);
            
            AccessToken token = new AccessToken.Builder(
                    twilioConfig.getAccountSid(),
                    twilioConfig.getApiKeySid(),
                    twilioConfig.getApiKeySecret()
            )
                    .identity(identity)
                    .grant(grant)
                    .ttl(twilioConfig.getTokenTtlSeconds())
                    .build();
            
            Instant expiresAt = Instant.now().plusSeconds(twilioConfig.getTokenTtlSeconds());
            
            log.debug("Generated video token for identity: {} in room: {}", identity, roomName);
            
            return VideoTokenResponse.builder()
                    .token(token.toJwt())
                    .roomName(roomName)
                    .identity(identity)
                    .expiresAt(expiresAt)
                    .ttlSeconds(twilioConfig.getTokenTtlSeconds())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Room information holder.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoomInfo {
        private String roomSid;
        private String roomName;
        private String status;
    }
}
