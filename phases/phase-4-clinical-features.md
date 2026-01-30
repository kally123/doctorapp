# Phase 4: Clinical Features - Detailed Implementation Plan

## Phase Overview

| Attribute | Details |
|-----------|---------|
| **Duration** | 4 Weeks |
| **Start Date** | _Phase 3 End Date + 1 day_ |
| **End Date** | _Start Date + 4 weeks_ |
| **Team Size** | 10-12 members |
| **Goal** | Complete prescription management and electronic health records system |

---

## Phase 4 Objectives

1. ✅ Build Prescription Service with medicine database
2. ✅ Implement prescription creation during/after consultations
3. ✅ Enable digital signatures for prescriptions
4. ✅ Generate downloadable prescription PDFs
5. ✅ Build EHR Service for patient health records
6. ✅ Implement secure document upload and storage
7. ✅ Create medical history timeline view
8. ✅ Implement vitals tracking and charting
9. ✅ Enable record sharing between patients and doctors

---

## Prerequisites from Phase 3

Before starting Phase 4, ensure the following are complete:

| Prerequisite | Status |
|--------------|--------|
| Consultation Service deployed and functional | ⬜ |
| Video/Audio consultations working | ⬜ |
| Session management and tracking operational | ⬜ |
| Kafka event streaming operational | ⬜ |
| User authentication and authorization working | ⬜ |
| S3/Object storage configured | ⬜ |
| MongoDB cluster operational | ⬜ |

---

## Team Allocation for Phase 4

| Role | Name | Focus Area |
|------|------|------------|
| Tech Lead | _TBD_ | Architecture, security reviews, code reviews |
| Backend 1 | _TBD_ | Prescription Service, Medicine Database |
| Backend 2 | _TBD_ | Digital Signature, PDF Generation |
| Backend 3 | _TBD_ | EHR Service, Document Storage |
| Backend 4 | _TBD_ | Vitals Tracking, Timeline API |
| Frontend 1 | _TBD_ | Patient Web App - Health Records, Prescriptions |
| Frontend 2 | _TBD_ | Doctor Dashboard - Prescription Builder |
| DevOps | _TBD_ | S3 setup, security configurations |
| QA 1 | _TBD_ | Testing - Prescription flows |
| QA 2 | _TBD_ | Testing - EHR, document uploads |

---

## Architecture Overview

