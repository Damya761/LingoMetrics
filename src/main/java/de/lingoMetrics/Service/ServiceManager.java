package de.lingoMetrics.Service;

import de.lingoMetrics.Models.AnalysisResult;
import de.lingoMetrics.Models.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ServiceManager {

    private final TextStrukturService textStrukturService;
    private final List<Consumer<Document>> analyseServices;
    private final AuswertungsService auswertungsService;

    public ServiceManager(
            TextStrukturService textStrukturService,
            List<Consumer<Document>> analyseServices,
            AuswertungsService auswertungsService
    ) {
        this.textStrukturService = Objects.requireNonNull(textStrukturService, "textStrukturService must not be null.");
        this.analyseServices = List.copyOf(Objects.requireNonNull(analyseServices, "analyseServices must not be null."));
        this.auswertungsService = auswertungsService;
    }

    public AnalysisResult analyse(AnalysisRequest request) {
        validateRequest(request);

        Document document = textStrukturService.createDocument(request.getRawtext());

        for (Consumer<Document> analyseService : analyseServices) {
            analyseService.accept(document);
        }

        Integer score = null;
        String gesamtBewertung = null;
        List<String> hinweise = List.of();

        if (request.isComparison()) {
            if (auswertungsService == null) {
                throw new IllegalStateException("AuswertungsService must be configured for comparison requests.");
            }

            List<String> auswertungsHinweise = new ArrayList<>();
            score = auswertungsService.calculateScore(document, request.getStiltyp(), auswertungsHinweise);
            gesamtBewertung = auswertungsService.determineRating(score);
            hinweise = auswertungsHinweise;
        }

        return AnalysisResult.from(document, request, score, gesamtBewertung, hinweise);
    }

    private void validateRequest(AnalysisRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AnalysisRequest must not be null.");
        }

        if (request.getRawtext() == null || request.getRawtext().trim().isEmpty()) {
            throw new IllegalArgumentException("AnalysisRequest text must not be null or empty.");
        }

        if (request.isComparison() && (request.getStiltyp() == null || request.getStiltyp().trim().isEmpty())) {
            throw new IllegalArgumentException("AnalysisRequest style type must not be null or empty for comparisons.");
        }
    }

    public static final class AnalysisRequest {
        private final String rawtext;
        private final String stiltyp;
        private final boolean export;
        private final boolean comparison;

        public AnalysisRequest(String rawtext, String stiltyp, boolean isExport, boolean isComparison) {
            this.rawtext = rawtext;
            this.stiltyp = stiltyp;
            this.export = isExport;
            this.comparison = isComparison;
        }

        public String getRawtext() {
            return rawtext;
        }

        public String getStiltyp() {
            return stiltyp;
        }

        public boolean isExport() {
            return export;
        }

        public boolean isComparison() {
            return comparison;
        }
    }
}
