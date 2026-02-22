package com.healthapp.prescription.service;

import org.springframework.context.annotation.Profile;
import com.healthapp.prescription.domain.Prescription;
import com.healthapp.prescription.domain.PrescriptionItem;
import com.healthapp.prescription.dto.PrescriptionResponse.DoctorInfo;
import com.healthapp.prescription.dto.PrescriptionResponse.PatientInfo;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import org.springframework.context.annotation.Profile;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service for generating prescription PDFs.
 */
@Slf4j
@Profile("!test")
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket:healthapp-prescriptions}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiration:24h}")
    private Duration presignedUrlExpiration;

    /**
     * Generate PDF and upload to S3.
     */
    public Mono<PdfResult> generateAndStorePdf(
            Prescription prescription,
            List<PrescriptionItem> items,
            DoctorInfo doctor,
            PatientInfo patient
    ) {
        return Mono.fromCallable(() -> {
            byte[] pdfBytes = generatePdf(prescription, items, doctor, patient);
            String s3Key = buildS3Key(prescription);
            
            // Upload to S3
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .contentType("application/pdf")
                            .build(),
                    RequestBody.fromBytes(pdfBytes)
            );
            
            // Generate presigned URL
            String presignedUrl = generatePresignedUrl(s3Key);
            
            log.info("Generated PDF for prescription: {} at S3 key: {}", 
                    prescription.getPrescriptionNumber(), s3Key);
            
            return PdfResult.builder()
                    .s3Key(s3Key)
                    .presignedUrl(presignedUrl)
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get presigned URL for existing PDF.
     */
    public Mono<String> getPresignedUrl(String s3Key) {
        return Mono.fromCallable(() -> generatePresignedUrl(s3Key))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private byte[] generatePdf(
            Prescription prescription,
            List<PrescriptionItem> items,
            DoctorInfo doctor,
            PatientInfo patient
    ) throws Exception {
        String html = buildPrescriptionHtml(prescription, items, doctor, patient);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, baos);
        
        return baos.toByteArray();
    }

    private String buildPrescriptionHtml(
            Prescription prescription,
            List<PrescriptionItem> items,
            DoctorInfo doctor,
            PatientInfo patient
    ) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; font-size: 12px; margin: 20px; }");
        html.append(".header { border-bottom: 2px solid #333; padding-bottom: 10px; margin-bottom: 15px; }");
        html.append(".doctor-name { font-size: 18px; font-weight: bold; color: #2c5282; }");
        html.append(".doctor-info { color: #666; font-size: 11px; }");
        html.append(".patient-info { background-color: #f7fafc; padding: 10px; margin-bottom: 15px; }");
        html.append(".section-title { font-weight: bold; color: #2c5282; margin-top: 15px; margin-bottom: 5px; }");
        html.append(".rx-symbol { font-size: 24px; color: #2c5282; font-weight: bold; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #edf2f7; }");
        html.append(".footer { margin-top: 30px; border-top: 1px solid #ddd; padding-top: 10px; font-size: 10px; color: #666; }");
        html.append(".signature { text-align: right; margin-top: 40px; }");
        html.append(".advice-box { background-color: #fffff0; padding: 10px; margin-top: 10px; }");
        html.append("</style></head><body>");
        
        // Header with Doctor Info
        html.append("<div class='header'>");
        if (doctor != null) {
            html.append("<div class='doctor-name'>").append(doctor.getName() != null ? doctor.getName() : "Doctor").append("</div>");
            html.append("<div class='doctor-info'>");
            if (doctor.getQualifications() != null) html.append(doctor.getQualifications()).append("<br>");
            if (doctor.getSpecialization() != null) html.append(doctor.getSpecialization()).append("<br>");
            if (doctor.getRegistrationNumber() != null) html.append("Reg. No: ").append(doctor.getRegistrationNumber()).append("<br>");
            if (doctor.getClinicName() != null) html.append(doctor.getClinicName()).append("<br>");
            if (doctor.getClinicAddress() != null) html.append(doctor.getClinicAddress()).append("<br>");
            if (doctor.getPhoneNumber() != null) html.append("Ph: ").append(doctor.getPhoneNumber());
            html.append("</div>");
        }
        html.append("</div>");
        
        // Patient Info
        html.append("<div class='patient-info'>");
        html.append("<strong>Patient: </strong>");
        if (patient != null) {
            html.append(patient.getName() != null ? patient.getName() : "N/A");
            if (patient.getAge() != null) html.append(" (").append(patient.getAge()).append(" yrs)");
            if (patient.getGender() != null) html.append(" | ").append(patient.getGender());
        }
        html.append(" | <strong>Rx No: </strong>").append(prescription.getPrescriptionNumber());
        html.append("<br><strong>Date: </strong>").append(prescription.getPrescriptionDate().format(dateFormatter));
        if (prescription.getValidUntil() != null) {
            html.append(" | <strong>Valid Until: </strong>").append(prescription.getValidUntil().format(dateFormatter));
        }
        html.append("</div>");
        
        // Diagnosis
        if (prescription.getDiagnosis() != null) {
            html.append("<div class='section-title'>Diagnosis</div>");
            html.append("<div>").append(prescription.getDiagnosis()).append("</div>");
        }
        
        if (prescription.getChiefComplaints() != null) {
            html.append("<div class='section-title'>Chief Complaints</div>");
            html.append("<div>").append(prescription.getChiefComplaints()).append("</div>");
        }
        
        // Medicines Table
        html.append("<div class='section-title'><span class='rx-symbol'>Ã¢â€žÅ¾</span> Medicines</div>");
        html.append("<table>");
        html.append("<tr><th>#</th><th>Medicine</th><th>Dosage</th><th>Frequency</th><th>Duration</th><th>Instructions</th></tr>");
        
        int index = 1;
        for (PrescriptionItem item : items) {
            html.append("<tr>");
            html.append("<td>").append(index++).append("</td>");
            html.append("<td><strong>").append(item.getMedicineName()).append("</strong>");
            if (item.getStrength() != null) html.append(" ").append(item.getStrength());
            if (item.getFormulation() != null) html.append(" (").append(item.getFormulation()).append(")");
            if (item.getGenericName() != null) html.append("<br><small>").append(item.getGenericName()).append("</small>");
            html.append("</td>");
            html.append("<td>").append(item.getDosage() != null ? item.getDosage() : "-").append("</td>");
            html.append("<td>").append(item.getFrequency() != null ? item.getFrequency() : "-").append("</td>");
            html.append("<td>").append(item.getDuration() != null ? item.getDuration() : "-").append("</td>");
            html.append("<td>").append(item.getTiming() != null ? item.getTiming() : "");
            if (item.getSpecialInstructions() != null) {
                html.append("<br><small>").append(item.getSpecialInstructions()).append("</small>");
            }
            html.append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        
        // Advice
        if (prescription.getGeneralAdvice() != null || prescription.getDietAdvice() != null) {
            html.append("<div class='advice-box'>");
            if (prescription.getGeneralAdvice() != null) {
                html.append("<div class='section-title'>Advice</div>");
                html.append("<div>").append(prescription.getGeneralAdvice().replace("\n", "<br>")).append("</div>");
            }
            if (prescription.getDietAdvice() != null) {
                html.append("<div class='section-title'>Diet Advice</div>");
                html.append("<div>").append(prescription.getDietAdvice().replace("\n", "<br>")).append("</div>");
            }
            html.append("</div>");
        }
        
        // Lab Tests
        if (prescription.getLabTestsRecommended() != null && !prescription.getLabTestsRecommended().isEmpty()) {
            html.append("<div class='section-title'>Lab Tests Recommended</div>");
            html.append("<div>").append(prescription.getLabTestsRecommended()).append("</div>");
        }
        
        // Follow-up
        if (prescription.getFollowUpDate() != null) {
            html.append("<div class='section-title'>Follow-up</div>");
            html.append("<div>").append(prescription.getFollowUpDate().format(dateFormatter));
            if (prescription.getFollowUpNotes() != null) {
                html.append(" - ").append(prescription.getFollowUpNotes());
            }
            html.append("</div>");
        }
        
        // Signature
        html.append("<div class='signature'>");
        if (prescription.getSignedAt() != null) {
            html.append("<div>Digitally Signed</div>");
        }
        if (doctor != null && doctor.getName() != null) {
            html.append("<div><strong>").append(doctor.getName()).append("</strong></div>");
        }
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("This is a digitally generated prescription. ");
        html.append("Verify at: https://healthapp.com/verify/").append(prescription.getPrescriptionNumber());
        html.append("</div>");
        
        html.append("</body></html>");
        
        return html.toString();
    }

    private String buildS3Key(Prescription prescription) {
        return String.format("prescriptions/%s/%s/%s.pdf",
                prescription.getDoctorId(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")),
                prescription.getPrescriptionNumber());
    }

    private String generatePresignedUrl(String s3Key) {
        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(presignedUrlExpiration)
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build())
                .build();
        
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PdfResult {
        private String s3Key;
        private String presignedUrl;
    }
}