### Clinical Services Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CLINICAL FEATURES ARCHITECTURE                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                     PRESCRIPTION SERVICE                            │     │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │     │
│  │  │ Prescription│  │  Medicine   │  │   Digital   │  │    PDF    │  │     │
│  │  │   Manager   │  │  Database   │  │  Signature  │  │ Generator │  │     │
│  │  │  (WebFlux)  │  │  (Search)   │  │   Service   │  │           │  │     │
│  │  └──────┬──────┘  └──────┬──────┘  └─────────────┘  └───────────┘  │     │
│  │         │                │                                          │     │
│  │         ▼                ▼                                          │     │
│  │  ┌─────────────┐  ┌─────────────┐                                  │     │
│  │  │ PostgreSQL  │  │Elasticsearch│                                  │     │
│  │  │(Prescriptions│ │ (Medicines) │                                  │     │
│  │  └─────────────┘  └─────────────┘                                  │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                           │                                                  │
│                           │ prescription.created event                       │
│                           ▼                                                  │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                        EHR SERVICE                                  │     │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │     │
│  │  │   Health    │  │  Document   │  │   Vitals    │  │  Timeline │  │     │
│  │  │   Records   │  │   Storage   │  │  Tracking   │  │   Builder │  │     │
│  │  │   Manager   │  │    (S3)     │  │             │  │           │  │     │
│  │  └──────┬──────┘  └──────┬──────┘  └─────────────┘  └───────────┘  │     │
│  │         │                │                                          │     │
│  │         ▼                ▼                                          │     │
│  │  ┌─────────────┐  ┌─────────────┐                                  │     │
│  │  │  MongoDB    │  │  AWS S3     │                                  │     │
│  │  │  (Records)  │  │ (Documents) │                                  │     │
│  │  └─────────────┘  └─────────────┘                                  │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                           │                                                  │
│                           ▼                                                  │
│                    ┌─────────────┐                                          │
│                    │   Kafka     │                                          │
│                    │  (Events)   │                                          │
│                    └─────────────┘                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Technology Choices

| Component | Technology | Rationale |
|-----------|------------|-----------|
| **Prescription DB** | PostgreSQL + R2DBC | Structured data, relationships |
| **Medicine Search** | Elasticsearch | Fast autocomplete, fuzzy matching |
| **EHR Storage** | MongoDB | Flexible schema for varied record types |
| **Document Storage** | AWS S3 | Scalable, secure file storage |
| **PDF Generation** | iText/OpenPDF | Java-native PDF creation |
| **Digital Signature** | Java Crypto + X.509 | Standard, compliant signatures |

---

## Sprint Breakdown

### Sprint 8 (Week 15-16): Prescription Service

**Sprint Goal**: Doctors can create, sign, and share prescriptions. Patients can view and download prescriptions.

---

#### DevOps Tasks - Sprint 8

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| D8.1 | Medicine Database Setup | Import and index medicine master data in Elasticsearch | DevOps | 8 | P0 | Medicines searchable |
| D8.2 | S3 Bucket for Prescriptions | Create encrypted S3 bucket for prescription PDFs | DevOps | 4 | P0 | Bucket accessible, encrypted |
| D8.3 | Certificate for Signatures | Generate X.509 certificates for digital signatures | DevOps | 8 | P0 | Certificates in Vault |
| D8.4 | Prescription Service Deployment | K8s manifests, ConfigMaps, Secrets | DevOps | 8 | P0 | Service deployed to dev |

**DevOps Subtasks:**

<details>
<summary><strong>D8.1 - Medicine Database Setup (Detailed Steps)</strong></summary>

```markdown
## Subtasks for D8.1

1. [ ] Obtain medicine master data (FDA/regulatory database)
2. [ ] Create Elasticsearch index mapping for medicines
3. [ ] Configure index settings:
   - Shards: 3
   - Replicas: 2
   - Refresh interval: 30s
4. [ ] Create analyzers for autocomplete:
   - Edge n-gram for prefix matching
   - Synonym analyzer for drug names
5. [ ] Import medicine data using bulk API
6. [ ] Verify search functionality
7. [ ] Set up index lifecycle management
8. [ ] Document data refresh process
```

```json
// Elasticsearch index mapping
{
  "mappings": {
    "properties": {
      "medicineId": { "type": "keyword" },
      "brandName": { 
        "type": "text",
        "analyzer": "autocomplete",
        "search_analyzer": "standard"
      },
      "genericName": { 
        "type": "text",
        "analyzer": "autocomplete"
      },
      "manufacturer": { "type": "keyword" },
      "category": { "type": "keyword" },
      "formulation": { "type": "keyword" },
      "strength": { "type": "keyword" },
      "packSize": { "type": "integer" },
      "price": { "type": "float" },
      "requiresPrescription": { "type": "boolean" },
      "isAvailable": { "type": "boolean" },
      "suggest": {
        "type": "completion",
        "analyzer": "simple"
      }
    }
  },
  "settings": {
    "analysis": {
      "filter": {
        "autocomplete_filter": {
          "type": "edge_ngram",
          "min_gram": 2,
          "max_gram": 20
        }
      },
      "analyzer": {
        "autocomplete": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "autocomplete_filter"]
        }
      }
    }
  }
}
```
</details>

<details>
<summary><strong>D8.3 - Certificate for Digital Signatures (Detailed Steps)</strong></summary>

```markdown
## Subtasks for D8.3

1. [ ] Generate root CA certificate (for internal use)
2. [ ] Generate signing certificate for prescription service
3. [ ] Configure certificate chain
4. [ ] Store private keys securely in HashiCorp Vault
5. [ ] Set up certificate rotation policy
6. [ ] Document certificate management process

## Certificate Structure:
- Root CA (self-signed, 10-year validity)
  └── Intermediate CA (5-year validity)
      └── Prescription Signing Cert (1-year validity, auto-rotate)

## Vault Configuration:
vault kv put secret/prescription-service/signing-cert \
    private_key=@signing-key.pem \
    certificate=@signing-cert.pem \
    certificate_chain=@chain.pem
```
</details>

---

#### Backend Tasks - Prescription Service

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B13.1 | Create Prescription Service | Spring Boot 3.x + WebFlux project setup | Backend 1 | 4 | P0 | Service builds and runs |
| B13.2 | Prescription Database Schema | Design tables with Flyway migrations | Backend 1 | 8 | P0 | Migrations run successfully |
| B13.3 | Import Medicine Database | Load medicine master data, create repository | Backend 1 | 12 | P0 | Medicines queryable |
| B13.4 | Medicine Search API | Elasticsearch-based medicine search with autocomplete | Backend 1 | 12 | P0 | Fast autocomplete working |
| B13.5 | Prescription Entity & Repository | R2DBC models for prescriptions | Backend 1 | 8 | P0 | Repository tests pass |
| B13.6 | Create Prescription API | Endpoint to create prescription | Backend 1 | 12 | P0 | Prescription created |
| B13.7 | Prescription Templates | CRUD for reusable prescription templates | Backend 2 | 12 | P1 | Templates working |
| B13.8 | Digital Signature Service | Sign prescriptions with doctor's certificate | Backend 2 | 16 | P0 | Signature verifiable |
| B13.9 | PDF Generation Service | Generate prescription PDF with styling | Backend 2 | 16 | P0 | PDF generated correctly |
| B13.10 | Prescription Sharing API | Share prescription with pharmacy/patient | Backend 1 | 8 | P0 | Sharing works |
| B13.11 | Prescription Events | Publish to Kafka | Backend 1 | 4 | P0 | Events published |
| B13.12 | Get Prescriptions API | List/Get prescriptions for patient/doctor | Backend 1 | 8 | P0 | Prescriptions retrievable |
| B13.13 | Unit & Integration Tests | 80%+ coverage | Backend 1, 2 | 16 | P0 | Tests pass |

**Database Schema:**

<details>
<summary><strong>B13.2 - Prescription Database Schema</strong></summary>

```sql
-- V1__create_prescription_tables.sql

-- Prescription status enum
CREATE TYPE prescription_status AS ENUM (
    'DRAFT',           -- Doctor is still working on it
    'SIGNED',          -- Digitally signed, ready
    'DISPENSED',       -- Pharmacy has dispensed
    'PARTIALLY_DISPENSED',  -- Some items dispensed
    'EXPIRED',         -- Validity period ended
    'CANCELLED'        -- Cancelled by doctor
);

-- Main prescriptions table
CREATE TABLE prescriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- References
    consultation_id UUID,          -- Can be NULL for walk-in prescriptions
    appointment_id UUID,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    
    -- Prescription details
    prescription_number VARCHAR(50) UNIQUE NOT NULL,  -- Human-readable ID
    prescription_date DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_until DATE,              -- Prescription validity
    
    -- Clinical information
    diagnosis TEXT,
    chief_complaints TEXT,
    clinical_notes TEXT,
    
    -- Advice and follow-up
    general_advice TEXT,
    diet_advice TEXT,
    follow_up_date DATE,
    follow_up_notes TEXT,
    
    -- Lab tests recommended
    lab_tests_recommended TEXT[],
    
    -- Digital signature
    status prescription_status NOT NULL DEFAULT 'DRAFT',
    signed_at TIMESTAMP WITH TIME ZONE,
    signature_hash VARCHAR(256),    -- SHA-256 of signed content
    certificate_serial VARCHAR(100), -- Certificate used for signing
    
    -- PDF storage
    pdf_url VARCHAR(500),
    pdf_generated_at TIMESTAMP WITH TIME ZONE,
    
    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by UUID NOT NULL,       -- Usually same as doctor_id
    
    -- Template used (if any)
    template_id UUID
);

-- Indexes
CREATE INDEX idx_prescriptions_patient ON prescriptions(patient_id);
CREATE INDEX idx_prescriptions_doctor ON prescriptions(doctor_id);
CREATE INDEX idx_prescriptions_consultation ON prescriptions(consultation_id);
CREATE INDEX idx_prescriptions_date ON prescriptions(prescription_date);
CREATE INDEX idx_prescriptions_status ON prescriptions(status);
CREATE INDEX idx_prescriptions_number ON prescriptions(prescription_number);

-- Prescription items (medicines)
CREATE TABLE prescription_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    prescription_id UUID NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    
    -- Medicine reference
    medicine_id VARCHAR(50),        -- Reference to medicine database
    medicine_name VARCHAR(255) NOT NULL,
    generic_name VARCHAR(255),
    manufacturer VARCHAR(255),
    
    -- Dosage information
    strength VARCHAR(50),           -- e.g., "500mg", "10ml"
    formulation VARCHAR(50),        -- e.g., "Tablet", "Syrup", "Injection"
    
    -- Prescription details
    dosage VARCHAR(100),            -- e.g., "1 tablet", "5ml"
    frequency VARCHAR(100),         -- e.g., "Twice daily", "Every 8 hours"
    duration VARCHAR(100),          -- e.g., "7 days", "2 weeks"
    timing VARCHAR(100),            -- e.g., "After meals", "Before bed"
    route VARCHAR(50),              -- e.g., "Oral", "Topical", "IV"
    
    quantity INT,                   -- Total quantity prescribed
    quantity_unit VARCHAR(20),      -- e.g., "tablets", "ml", "units"
    
    -- Additional instructions
    special_instructions TEXT,
    
    -- Ordering
    sequence_order INT DEFAULT 0,
    
    -- Dispensing tracking
    is_dispensed BOOLEAN DEFAULT FALSE,
    dispensed_quantity INT DEFAULT 0,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_prescription_items_prescription ON prescription_items(prescription_id);

-- Prescription templates (for doctors to reuse)
CREATE TABLE prescription_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    
    template_name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Template conditions
    diagnosis VARCHAR(255),
    specialization VARCHAR(100),
    
    -- Template content (stored as JSON for flexibility)
    template_items JSONB NOT NULL,  -- Array of medicine items
    general_advice TEXT,
    diet_advice TEXT,
    
    -- Usage tracking
    usage_count INT DEFAULT 0,
    last_used_at TIMESTAMP WITH TIME ZONE,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    is_public BOOLEAN DEFAULT FALSE,  -- Shared with other doctors
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_template_name UNIQUE (doctor_id, template_name)
);

CREATE INDEX idx_templates_doctor ON prescription_templates(doctor_id);
CREATE INDEX idx_templates_diagnosis ON prescription_templates(diagnosis);

-- Audit trail for prescriptions
CREATE TABLE prescription_audit (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    prescription_id UUID NOT NULL REFERENCES prescriptions(id),
    
    action VARCHAR(50) NOT NULL,    -- CREATED, UPDATED, SIGNED, DISPENSED, etc.
    actor_id UUID NOT NULL,
    actor_type VARCHAR(20),         -- DOCTOR, PHARMACIST, SYSTEM
    
    previous_status prescription_status,
    new_status prescription_status,
    
    change_details JSONB,           -- What was changed
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_prescription_audit_prescription ON prescription_audit(prescription_id);
CREATE INDEX idx_prescription_audit_created ON prescription_audit(created_at);

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_prescription_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prescription_updated
    BEFORE UPDATE ON prescriptions
    FOR EACH ROW
    EXECUTE FUNCTION update_prescription_timestamp();
```
</details>

<details>
<summary><strong>B13.4 - Medicine Search API (Detailed)</strong></summary>

```java
// MedicineSearchService.java
@Service
@Slf4j
public class MedicineSearchService {
    
    private final ReactiveElasticsearchClient esClient;
    
    /**
     * Search medicines with autocomplete
     */
    public Flux<MedicineSearchResult> searchMedicines(String query, int limit) {
        return Mono.fromCallable(() -> buildAutocompleteQuery(query, limit))
            .flatMapMany(searchQuery -> 
                esClient.search(searchQuery, MedicineDocument.class)
            )
            .map(this::toSearchResult);
    }
    
    /**
     * Get medicine suggestions (completion suggester)
     */
    public Flux<String> getSuggestions(String prefix, int limit) {
        CompletionSuggester suggester = CompletionSuggester.of(cs -> cs
            .field("suggest")
            .prefix(prefix)
            .size(limit)
            .skipDuplicates(true)
            .fuzzy(f -> f.fuzziness("AUTO"))
        );
        
        return esClient.suggest(suggester, "medicines")
            .flatMapMany(response -> Flux.fromIterable(response.getSuggestions()));
    }
    
    /**
     * Search by generic name for alternatives
     */
    public Flux<MedicineSearchResult> findAlternatives(String genericName) {
        Query query = Query.of(q -> q
            .bool(b -> b
                .must(m -> m.match(mt -> mt
                    .field("genericName")
                    .query(genericName)))
                .filter(f -> f.term(t -> t
                    .field("isAvailable")
                    .value(true)))
            )
        );
        
        return esClient.search(query, MedicineDocument.class)
            .map(this::toSearchResult);
    }
    
    private Query buildAutocompleteQuery(String query, int limit) {
        return Query.of(q -> q
            .bool(b -> b
                .should(s -> s.match(m -> m
                    .field("brandName")
                    .query(query)
                    .boost(2.0f)))
                .should(s -> s.match(m -> m
                    .field("genericName")
                    .query(query)
                    .boost(1.5f)))
                .should(s -> s.match(m -> m
                    .field("brandName.autocomplete")
                    .query(query)))
                .minimumShouldMatch("1")
            )
        );
    }
}
```

```java
// MedicineDocument.java
@Document(indexName = "medicines")
@Value
@Builder
public class MedicineDocument {
    @Id
    String medicineId;
    String brandName;
    String genericName;
    String manufacturer;
    String category;           // e.g., "Antibiotics", "Analgesics"
    String formulation;        // e.g., "Tablet", "Syrup"
    String strength;           // e.g., "500mg"
    Integer packSize;
    BigDecimal price;
    Boolean requiresPrescription;
    Boolean isAvailable;
    List<String> alternativeIds;  // Generic alternatives
}
```
</details>

<details>
<summary><strong>B13.8 - Digital Signature Service (Detailed)</strong></summary>

```java
// DigitalSignatureService.java
@Service
@Slf4j
public class DigitalSignatureService {
    
    private final PrivateKey signingKey;
    private final X509Certificate signingCertificate;
    private final String certificateSerial;
    
    public DigitalSignatureService(
        @Value("${signing.key.path}") String keyPath,
        @Value("${signing.cert.path}") String certPath
    ) throws Exception {
        this.signingKey = loadPrivateKey(keyPath);
        this.signingCertificate = loadCertificate(certPath);
        this.certificateSerial = signingCertificate.getSerialNumber().toString();
    }
    
    /**
     * Sign prescription content and return signature hash
     */
    public Mono<SignatureResult> signPrescription(Prescription prescription) {
        return Mono.fromCallable(() -> {
            // Create canonical content for signing
            String contentToSign = createCanonicalContent(prescription);
            
            // Create signature
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(signingKey);
            signature.update(contentToSign.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();
            
            // Create hash for verification
            String signatureHash = Base64.getEncoder().encodeToString(signatureBytes);
            
            return SignatureResult.builder()
                .signatureHash(signatureHash)
                .certificateSerial(certificateSerial)
                .signedAt(Instant.now())
                .contentHash(hashContent(contentToSign))
                .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Verify prescription signature
     */
    public Mono<Boolean> verifySignature(Prescription prescription, String signatureHash) {
        return Mono.fromCallable(() -> {
            String contentToSign = createCanonicalContent(prescription);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureHash);
            
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(signingCertificate);
            signature.update(contentToSign.getBytes(StandardCharsets.UTF_8));
            
            return signature.verify(signatureBytes);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private String createCanonicalContent(Prescription prescription) {
        // Create deterministic string representation
        StringBuilder sb = new StringBuilder();
        sb.append("PRESCRIPTION|");
        sb.append(prescription.getId()).append("|");
        sb.append(prescription.getPrescriptionNumber()).append("|");
        sb.append(prescription.getPatientId()).append("|");
        sb.append(prescription.getDoctorId()).append("|");
        sb.append(prescription.getPrescriptionDate()).append("|");
        
        // Include medicine items in order
        prescription.getItems().stream()
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
}

@Value
@Builder
public class SignatureResult {
    String signatureHash;
    String certificateSerial;
    Instant signedAt;
    String contentHash;
}
```
</details>

<details>
<summary><strong>B13.9 - PDF Generation Service (Detailed)</strong></summary>

```java
// PrescriptionPdfService.java
@Service
@Slf4j
public class PrescriptionPdfService {
    
    private final S3Client s3Client;
    private final String bucketName;
    
    public Mono<String> generateAndStorePdf(
        Prescription prescription,
        DoctorProfile doctor,
        PatientInfo patient
    ) {
        return Mono.fromCallable(() -> {
            byte[] pdfBytes = generatePdf(prescription, doctor, patient);
            String key = buildS3Key(prescription);
            
            // Upload to S3
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/pdf")
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build(),
                RequestBody.fromBytes(pdfBytes)
            );
            
            return generatePresignedUrl(key);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private byte[] generatePdf(
        Prescription prescription,
        DoctorProfile doctor,
        PatientInfo patient
    ) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {
            
            // Header with clinic/hospital logo
            addHeader(document, doctor);
            
            // Doctor information
            addDoctorInfo(document, doctor);
            
            // Patient information
            addPatientInfo(document, patient, prescription);
            
            // Separator line
            addSeparator(document);
            
            // Diagnosis and complaints
            addDiagnosis(document, prescription);
            
            // Medicine table (Rx)
            addMedicinesTable(document, prescription.getItems());
            
            // Advice section
            addAdvice(document, prescription);
            
            // Lab tests recommended
            if (prescription.getLabTestsRecommended() != null) {
                addLabTests(document, prescription.getLabTestsRecommended());
            }
            
            // Follow-up information
            addFollowUp(document, prescription);
            
            // Digital signature section
            addSignature(document, doctor, prescription);
            
            // Footer
            addFooter(document, prescription);
        }
        
        return baos.toByteArray();
    }
    
    private void addMedicinesTable(Document document, List<PrescriptionItem> items) {
        // Create table with columns: #, Medicine, Dosage, Frequency, Duration, Instructions
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 30, 15, 15, 15, 20}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Header row
        table.addHeaderCell(createHeaderCell("#"));
        table.addHeaderCell(createHeaderCell("Medicine"));
        table.addHeaderCell(createHeaderCell("Dosage"));
        table.addHeaderCell(createHeaderCell("Frequency"));
        table.addHeaderCell(createHeaderCell("Duration"));
        table.addHeaderCell(createHeaderCell("Instructions"));
        
        // Medicine rows
        int index = 1;
        for (PrescriptionItem item : items) {
            table.addCell(createCell(String.valueOf(index++)));
            table.addCell(createCell(formatMedicineName(item)));
            table.addCell(createCell(item.getDosage()));
            table.addCell(createCell(item.getFrequency()));
            table.addCell(createCell(item.getDuration()));
            table.addCell(createCell(item.getSpecialInstructions() != null 
                ? item.getSpecialInstructions() : item.getTiming()));
        }
        
        document.add(table);
    }
    
    private String formatMedicineName(PrescriptionItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getMedicineName());
        if (item.getStrength() != null) {
            sb.append(" ").append(item.getStrength());
        }
        if (item.getFormulation() != null) {
            sb.append(" (").append(item.getFormulation()).append(")");
        }
        if (item.getGenericName() != null) {
            sb.append("\n").append(item.getGenericName());
        }
        return sb.toString();
    }
}
```

```
PDF Layout Example:
┌─────────────────────────────────────────────────────────────────┐
│  [LOGO]     HEALTHCARE CLINIC                                    │
│             123 Medical Street, City - 123456                    │
├─────────────────────────────────────────────────────────────────┤
│  Dr. Sarah Smith, MD, FACC          Reg. No: MCI-12345          │
│  Cardiologist                        Ph: +91-9876543210         │
├─────────────────────────────────────────────────────────────────┤
│  Patient: John Doe (M, 45 yrs)      Rx No: RX-2026-001234       │
│  Date: 30-Jan-2026                  Valid Until: 30-Apr-2026    │
├─────────────────────────────────────────────────────────────────┤
│  Diagnosis: Hypertension, Stage 1                               │
│  Chief Complaints: Headache, Dizziness                          │
├─────────────────────────────────────────────────────────────────┤
│  ℞                                                               │
│  ┌───┬────────────────┬─────────┬───────────┬────────┬────────┐ │
│  │ # │ Medicine       │ Dosage  │ Frequency │Duration│ Notes  │ │
│  ├───┼────────────────┼─────────┼───────────┼────────┼────────┤ │
│  │ 1 │ Amlodipine 5mg │ 1 tab   │ Once daily│ 30 days│ Morning│ │
│  │ 2 │ Aspirin 75mg   │ 1 tab   │ Once daily│ 30 days│ After  │ │
│  │   │                │         │           │        │ lunch  │ │
│  └───┴────────────────┴─────────┴───────────┴────────┴────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  Advice:                                                         │
│  • Reduce salt intake                                            │
│  • Regular exercise (30 min walk daily)                         │
│  • Monitor BP daily                                              │
├─────────────────────────────────────────────────────────────────┤
│  Lab Tests Recommended:                                          │
│  • Complete Blood Count (CBC)                                    │
│  • Lipid Profile                                                 │
├─────────────────────────────────────────────────────────────────┤
│  Follow-up: 30-Feb-2026                                         │
├─────────────────────────────────────────────────────────────────┤
│                                          [Digital Signature]     │
│                                          Dr. Sarah Smith         │
│                                          Signed: 30-Jan-2026     │
│  ─────────────────────────────────────────────────────────────  │
│  This is a digitally signed prescription.                       │
│  Verify at: https://healthapp.com/verify/RX-2026-001234         │
└─────────────────────────────────────────────────────────────────┘
```
</details>

---

#### Frontend Tasks - Sprint 8

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F8.1 | Prescription Builder UI | Full prescription creation interface | Frontend 2 | 32 | P0 | Can create complete prescription |
| F8.2 | Medicine Search Autocomplete | Fast medicine search with suggestions | Frontend 2 | 12 | P0 | Autocomplete works smoothly |
| F8.3 | Dosage/Frequency Selector | Easy selection of common dosages | Frontend 2 | 8 | P0 | Selectors work correctly |
| F8.4 | Prescription Preview | Preview before signing | Frontend 2 | 12 | P0 | Preview matches PDF |
| F8.5 | Template Selector | Choose and apply templates | Frontend 2 | 8 | P1 | Templates apply correctly |
| F8.6 | View Prescription (Patient) | Patient view of prescription | Frontend 1 | 12 | P0 | Prescription readable |
| F8.7 | Download Prescription PDF | Download signed PDF | Frontend 1 | 6 | P0 | PDF downloads |
| F8.8 | Prescriptions List (Patient) | List all prescriptions | Frontend 1 | 8 | P0 | List displays correctly |
| F8.9 | Sign Prescription Flow | Doctor signing workflow | Frontend 2 | 8 | P0 | Signing works |

**Frontend Component Details:**

<details>
<summary><strong>F8.1 - Prescription Builder UI (Detailed)</strong></summary>

```markdown
## Prescription Builder Component Structure

components/
├── prescription/
│   ├── PrescriptionBuilder/
│   │   ├── index.tsx                 # Main builder container
│   │   ├── PrescriptionBuilder.styles.ts
│   │   ├── PatientHeader.tsx         # Patient info display
│   │   ├── DiagnosisSection.tsx      # Diagnosis input
│   │   ├── MedicineList.tsx          # List of added medicines
│   │   ├── MedicineItem.tsx          # Single medicine row
│   │   ├── AddMedicineModal.tsx      # Add new medicine
│   │   ├── MedicineSearch.tsx        # Autocomplete search
│   │   ├── DosageSelector.tsx        # Dosage input
│   │   ├── FrequencySelector.tsx     # Frequency dropdown
│   │   ├── DurationSelector.tsx      # Duration input
│   │   ├── AdviceSection.tsx         # General advice
│   │   ├── LabTestsSection.tsx       # Recommended tests
│   │   ├── FollowUpSection.tsx       # Follow-up date
│   │   ├── TemplateSelector.tsx      # Apply template
│   │   ├── PreviewModal.tsx          # Preview prescription
│   │   ├── SignatureModal.tsx        # Signing confirmation
│   │   └── hooks/
│   │       ├── usePrescriptionBuilder.ts
│   │       ├── useMedicineSearch.ts
│   │       └── useTemplates.ts
│   └── PrescriptionView/
│       ├── index.tsx
│       └── PrescriptionCard.tsx

## Layout Structure

┌─────────────────────────────────────────────────────────────────────────┐
│  📋 New Prescription                               [Template ▼] [Preview]│
├─────────────────────────────────────────────────────────────────────────┤
│  Patient: John Doe | Age: 45 | Gender: Male | Appt: 30-Jan-2026        │
├─────────────────────────────────────────────────────────────────────────┤
│  Diagnosis *                                                            │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ Hypertension, Stage 1                                              │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  Chief Complaints                                                       │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ Headache, Dizziness for past 2 weeks                              │ │
│  └───────────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────────────┤
│  ℞ Medicines                                              [+ Add Medicine]│
│  ┌─────────────────────────────────────────────────────────────────────┐│
│  │ 1. Amlodipine 5mg (Tablet)                                    [🗑️] ││
│  │    └─ 1 tablet | Once daily | 30 days | Morning                    ││
│  ├─────────────────────────────────────────────────────────────────────┤│
│  │ 2. Aspirin 75mg (Tablet)                                      [🗑️] ││
│  │    └─ 1 tablet | Once daily | 30 days | After lunch                ││
│  └─────────────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────────────┤
│  Advice                                                                 │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ • Reduce salt intake                                               │ │
│  │ • Regular exercise                                                 │ │
│  └───────────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────────────┤
│  Lab Tests Recommended                           [+ Add Test]           │
│  [CBC] [Lipid Profile] [×]                                             │
├─────────────────────────────────────────────────────────────────────────┤
│  Follow-up Date: [📅 30-Feb-2026]                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                               [Save Draft]  [Preview & Sign]            │
└─────────────────────────────────────────────────────────────────────────┘
```
</details>

<details>
<summary><strong>F8.2 - Medicine Search Autocomplete (Detailed)</strong></summary>

```typescript
// MedicineSearch.tsx

interface MedicineSearchProps {
    onSelect: (medicine: Medicine) => void;
    placeholder?: string;
}

// Features:
// 1. Debounced search (300ms)
// 2. Minimum 2 characters to search
// 3. Show brand name + generic name
// 4. Show strength and formulation
// 5. Highlight matching text
// 6. Show "alternatives" for generic drugs
// 7. Recently prescribed (for this doctor)

// Search result item display:
// ┌─────────────────────────────────────────────────┐
// │ 🔵 Amlodipine 5mg Tablet                        │
// │    Generic: Amlodipine Besylate                 │
// │    Manufacturer: Cipla | ₹45 for 10 tablets    │
// ├─────────────────────────────────────────────────┤
// │ 🔵 Amlodac 5mg Tablet                           │
// │    Generic: Amlodipine Besylate                 │
// │    Manufacturer: Zydus | ₹38 for 10 tablets    │
// └─────────────────────────────────────────────────┘

const useMedicineSearch = () => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState<Medicine[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    
    const debouncedQuery = useDebounce(query, 300);
    
    useEffect(() => {
        if (debouncedQuery.length >= 2) {
            setIsLoading(true);
            searchMedicines(debouncedQuery)
                .then(setResults)
                .finally(() => setIsLoading(false));
        } else {
            setResults([]);
        }
    }, [debouncedQuery]);
    
    return { query, setQuery, results, isLoading };
};
```
</details>

---

### Sprint 9 (Week 17-18): EHR Service (Electronic Health Records)

**Sprint Goal**: Patients can store and manage health records. Doctors can view patient history. Vitals tracking operational.

---

#### DevOps Tasks - Sprint 9

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| D9.1 | EHR MongoDB Database | Configure MongoDB collection with indexes | DevOps | 4 | P0 | Database accessible |
| D9.2 | S3 Bucket for Documents | Create encrypted bucket for health documents | DevOps | 4 | P0 | Bucket configured |
| D9.3 | Document Virus Scanning | Set up ClamAV for uploaded files | DevOps | 8 | P0 | Scanning operational |
| D9.4 | EHR Service Deployment | K8s manifests and configurations | DevOps | 8 | P0 | Service deployed |
| D9.5 | CDN for Documents | Configure CloudFront for fast document access | DevOps | 4 | P1 | CDN operational |

---

#### Backend Tasks - EHR Service

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B14.1 | Create EHR Service | Spring Boot 3.x + WebFlux project setup | Backend 3 | 4 | P0 | Service builds and runs |
| B14.2 | Health Records Schema | Design MongoDB schema for records | Backend 3 | 8 | P0 | Schema documented |
| B14.3 | HealthRecord Entity & Repository | MongoDB reactive repository | Backend 3 | 8 | P0 | Repository tests pass |
| B14.4 | Create Health Record API | Create new health record | Backend 3 | 8 | P0 | Records created |
| B14.5 | Document Upload Service | Upload documents to S3 with scanning | Backend 3 | 16 | P0 | Upload works, virus scan runs |
| B14.6 | Get Patient Records API | Retrieve patient's health records | Backend 3 | 8 | P0 | Records retrieved |
| B14.7 | Record Sharing API | Share records with doctors | Backend 3 | 12 | P0 | Sharing works |
| B14.8 | Vitals Tracking API | CRUD for vital signs | Backend 4 | 12 | P1 | Vitals tracked |
| B14.9 | Prescription Event Handler | Auto-add prescriptions to EHR | Backend 3 | 8 | P0 | Prescriptions in EHR |
| B14.10 | Medical Timeline API | Aggregate timeline view | Backend 4 | 16 | P1 | Timeline works |
| B14.11 | Record Categories & Tags | Organize records with categories | Backend 3 | 8 | P0 | Categories work |
| B14.12 | Unit & Integration Tests | 80%+ coverage | Backend 3, 4 | 16 | P0 | Tests pass |

**Database Schema:**

<details>
<summary><strong>B14.2 - Health Records MongoDB Schema</strong></summary>

```javascript
// MongoDB Collection: health_records

// Document Schema
{
    "_id": ObjectId,
    "patientId": "uuid-string",
    "recordId": "uuid-string",           // External reference ID
    
    // Record type classification
    "recordType": "CONSULTATION" | "LAB_REPORT" | "PRESCRIPTION" | 
                  "IMAGING" | "VACCINATION" | "VITALS" | 
                  "DISCHARGE_SUMMARY" | "DOCTOR_NOTE" | "OTHER",
    
    "category": "Cardiology" | "Orthopedics" | "General",
    "subcategory": "ECG" | "X-Ray" | "Blood Test",
    
    // Basic information
    "title": "Annual Health Checkup Report",
    "description": "Complete body checkup including blood tests",
    
    // Source information
    "source": {
        "type": "UPLOADED" | "CONSULTATION" | "LAB" | "PHARMACY" | "HOSPITAL",
        "sourceId": "consultation-uuid",   // Reference to source
        "sourceName": "Apollo Hospitals",
        "uploadedBy": "PATIENT" | "DOCTOR" | "LAB" | "SYSTEM"
    },
    
    // Associated doctor (if applicable)
    "doctorInfo": {
        "doctorId": "uuid-string",
        "doctorName": "Dr. Sarah Smith",
        "specialization": "Cardiology"
    },
    
    // Record date (when the medical event occurred)
    "recordDate": ISODate("2026-01-15"),
    
    // Documents attached
    "documents": [
        {
            "documentId": "uuid-string",
            "fileName": "blood_report.pdf",
            "fileType": "application/pdf",
            "fileSize": 1024000,
            "s3Key": "health-records/patient-123/doc-456.pdf",
            "thumbnailKey": "thumbnails/doc-456-thumb.jpg",
            "uploadedAt": ISODate,
            "virusScanStatus": "CLEAN" | "INFECTED" | "PENDING",
            "virusScannedAt": ISODate
        }
    ],
    
    // Structured data (varies by record type)
    "structuredData": {
        // For LAB_REPORT
        "labTests": [
            {
                "testName": "Hemoglobin",
                "value": "14.5",
                "unit": "g/dL",
                "referenceRange": "12-16",
                "status": "NORMAL" | "HIGH" | "LOW" | "CRITICAL"
            }
        ],
        
        // For VITALS
        "vitals": {
            "bloodPressureSystolic": 120,
            "bloodPressureDiastolic": 80,
            "heartRate": 72,
            "temperature": 98.6,
            "weight": 70,
            "height": 175,
            "bmi": 22.9
        },
        
        // For PRESCRIPTION - reference only
        "prescriptionId": "rx-uuid",
        
        // For IMAGING
        "imagingType": "X-Ray" | "MRI" | "CT" | "Ultrasound",
        "bodyPart": "Chest",
        "findings": "No abnormalities detected"
    },
    
    // Tags for search
    "tags": ["routine-checkup", "blood-test", "2026"],
    
    // Sharing settings
    "sharing": {
        "isPublic": false,           // Visible to all doctors
        "sharedWith": [              // Specific doctors
            {
                "doctorId": "uuid-string",
                "sharedAt": ISODate,
                "expiresAt": ISODate,      // Optional expiry
                "accessLevel": "VIEW" | "DOWNLOAD"
            }
        ]
    },
    
    // Notes
    "notes": "Important: Patient allergic to penicillin",
    
    // Metadata
    "createdAt": ISODate,
    "updatedAt": ISODate,
    "createdBy": "uuid-string",
    "isDeleted": false,
    "deletedAt": ISODate
}

// Indexes
db.health_records.createIndex({ "patientId": 1, "recordDate": -1 });
db.health_records.createIndex({ "patientId": 1, "recordType": 1 });
db.health_records.createIndex({ "patientId": 1, "category": 1 });
db.health_records.createIndex({ "sharing.sharedWith.doctorId": 1 });
db.health_records.createIndex({ "tags": 1 });
db.health_records.createIndex({ 
    "title": "text", 
    "description": "text", 
    "tags": "text" 
});
```

```javascript
// MongoDB Collection: vitals_history

{
    "_id": ObjectId,
    "patientId": "uuid-string",
    
    "recordedAt": ISODate,
    "recordedBy": "PATIENT" | "DOCTOR" | "DEVICE",
    "deviceId": "fitbit-123",           // If from device
    
    "vitals": {
        "bloodPressure": {
            "systolic": 120,
            "diastolic": 80,
            "unit": "mmHg"
        },
        "heartRate": {
            "value": 72,
            "unit": "bpm"
        },
        "temperature": {
            "value": 98.6,
            "unit": "F"
        },
        "respiratoryRate": {
            "value": 16,
            "unit": "breaths/min"
        },
        "oxygenSaturation": {
            "value": 98,
            "unit": "%"
        },
        "weight": {
            "value": 70,
            "unit": "kg"
        },
        "height": {
            "value": 175,
            "unit": "cm"
        },
        "bloodGlucose": {
            "value": 95,
            "unit": "mg/dL",
            "type": "FASTING" | "POST_MEAL" | "RANDOM"
        }
    },
    
    "notes": "Measured after morning walk",
    "createdAt": ISODate
}

// Index for time-series queries
db.vitals_history.createIndex({ "patientId": 1, "recordedAt": -1 });
```
</details>

<details>
<summary><strong>B14.5 - Document Upload Service (Detailed)</strong></summary>

```java
// DocumentUploadService.java
@Service
@Slf4j
public class DocumentUploadService {
    
    private final S3AsyncClient s3Client;
    private final VirusScanService virusScanService;
    private final ThumbnailService thumbnailService;
    
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "application/pdf",
        "image/jpeg",
        "image/png",
        "image/gif",
        "application/dicom"  // Medical imaging
    );
    
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    public Mono<UploadResult> uploadDocument(
        String patientId,
        FilePart filePart,
        DocumentMetadata metadata
    ) {
        return validateFile(filePart)
            .then(scanForVirus(filePart))
            .flatMap(scanResult -> {
                if (scanResult.isInfected()) {
                    return Mono.error(new VirusDetectedException(scanResult.getThreat()));
                }
                return uploadToS3(patientId, filePart, metadata);
            })
            .flatMap(s3Key -> generateThumbnail(s3Key, filePart.headers().getContentType()))
            .map(this::buildUploadResult);
    }
    
    private Mono<Void> validateFile(FilePart filePart) {
        String contentType = filePart.headers().getContentType().toString();
        
        if (!ALLOWED_TYPES.contains(contentType)) {
            return Mono.error(new InvalidFileTypeException(
                "File type not allowed: " + contentType));
        }
        
        // Size validation happens at controller level with @RequestPart
        return Mono.empty();
    }
    
    private Mono<ScanResult> scanForVirus(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                return virusScanService.scan(bytes);
            });
    }
    
    private Mono<String> uploadToS3(
        String patientId,
        FilePart filePart,
        DocumentMetadata metadata
    ) {
        String key = buildS3Key(patientId, metadata);
        
        return DataBufferUtils.join(filePart.content())
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                
                return Mono.fromFuture(
                    s3Client.putObject(
                        PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(filePart.headers().getContentType().toString())
                            .serverSideEncryption(ServerSideEncryption.AES256)
                            .metadata(Map.of(
                                "patient-id", patientId,
                                "original-name", filePart.filename(),
                                "upload-time", Instant.now().toString()
                            ))
                            .build(),
                        AsyncRequestBody.fromBytes(bytes)
                    )
                ).thenReturn(key);
            });
    }
    
    private String buildS3Key(String patientId, DocumentMetadata metadata) {
        return String.format(
            "health-records/%s/%s/%s-%s",
            patientId,
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")),
            UUID.randomUUID(),
            sanitizeFilename(metadata.getFileName())
        );
    }
    
    public Mono<String> generatePresignedUrl(String s3Key, Duration expiration) {
        return Mono.fromCallable(() -> {
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build())
                    .build()
            );
            return presignedRequest.url().toString();
        });
    }
}
```
</details>

<details>
<summary><strong>B14.7 - Record Sharing API (Detailed)</strong></summary>

```java
// RecordSharingService.java
@Service
public class RecordSharingService {
    
    private final HealthRecordRepository recordRepository;
    private final NotificationPublisher notificationPublisher;
    
    /**
     * Share a record with a specific doctor
     */
    public Mono<ShareResult> shareWithDoctor(
        String recordId,
        String patientId,
        ShareRequest request
    ) {
        return recordRepository.findByIdAndPatientId(recordId, patientId)
            .switchIfEmpty(Mono.error(new RecordNotFoundException(recordId)))
            .flatMap(record -> {
                SharingEntry entry = SharingEntry.builder()
                    .doctorId(request.getDoctorId())
                    .sharedAt(Instant.now())
                    .expiresAt(request.getExpiresAt())
                    .accessLevel(request.getAccessLevel())
                    .build();
                
                record.getSharing().getSharedWith().add(entry);
                return recordRepository.save(record);
            })
            .flatMap(record -> notifyDoctor(record, request.getDoctorId()))
            .map(record -> ShareResult.success(record.getId()));
    }
    
    /**
     * Revoke sharing access
     */
    public Mono<Void> revokeAccess(
        String recordId,
        String patientId,
        String doctorId
    ) {
        return recordRepository.findByIdAndPatientId(recordId, patientId)
            .flatMap(record -> {
                record.getSharing().getSharedWith()
                    .removeIf(entry -> entry.getDoctorId().equals(doctorId));
                return recordRepository.save(record);
            })
            .then();
    }
    
    /**
     * Share all records with a doctor (for upcoming consultation)
     */
    public Mono<Void> shareAllForConsultation(
        String patientId,
        String doctorId,
        String consultationId,
        Duration duration
    ) {
        Instant expiresAt = Instant.now().plus(duration);
        
        return recordRepository.findByPatientId(patientId)
            .flatMap(record -> {
                SharingEntry entry = SharingEntry.builder()
                    .doctorId(doctorId)
                    .consultationId(consultationId)
                    .sharedAt(Instant.now())
                    .expiresAt(expiresAt)
                    .accessLevel(AccessLevel.VIEW)
                    .build();
                
                record.getSharing().getSharedWith().add(entry);
                return recordRepository.save(record);
            })
            .then();
    }
    
    /**
     * Check if doctor has access to a record
     */
    public Mono<Boolean> hasAccess(String recordId, String doctorId) {
        return recordRepository.findById(recordId)
            .map(record -> {
                if (record.getSharing().isPublic()) {
                    return true;
                }
                return record.getSharing().getSharedWith().stream()
                    .anyMatch(entry -> 
                        entry.getDoctorId().equals(doctorId) &&
                        (entry.getExpiresAt() == null || 
                         entry.getExpiresAt().isAfter(Instant.now()))
                    );
            })
            .defaultIfEmpty(false);
    }
}

@Value
@Builder
public class ShareRequest {
    String doctorId;
    Instant expiresAt;          // Optional expiry
    AccessLevel accessLevel;    // VIEW or DOWNLOAD
    String reason;              // Why sharing
}

public enum AccessLevel {
    VIEW,       // Can view in app
    DOWNLOAD    // Can download files
}
```
</details>

<details>
<summary><strong>B14.10 - Medical Timeline API (Detailed)</strong></summary>

```java
// MedicalTimelineService.java
@Service
public class MedicalTimelineService {
    
    private final HealthRecordRepository recordRepository;
    private final VitalsRepository vitalsRepository;
    private final PrescriptionClient prescriptionClient;
    private final AppointmentClient appointmentClient;
    
    /**
     * Build comprehensive medical timeline for a patient
     */
    public Flux<TimelineEntry> getTimeline(
        String patientId,
        TimelineRequest request
    ) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        
        // Gather data from multiple sources
        Flux<TimelineEntry> records = getHealthRecordEntries(patientId, fromDate, toDate);
        Flux<TimelineEntry> vitals = getVitalsEntries(patientId, fromDate, toDate);
        Flux<TimelineEntry> prescriptions = getPrescriptionEntries(patientId, fromDate, toDate);
        Flux<TimelineEntry> appointments = getAppointmentEntries(patientId, fromDate, toDate);
        
        // Merge and sort by date (descending)
        return Flux.merge(records, vitals, prescriptions, appointments)
            .filter(entry -> matchesFilters(entry, request.getFilters()))
            .sort(Comparator.comparing(TimelineEntry::getEventDate).reversed())
            .take(request.getLimit());
    }
    
    private Flux<TimelineEntry> getHealthRecordEntries(
        String patientId, LocalDate from, LocalDate to
    ) {
        return recordRepository.findByPatientIdAndDateRange(patientId, from, to)
            .map(record -> TimelineEntry.builder()
                .id(record.getId())
                .eventDate(record.getRecordDate())
                .entryType(TimelineEntryType.RECORD)
                .recordType(record.getRecordType())
                .title(record.getTitle())
                .description(record.getDescription())
                .category(record.getCategory())
                .doctorName(record.getDoctorInfo() != null ? 
                    record.getDoctorInfo().getDoctorName() : null)
                .hasDocuments(!record.getDocuments().isEmpty())
                .documentCount(record.getDocuments().size())
                .metadata(Map.of("recordId", record.getId()))
                .build());
    }
    
    private Flux<TimelineEntry> getVitalsEntries(
        String patientId, LocalDate from, LocalDate to
    ) {
        return vitalsRepository.findByPatientIdAndDateRange(patientId, from, to)
            .map(vitals -> TimelineEntry.builder()
                .id(vitals.getId())
                .eventDate(vitals.getRecordedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                .entryType(TimelineEntryType.VITALS)
                .title("Vitals Recorded")
                .description(formatVitalsSummary(vitals))
                .category("Vitals")
                .metadata(Map.of(
                    "bp", vitals.getVitals().getBloodPressure(),
                    "heartRate", vitals.getVitals().getHeartRate()
                ))
                .build());
    }
}

@Value
@Builder
public class TimelineEntry {
    String id;
    LocalDate eventDate;
    LocalTime eventTime;
    TimelineEntryType entryType;     // RECORD, VITALS, PRESCRIPTION, APPOINTMENT
    String recordType;                // Specific type within category
    String title;
    String description;
    String category;
    String doctorName;
    String doctorSpecialization;
    Boolean hasDocuments;
    Integer documentCount;
    Map<String, Object> metadata;
}

public enum TimelineEntryType {
    RECORD,
    VITALS,
    PRESCRIPTION,
    APPOINTMENT,
    CONSULTATION,
    LAB_RESULT,
    VACCINATION
}
```

```
Timeline UI Representation:

┌─────────────────────────────────────────────────────────────────────────┐
│  📅 Medical Timeline                    [Filter: All ▼] [Date Range 📅] │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ● January 2026                                                          │
│  │                                                                       │
│  ├─ 30 Jan ─ 💊 Prescription                                            │
│  │           Dr. Sarah Smith (Cardiology)                                │
│  │           Amlodipine 5mg, Aspirin 75mg                               │
│  │           [View Prescription]                                         │
│  │                                                                       │
│  ├─ 30 Jan ─ 📹 Video Consultation                                      │
│  │           Dr. Sarah Smith (Cardiology)                                │
│  │           Duration: 15 minutes                                        │
│  │           [View Summary]                                              │
│  │                                                                       │
│  ├─ 28 Jan ─ 🔬 Lab Report                                              │
│  │           Complete Blood Count                                        │
│  │           Apollo Diagnostics                                          │
│  │           [View Report] [📄 1 document]                              │
│  │                                                                       │
│  ├─ 25 Jan ─ ❤️ Vitals                                                  │
│  │           BP: 130/85 | HR: 78 | Weight: 72kg                         │
│  │                                                                       │
│  ● December 2025                                                         │
│  │                                                                       │
│  ├─ 15 Dec ─ 🏥 Hospital Visit                                          │
│  │           Annual Health Checkup                                       │
│  │           Apollo Hospitals                                            │
│  │           [View Records] [📄 3 documents]                            │
│  │                                                                       │
└─────────────────────────────────────────────────────────────────────────┘
```
</details>

---

#### Frontend Tasks - Sprint 9

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F9.1 | Health Records Page | Main health records listing | Frontend 1 | 20 | P0 | Records displayed |
| F9.2 | Document Upload Component | Upload documents with progress | Frontend 1 | 16 | P0 | Upload works |
| F9.3 | Record Viewer | View PDFs and images | Frontend 1 | 12 | P0 | Viewer works |
| F9.4 | Medical Timeline View | Chronological timeline | Frontend 1 | 20 | P1 | Timeline displays |
| F9.5 | Vitals Entry Form | Add/edit vital signs | Frontend 1 | 12 | P1 | Vitals saved |
| F9.6 | Vitals Charts/Graphs | Visualize vitals history | Frontend 1 | 16 | P1 | Charts display |
| F9.7 | Patient Records View (Doctor) | Doctor's view of patient history | Frontend 2 | 16 | P0 | Records visible |
| F9.8 | Record Sharing Controls | Share/revoke access UI | Frontend 1 | 12 | P0 | Sharing works |
| F9.9 | Record Categories & Filters | Filter and organize records | Frontend 1 | 8 | P0 | Filters work |

**Frontend Component Details:**

<details>
<summary><strong>F9.1 - Health Records Page (Detailed)</strong></summary>

```markdown
## Health Records Page Structure

pages/
├── health-records/
│   ├── index.tsx                 # Main records page
│   ├── [recordId].tsx            # Single record detail
│   └── upload.tsx                # Upload new record

components/
├── health-records/
│   ├── RecordsList/
│   │   ├── index.tsx
│   │   ├── RecordCard.tsx
│   │   ├── RecordFilters.tsx
│   │   └── RecordSearch.tsx
│   ├── RecordDetail/
│   │   ├── index.tsx
│   │   ├── DocumentViewer.tsx
│   │   ├── SharingPanel.tsx
│   │   └── RecordMetadata.tsx
│   ├── Upload/
│   │   ├── UploadModal.tsx
│   │   ├── FileDropzone.tsx
│   │   ├── UploadProgress.tsx
│   │   └── RecordForm.tsx
│   └── Timeline/
│       ├── Timeline.tsx
│       ├── TimelineEntry.tsx
│       └── TimelineFilters.tsx

## Page Layout

┌─────────────────────────────────────────────────────────────────────────┐
│  🏥 My Health Records                        [+ Upload Record] [Timeline]│
├─────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ 🔍 Search records...                                             │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  Filters: [All Types ▼] [All Categories ▼] [Date: Last 6 months ▼]     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ 📋 Prescription                                    30 Jan 2026   │   │
│  │    Dr. Sarah Smith - Cardiology                                  │   │
│  │    Hypertension medication                                       │   │
│  │    [View] [Download PDF] [Share]                                 │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ 🔬 Lab Report                                      28 Jan 2026   │   │
│  │    Complete Blood Count                                          │   │
│  │    Apollo Diagnostics | 📄 1 document                            │   │
│  │    [View] [Download] [Share]                                     │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ 🏥 Hospital Record                                 15 Dec 2025   │   │
│  │    Annual Health Checkup                                         │   │
│  │    Apollo Hospitals | 📄 3 documents                             │   │
│  │    [View] [Download] [Share]                                     │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  [Load More...]                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```
</details>

<details>
<summary><strong>F9.6 - Vitals Charts/Graphs (Detailed)</strong></summary>

```typescript
// VitalsChart.tsx

interface VitalsChartProps {
    patientId: string;
    vitalType: 'bloodPressure' | 'heartRate' | 'weight' | 'glucose';
    dateRange: DateRange;
}

// Chart types:
// 1. Blood Pressure - Dual line chart (systolic/diastolic)
// 2. Heart Rate - Line chart with normal range shading
// 3. Weight - Line chart with trend line
// 4. Blood Glucose - Scatter plot with meal markers

// Features:
// - Zoom and pan
// - Tooltip with exact values
// - Normal range highlighting
// - Trend indicators
// - Export to image

// Libraries: Recharts or Chart.js

// Example Chart Layout:
/*
┌─────────────────────────────────────────────────────────────────────────┐
│  Blood Pressure History                              [1W] [1M] [3M] [1Y]│
├─────────────────────────────────────────────────────────────────────────┤
│  mmHg                                                                    │
│  150 ─┼─────────────────────────────────────────────────────────────────│
│       │                    ╭─╮                                           │
│  140 ─┼────────────────────╯ ╰─╮        ╭─────╮                         │
│       │    ╭───────────╮       ╰────────╯     ╰──── Systolic            │
│  130 ─┼────╯           ╰─────────────────────────────────────────────────│
│       │══════════════════════════════════════════════ Normal Range ═════│
│  120 ─┼─────────────────────────────────────────────────────────────────│
│       │                                                                  │
│  90  ─┼────────╮    ╭────────────────╮                                  │
│       │        ╰────╯                ╰───────────────── Diastolic       │
│  80  ─┼─────────────────────────────────────────────────────────────────│
│       │                                                                  │
│  70  ─┼─────────────────────────────────────────────────────────────────│
│       └─────────┬──────────┬──────────┬──────────┬──────────┬───────────│
│               Jan 5      Jan 12     Jan 19     Jan 26     Feb 2         │
└─────────────────────────────────────────────────────────────────────────┘
│  Latest: 128/82 mmHg (Jan 30) | Avg: 132/85 mmHg | Trend: ↓ Improving  │
└─────────────────────────────────────────────────────────────────────────┘
*/

const VitalsChart: React.FC<VitalsChartProps> = ({ 
    patientId, 
    vitalType, 
    dateRange 
}) => {
    const { data, isLoading } = useVitalsHistory(patientId, vitalType, dateRange);
    
    if (vitalType === 'bloodPressure') {
        return (
            <ResponsiveContainer width="100%" height={300}>
                <LineChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis domain={[60, 160]} />
                    <Tooltip />
                    <Legend />
                    <ReferenceLine y={120} stroke="green" strokeDasharray="5 5" />
                    <ReferenceLine y={80} stroke="green" strokeDasharray="5 5" />
                    <Line 
                        type="monotone" 
                        dataKey="systolic" 
                        stroke="#ef4444" 
                        name="Systolic"
                    />
                    <Line 
                        type="monotone" 
                        dataKey="diastolic" 
                        stroke="#3b82f6" 
                        name="Diastolic"
                    />
                </LineChart>
            </ResponsiveContainer>
        );
    }
    // ... other vital types
};
```
</details>

---

## Kafka Events

### Events Published by Prescription Service

| Event Type | Trigger | Payload | Consumers |
|------------|---------|---------|-----------|
| `prescription.created` | Prescription saved as draft | prescriptionId, doctorId, patientId | - |
| `prescription.signed` | Doctor signs prescription | prescriptionId, pdfUrl, items | EHR Service, Notification |
| `prescription.shared` | Shared with pharmacy | prescriptionId, pharmacyId | Order Service |
| `prescription.dispensed` | Pharmacy dispenses | prescriptionId, dispensedItems | EHR Service |

### Events Consumed by EHR Service

| Event Type | Source | Action |
|------------|--------|--------|
| `prescription.signed` | Prescription Service | Add to health records |
| `consultation.completed` | Consultation Service | Create consultation record |
| `lab.report.uploaded` | Lab Service | Add lab report to records |
| `appointment.completed` | Appointment Service | Add appointment summary |

---

## API Endpoints

### Prescription Service APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/prescriptions` | Create prescription | Doctor |
| GET | `/api/v1/prescriptions/{id}` | Get prescription | Patient/Doctor |
| PUT | `/api/v1/prescriptions/{id}` | Update draft | Doctor |
| POST | `/api/v1/prescriptions/{id}/sign` | Sign prescription | Doctor |
| GET | `/api/v1/prescriptions/{id}/pdf` | Download PDF | Patient/Doctor |
| POST | `/api/v1/prescriptions/{id}/share` | Share with pharmacy | Patient |
| GET | `/api/v1/prescriptions/patient/{patientId}` | Patient's prescriptions | Patient/Doctor |
| GET | `/api/v1/prescriptions/doctor/me` | Doctor's prescriptions | Doctor |
| GET | `/api/v1/medicines/search` | Search medicines | Doctor |
| GET | `/api/v1/medicines/{id}/alternatives` | Get alternatives | Doctor |
| GET | `/api/v1/prescription-templates` | List templates | Doctor |
| POST | `/api/v1/prescription-templates` | Create template | Doctor |

### EHR Service APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/health-records` | Create record | Patient |
| GET | `/api/v1/health-records/{id}` | Get record | Patient/Shared Doctor |
| PUT | `/api/v1/health-records/{id}` | Update record | Patient |
| DELETE | `/api/v1/health-records/{id}` | Delete record | Patient |
| GET | `/api/v1/health-records/me` | My records | Patient |
| POST | `/api/v1/health-records/{id}/share` | Share with doctor | Patient |
| DELETE | `/api/v1/health-records/{id}/share/{doctorId}` | Revoke access | Patient |
| GET | `/api/v1/health-records/patient/{id}` | Patient records (doctor) | Doctor |
| POST | `/api/v1/health-records/upload` | Upload document | Patient |
| GET | `/api/v1/health-records/timeline` | Get timeline | Patient/Doctor |
| POST | `/api/v1/vitals` | Record vitals | Patient |
| GET | `/api/v1/vitals/history` | Vitals history | Patient/Doctor |

---

## Service Project Structures

### Prescription Service Structure

```
prescription-service/
├── src/main/java/com/healthapp/prescription/
│   ├── PrescriptionServiceApplication.java
│   ├── controller/
│   │   ├── PrescriptionController.java
│   │   ├── MedicineController.java
│   │   └── TemplateController.java
│   ├── service/
│   │   ├── PrescriptionService.java
│   │   ├── MedicineSearchService.java
│   │   ├── DigitalSignatureService.java
│   │   ├── PdfGenerationService.java
│   │   └── TemplateService.java
│   ├── repository/
│   │   ├── PrescriptionRepository.java
│   │   ├── PrescriptionItemRepository.java
│   │   └── TemplateRepository.java
│   ├── model/
│   │   ├── Prescription.java
│   │   ├── PrescriptionItem.java
│   │   ├── PrescriptionTemplate.java
│   │   └── PrescriptionStatus.java
│   ├── dto/
│   │   ├── CreatePrescriptionRequest.java
│   │   ├── PrescriptionDto.java
│   │   ├── MedicineSearchResult.java
│   │   └── SignPrescriptionRequest.java
│   ├── event/
│   │   └── PrescriptionEventPublisher.java
│   └── config/
│       ├── ElasticsearchConfig.java
│       ├── S3Config.java
│       └── SecurityConfig.java
├── src/main/resources/
│   ├── application.yml
│   ├── templates/
│   │   └── prescription-template.html
│   └── db/migration/
│       └── V1__create_prescription_tables.sql
└── src/test/
```

### EHR Service Structure

```
ehr-service/
├── src/main/java/com/healthapp/ehr/
│   ├── EhrServiceApplication.java
│   ├── controller/
│   │   ├── HealthRecordController.java
│   │   ├── VitalsController.java
│   │   ├── TimelineController.java
│   │   └── DocumentController.java
│   ├── service/
│   │   ├── HealthRecordService.java
│   │   ├── DocumentUploadService.java
│   │   ├── RecordSharingService.java
│   │   ├── VitalsService.java
│   │   ├── TimelineService.java
│   │   └── VirusScanService.java
│   ├── repository/
│   │   ├── HealthRecordRepository.java
│   │   └── VitalsRepository.java
│   ├── model/
│   │   ├── HealthRecord.java
│   │   ├── VitalsHistory.java
│   │   ├── Document.java
│   │   └── SharingEntry.java
│   ├── dto/
│   │   ├── CreateRecordRequest.java
│   │   ├── HealthRecordDto.java
│   │   ├── TimelineEntry.java
│   │   └── VitalsDto.java
│   ├── event/
│   │   ├── EhrEventPublisher.java
│   │   └── PrescriptionEventConsumer.java
│   └── config/
│       ├── MongoConfig.java
│       ├── S3Config.java
│       └── SecurityConfig.java
├── src/main/resources/
│   └── application.yml
└── src/test/
```

---

## Testing Strategy

### Unit Tests

| Component | Test Focus | Coverage Target |
|-----------|------------|-----------------|
| PrescriptionService | CRUD, validation, business rules | 90% |
| DigitalSignatureService | Signing, verification | 95% |
| MedicineSearchService | Search accuracy, performance | 85% |
| HealthRecordService | CRUD, sharing logic | 90% |
| DocumentUploadService | Upload, virus scan | 85% |
| VitalsService | Data validation, calculations | 90% |

### Integration Tests

| Test Scenario | Description |
|---------------|-------------|
| Create Prescription Flow | Doctor creates and signs prescription |
| PDF Generation | Verify PDF content and formatting |
| Medicine Search | Search accuracy and autocomplete |
| Document Upload | Upload, scan, and store |
| Record Sharing | Share and verify access |
| Timeline Aggregation | Verify all sources included |

### End-to-End Tests

| Test Scenario | Description |
|---------------|-------------|
| Complete Prescription Workflow | Consultation → Prescription → EHR |
| Health Record Lifecycle | Upload → View → Share → Revoke |
| Vitals Tracking | Record → View History → Charts |

---

## Security Considerations

### Data Protection

| Aspect | Implementation |
|--------|----------------|
| **Data at Rest** | AES-256 encryption (S3, MongoDB) |
| **Data in Transit** | TLS 1.3 |
| **Document Access** | Pre-signed URLs (1-hour expiry) |
| **PHI Access Logging** | All access logged for audit |

### Access Control

| Resource | Patient | Doctor | Admin |
|----------|---------|--------|-------|
| Own prescriptions | View, Download | Create, Sign | View |
| Own health records | Full CRUD | - | View |
| Shared records | - | View only | View |
| Patient records | - | If shared/consulting | View |

---

## Phase 4 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Prescription Service deployed to dev | End of Week 15 | ⬜ |
| Medicine search working | End of Week 15 | ⬜ |
| Doctor can create prescriptions | End of Week 16 | ⬜ |
| Digital signature working | End of Week 16 | ⬜ |
| PDF generation working | End of Week 16 | ⬜ |
| Patient can view/download prescriptions | End of Week 16 | ⬜ |
| EHR Service deployed to dev | End of Week 17 | ⬜ |
| Document upload working | End of Week 17 | ⬜ |
| Patient can manage health records | End of Week 18 | ⬜ |
| Doctor can view patient history | End of Week 18 | ⬜ |
| Vitals tracking working | End of Week 18 | ⬜ |
| Medical timeline working | End of Week 18 | ⬜ |
| All tests passing | End of Week 18 | ⬜ |

---

## Definition of Done - Phase 4

- [ ] All tasks completed and code merged to main branch
- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests passing
- [ ] API documentation updated (OpenAPI/Swagger)
- [ ] No P0/P1 bugs open
- [ ] Security review completed (PHI handling)
- [ ] HIPAA compliance checklist passed
- [ ] Prescription creation demo successful
- [ ] PDF generation and download working
- [ ] Document upload with virus scanning working
- [ ] Health records sharing working
- [ ] Vitals charts displaying correctly
- [ ] DevOps monitoring dashboards configured

---

## Dependencies on Other Teams

| Dependency | From Team | Description | Required By |
|------------|-----------|-------------|-------------|
| Consultation events | Consultation Service | consultation.completed event | Week 17 |
| Patient info | User Service | Patient details for prescription | Week 15 |
| Doctor profile | Doctor Service | Doctor info for PDF | Week 15 |
| Appointment info | Appointment Service | Link prescription to appointment | Week 15 |
| Notification triggers | Notification Service | Prescription ready notification | Week 16 |

---

## Appendix

### A. Environment Variables

```yaml
# Prescription Service
ELASTICSEARCH_HOST: ${vault:elasticsearch/host}
ELASTICSEARCH_PORT: 9200
S3_BUCKET_PRESCRIPTIONS: healthcare-prescriptions
SIGNING_KEY_PATH: ${vault:prescription-service/signing-key}
SIGNING_CERT_PATH: ${vault:prescription-service/signing-cert}

# EHR Service
MONGODB_URI: ${vault:mongodb/ehr-uri}
MONGODB_DATABASE: healthcare_ehr
S3_BUCKET_DOCUMENTS: healthcare-documents
CLAMAV_HOST: clamav-service
CLAMAV_PORT: 3310
```

### B. Medicine Database Sample

```json
{
    "medicineId": "MED-001",
    "brandName": "Amlodipine",
    "genericName": "Amlodipine Besylate",
    "manufacturer": "Cipla",
    "category": "Cardiovascular",
    "subcategory": "Calcium Channel Blockers",
    "formulation": "Tablet",
    "strength": "5mg",
    "packSize": 10,
    "price": 45.00,
    "requiresPrescription": true,
    "isAvailable": true,
    "alternatives": ["MED-002", "MED-003"],
    "contraindications": ["Hypersensitivity", "Cardiogenic shock"],
    "sideEffects": ["Peripheral edema", "Dizziness", "Flushing"]
}
```

### C. Record Type Categories

```javascript
const RECORD_TYPES = {
    CONSULTATION: {
        icon: '👨‍⚕️',
        color: '#3b82f6',
        label: 'Consultation'
    },
    LAB_REPORT: {
        icon: '🔬',
        color: '#10b981',
        label: 'Lab Report'
    },
    PRESCRIPTION: {
        icon: '💊',
        color: '#8b5cf6',
        label: 'Prescription'
    },
    IMAGING: {
        icon: '📷',
        color: '#f59e0b',
        label: 'Imaging'
    },
    VACCINATION: {
        icon: '💉',
        color: '#ef4444',
        label: 'Vaccination'
    },
    VITALS: {
        icon: '❤️',
        color: '#ec4899',
        label: 'Vitals'
    },
    DISCHARGE_SUMMARY: {
        icon: '🏥',
        color: '#6366f1',
        label: 'Discharge Summary'
    },
    OTHER: {
        icon: '📄',
        color: '#6b7280',
        label: 'Other'
    }
};
```

---

*Document Version: 1.0*  
*Last Updated: January 30, 2026*  
*Author: Healthcare Platform Team*
