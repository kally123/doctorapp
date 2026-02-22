package com.healthapp.prescription.service;

import org.springframework.context.annotation.Profile;
import com.healthapp.prescription.domain.Prescription;
import com.healthapp.prescription.domain.PrescriptionItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.context.annotation.Profile;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Service for digitally signing prescriptions.
 */
@Slf4j
@Profile("!test")
@Service
public class DigitalSignatureService {

    private final boolean testMode;
    private final PrivateKey signingKey;
    private final X509Certificate signingCertificate;
    private final String certificateSerial;

    public DigitalSignatureService(
            @Value("${signing.test-mode:true}") boolean testMode,
            @Value("${signing.key-store-path:}") String keyStorePath,
            @Value("${signing.key-store-password:}") String keyStorePassword,
            @Value("${signing.key-alias:}") String keyAlias
    ) {
        this.testMode = testMode;
        
        PrivateKey tempKey = null;
        X509Certificate tempCert = null;
        String tempSerial = "TEST-CERT-001";
        
        if (!testMode && keyStorePath != null && !keyStorePath.isEmpty()) {
            try {
                KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePassword);
                tempKey = (PrivateKey) keyStore.getKey(keyAlias, keyStorePassword.toCharArray());
                tempCert = (X509Certificate) keyStore.getCertificate(keyAlias);
                tempSerial = tempCert.getSerialNumber().toString();
            } catch (Exception e) {
                log.error("Failed to load signing certificate, falling back to test mode", e);
            }
        }
        
        this.signingKey = tempKey;
        this.signingCertificate = tempCert;
        this.certificateSerial = tempSerial;
    }

    private KeyStore loadKeyStore(String path, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (var is = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
            if (is == null) {
                throw new IllegalArgumentException("KeyStore not found: " + path);
            }
            keyStore.load(is, password.toCharArray());
        }
        return keyStore;
    }

    /**
     * Sign a prescription and return signature result.
     */
    public Mono<SignatureResult> signPrescription(Prescription prescription, List<PrescriptionItem> items) {
        return Mono.fromCallable(() -> {
            String contentToSign = createCanonicalContent(prescription, items);
            String contentHash = hashContent(contentToSign);
            
            String signatureHash;
            if (testMode || signingKey == null) {
                // Test mode - create a simulated signature
                signatureHash = "TEST_SIG_" + Base64.getEncoder().encodeToString(
                        contentHash.getBytes(StandardCharsets.UTF_8));
                log.info("Test mode: Generated simulated signature for prescription: {}", 
                        prescription.getPrescriptionNumber());
            } else {
                // Production mode - real signature
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initSign(signingKey);
                signature.update(contentToSign.getBytes(StandardCharsets.UTF_8));
                byte[] signatureBytes = signature.sign();
                signatureHash = Base64.getEncoder().encodeToString(signatureBytes);
            }
            
            return SignatureResult.builder()
                    .signatureHash(signatureHash)
                    .certificateSerial(certificateSerial)
                    .signedAt(Instant.now())
                    .contentHash(contentHash)
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Verify prescription signature.
     */
    public Mono<Boolean> verifySignature(Prescription prescription, List<PrescriptionItem> items, 
                                          String signatureHash) {
        return Mono.fromCallable(() -> {
            if (testMode || signingCertificate == null) {
                // Test mode - verify simulated signature
                String contentToSign = createCanonicalContent(prescription, items);
                String contentHash = hashContent(contentToSign);
                String expectedSig = "TEST_SIG_" + Base64.getEncoder().encodeToString(
                        contentHash.getBytes(StandardCharsets.UTF_8));
                return expectedSig.equals(signatureHash);
            }
            
            // Production mode - real verification
            String contentToSign = createCanonicalContent(prescription, items);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureHash);
            
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(signingCertificate);
            signature.update(contentToSign.getBytes(StandardCharsets.UTF_8));
            
            return signature.verify(signatureBytes);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String createCanonicalContent(Prescription prescription, List<PrescriptionItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("PRESCRIPTION|");
        sb.append(prescription.getId()).append("|");
        sb.append(prescription.getPrescriptionNumber()).append("|");
        sb.append(prescription.getPatientId()).append("|");
        sb.append(prescription.getDoctorId()).append("|");
        sb.append(prescription.getPrescriptionDate()).append("|");
        
        // Include medicine items in order
        items.stream()
                .sorted(Comparator.comparing(PrescriptionItem::getSequenceOrder))
                .forEach(item -> {
                    sb.append(item.getMedicineName()).append(":");
                    sb.append(item.getDosage()).append(":");
                    sb.append(item.getFrequency()).append(":");
                    sb.append(item.getDuration()).append("|");
                });
        
        return sb.toString();
    }

    private String hashContent(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SignatureResult {
        private String signatureHash;
        private String certificateSerial;
        private Instant signedAt;
        private String contentHash;
    }
}
