package com.healthapp.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;
import com.healthapp.search.model.DoctorDocument;
import com.healthapp.search.model.dto.*;
import com.healthapp.search.repository.DoctorSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for doctor search operations using Elasticsearch.
 * Disabled in test profile to avoid requiring Elasticsearch during tests.
 */
@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class DoctorSearchService {
    
    private final DoctorSearchRepository doctorSearchRepository;
    private final ElasticsearchClient elasticsearchClient;
    
    private static final String INDEX_NAME = "doctors";
    
    /**
     * Performs an advanced doctor search with filters, sorting, and aggregations.
     */
    public Mono<DoctorSearchResponse> search(DoctorSearchRequest request) {
        return Mono.fromCallable(() -> executeSearch(request))
                .doOnSuccess(response -> log.debug(
                        "Search completed: {} results in {}ms",
                        response.getTotalHits(), response.getTookMs()
                ))
                .doOnError(error -> log.error("Search failed", error));
    }
    
    /**
     * Provides autocomplete suggestions for doctor search.
     */
    public Mono<List<String>> autocomplete(String query, int limit) {
        return Mono.fromCallable(() -> executeAutocomplete(query, limit));
    }
    
    /**
     * Indexes a doctor document.
     */
    public Mono<DoctorDocument> indexDoctor(DoctorDocument document) {
        // Calculate popularity score
        document.setPopularityScore(calculatePopularityScore(document));
        
        return doctorSearchRepository.save(document)
                .doOnSuccess(doc -> log.info("Indexed doctor: {}", doc.getId()));
    }
    
    /**
     * Updates a doctor document.
     */
    public Mono<DoctorDocument> updateDoctor(DoctorDocument document) {
        document.setPopularityScore(calculatePopularityScore(document));
        
        return doctorSearchRepository.save(document)
                .doOnSuccess(doc -> log.info("Updated doctor index: {}", doc.getId()));
    }
    
    /**
     * Deletes a doctor from the index.
     */
    public Mono<Void> deleteDoctor(String doctorId) {
        return doctorSearchRepository.deleteById(doctorId)
                .doOnSuccess(v -> log.info("Deleted doctor from index: {}", doctorId));
    }
    
    // Private helper methods
    
    private DoctorSearchResponse executeSearch(DoctorSearchRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        
        // Build the query
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        
        // Must accept patients (unless disabled)
        if (Boolean.TRUE.equals(request.getAcceptingPatientsOnly())) {
            boolQuery.filter(QueryBuilders.term(t -> t.field("isAcceptingPatients").value(true)));
        }
        
        // Free text search
        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            boolQuery.must(QueryBuilders.multiMatch(m -> m
                    .query(request.getQuery())
                    .fields("fullName^3", "fullName.autocomplete^2", "primarySpecialization^2", 
                            "specializations.name", "bio", "clinics.name", "cities")
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
            ));
        }
        
        // Specialization filter
        if (request.getSpecialization() != null) {
            boolQuery.filter(QueryBuilders.term(t -> t
                    .field("primarySpecialization.keyword")
                    .value(request.getSpecialization())
            ));
        }
        
        if (request.getSpecializationIds() != null && !request.getSpecializationIds().isEmpty()) {
            boolQuery.filter(QueryBuilders.terms(t -> t
                    .field("specializations.id")
                    .terms(v -> v.value(request.getSpecializationIds().stream()
                            .map(id -> co.elastic.clients.elasticsearch._types.FieldValue.of(id))
                            .collect(Collectors.toList())))
            ));
        }
        
        // City filter
        if (request.getCity() != null) {
            boolQuery.filter(QueryBuilders.term(t -> t
                    .field("cities")
                    .value(request.getCity())
            ));
        }
        
        // Rating filter
        if (request.getMinRating() != null) {
            boolQuery.filter(QueryBuilders.range(r -> r
                    .field("rating")
                    .gte(JsonData.of(request.getMinRating()))
            ));
        }
        
        // Fee filter
        if (request.getMinFee() != null) {
            boolQuery.filter(QueryBuilders.range(r -> r
                    .field("consultationFee")
                    .gte(JsonData.of(request.getMinFee()))
            ));
        }
        if (request.getMaxFee() != null) {
            boolQuery.filter(QueryBuilders.range(r -> r
                    .field("consultationFee")
                    .lte(JsonData.of(request.getMaxFee()))
            ));
        }
        
        // Experience filter
        if (request.getMinExperience() != null) {
            boolQuery.filter(QueryBuilders.range(r -> r
                    .field("experienceYears")
                    .gte(JsonData.of(request.getMinExperience()))
            ));
        }
        
        // Verified filter
        if (Boolean.TRUE.equals(request.getVerifiedOnly())) {
            boolQuery.filter(QueryBuilders.term(t -> t.field("isVerified").value(true)));
        }
        
        // Video consultation filter
        if (Boolean.TRUE.equals(request.getOffersVideoConsultation())) {
            boolQuery.filter(QueryBuilders.term(t -> t.field("offersVideoConsultation").value(true)));
        }
        
        // Languages filter
        if (request.getLanguages() != null && !request.getLanguages().isEmpty()) {
            boolQuery.filter(QueryBuilders.terms(t -> t
                    .field("languages")
                    .terms(v -> v.value(request.getLanguages().stream()
                            .map(lang -> co.elastic.clients.elasticsearch._types.FieldValue.of(lang))
                            .collect(Collectors.toList())))
            ));
        }
        
        // Geo distance filter
        if (request.getLatitude() != null && request.getLongitude() != null && request.getRadiusKm() != null) {
            boolQuery.filter(QueryBuilders.geoDistance(g -> g
                    .field("primaryLocation")
                    .distance(request.getRadiusKm() + "km")
                    .location(l -> l.latlon(ll -> ll.lat(request.getLatitude()).lon(request.getLongitude())))
            ));
        }
        
        // Build search request
        SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(boolQuery.build()._toQuery())
                .from(request.getPage() * request.getSize())
                .size(request.getSize());
        
        // Add sorting
        addSorting(searchBuilder, request);
        
        // Add highlighting
        searchBuilder.highlight(h -> h
                .fields("fullName", f -> f.preTags("<em>").postTags("</em>"))
                .fields("specializations.name", f -> f.preTags("<em>").postTags("</em>"))
                .fields("bio", f -> f.preTags("<em>").postTags("</em>").fragmentSize(150).numberOfFragments(3))
        );
        
        // Execute search
        SearchResponse<DoctorDocument> response = elasticsearchClient.search(
                searchBuilder.build(), DoctorDocument.class
        );
        
        // Map results
        List<DoctorSearchResult> results = response.hits().hits().stream()
                .map(this::mapHitToResult)
                .collect(Collectors.toList());
        
        TotalHits totalHits = response.hits().total();
        long total = totalHits != null ? totalHits.value() : 0;
        
        return DoctorSearchResponse.builder()
                .results(results)
                .totalHits(total)
                .page(request.getPage())
                .size(request.getSize())
                .totalPages((int) Math.ceil((double) total / request.getSize()))
                .tookMs(System.currentTimeMillis() - startTime)
                .build();
    }
    
    private void addSorting(SearchRequest.Builder builder, DoctorSearchRequest request) {
        SortOrder order = request.getSortDirection() == DoctorSearchRequest.SortDirection.ASC 
                ? SortOrder.Asc : SortOrder.Desc;
        
        switch (request.getSortBy()) {
            case RATING:
                builder.sort(s -> s.field(f -> f.field("rating").order(order)));
                break;
            case EXPERIENCE:
                builder.sort(s -> s.field(f -> f.field("experienceYears").order(order)));
                break;
            case CONSULTATION_FEE:
                builder.sort(s -> s.field(f -> f.field("consultationFee").order(order)));
                break;
            case POPULARITY:
                builder.sort(s -> s.field(f -> f.field("popularityScore").order(order)));
                break;
            case DISTANCE:
                if (request.getLatitude() != null && request.getLongitude() != null) {
                    builder.sort(s -> s.geoDistance(g -> g
                            .field("primaryLocation")
                            .location(l -> l.latlon(ll -> ll.lat(request.getLatitude()).lon(request.getLongitude())))
                            .order(order)
                            .unit(co.elastic.clients.elasticsearch._types.DistanceUnit.Kilometers)
                    ));
                }
                break;
            case RELEVANCE:
            default:
                builder.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
                break;
        }
    }
    
    private DoctorSearchResult mapHitToResult(Hit<DoctorDocument> hit) {
        DoctorDocument doc = hit.source();
        if (doc == null) return null;
        
        List<String> highlights = new ArrayList<>();
        if (hit.highlight() != null) {
            hit.highlight().forEach((field, fragments) -> highlights.addAll(fragments));
        }
        
        return DoctorSearchResult.builder()
                .id(doc.getId())
                .userId(doc.getUserId())
                .fullName(doc.getFullName())
                .title(doc.getTitle())
                .profilePhotoUrl(doc.getProfilePhotoUrl())
                .bio(doc.getBio())
                .specializations(doc.getSpecializations() != null ? 
                        doc.getSpecializations().stream()
                                .map(s -> DoctorSearchResult.SpecializationResult.builder()
                                        .id(s.getId())
                                        .name(s.getName())
                                        .isPrimary(s.getIsPrimary())
                                        .build())
                                .collect(Collectors.toList()) : null)
                .primarySpecialization(doc.getPrimarySpecialization())
                .qualifications(doc.getQualifications() != null ?
                        doc.getQualifications().stream()
                                .map(q -> DoctorSearchResult.QualificationResult.builder()
                                        .degree(q.getDegree())
                                        .institution(q.getInstitution())
                                        .year(q.getYear())
                                        .build())
                                .collect(Collectors.toList()) : null)
                .languages(doc.getLanguages())
                .clinics(doc.getClinics() != null ?
                        doc.getClinics().stream()
                                .map(c -> DoctorSearchResult.ClinicResult.builder()
                                        .id(c.getId())
                                        .name(c.getName())
                                        .address(c.getAddress())
                                        .city(c.getCity())
                                        .isPrimary(c.getIsPrimary())
                                        .build())
                                .collect(Collectors.toList()) : null)
                .primaryCity(doc.getCities() != null && !doc.getCities().isEmpty() ? 
                        doc.getCities().get(0) : null)
                .experienceYears(doc.getExperienceYears())
                .consultationFee(doc.getConsultationFee())
                .videoConsultationFee(doc.getVideoConsultationFee())
                .rating(doc.getRating())
                .totalReviews(doc.getTotalReviews())
                .totalConsultations(doc.getTotalConsultations())
                .isVerified(doc.getIsVerified())
                .isAcceptingPatients(doc.getIsAcceptingPatients())
                .offersVideoConsultation(doc.getOffersVideoConsultation())
                .offersInPersonConsultation(doc.getOffersInPersonConsultation())
                .score(hit.score())
                .highlights(highlights)
                .build();
    }
    
    private List<String> executeAutocomplete(String query, int limit) throws Exception {
        SearchResponse<DoctorDocument> response = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q.bool(b -> b
                                .must(m -> m.match(mt -> mt
                                        .field("fullName.autocomplete")
                                        .query(query)
                                ))
                                .filter(f -> f.term(t -> t.field("isAcceptingPatients").value(true)))
                        ))
                        .size(limit)
                        .source(src -> src.filter(f -> f.includes("fullName", "primarySpecialization"))),
                DoctorDocument.class
        );
        
        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(doc -> doc != null)
                .map(doc -> doc.getFullName() + " - " + doc.getPrimarySpecialization())
                .collect(Collectors.toList());
    }
    
    private Double calculatePopularityScore(DoctorDocument doc) {
        double score = 0.0;
        
        // Rating contribution (0-5 scale, weight: 40%)
        if (doc.getRating() != null) {
            score += (doc.getRating() / 5.0) * 40;
        }
        
        // Reviews contribution (log scale, weight: 25%)
        if (doc.getTotalReviews() != null && doc.getTotalReviews() > 0) {
            score += Math.min(Math.log10(doc.getTotalReviews()) * 10, 25);
        }
        
        // Consultations contribution (log scale, weight: 20%)
        if (doc.getTotalConsultations() != null && doc.getTotalConsultations() > 0) {
            score += Math.min(Math.log10(doc.getTotalConsultations()) * 8, 20);
        }
        
        // Profile views contribution (log scale, weight: 10%)
        if (doc.getProfileViews() != null && doc.getProfileViews() > 0) {
            score += Math.min(Math.log10(doc.getProfileViews()) * 4, 10);
        }
        
        // Verified bonus (5%)
        if (Boolean.TRUE.equals(doc.getIsVerified())) {
            score += 5;
        }
        
        return score;
    }
}
