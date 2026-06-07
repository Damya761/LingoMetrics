package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Repository.WordRepository;
import de.lingoMetrics.Service.analysis.TextStatisticsService;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ServiceManager {

    private final TextStrukturService textStrukturService;
    private final List<Consumer<Document>> analyseServices;

    public ServiceManager(
            TextStrukturService textStrukturService,
            TextStatisticsService textStatisticsService,
            WortSchatzAnalyseService wortSchatzAnalyseService
    ) {
        this(
                textStrukturService,
                List.of(
                        wortSchatzAnalyseService::Analyze,
                        textStatisticsService::analyze
                )
        );
    }

    public ServiceManager(
            TextStrukturService textStrukturService,
            List<Consumer<Document>> analyseServices
    ) {
        this.textStrukturService = Objects.requireNonNull(textStrukturService, "textStrukturService must not be null.");
        this.analyseServices = List.copyOf(Objects.requireNonNull(analyseServices, "analyseServices must not be null."));
    }

    public static ServiceManager createDefault() throws IOException {
        WordRepository wordRepository = new WordRepository();
        wordRepository.load();

        return new ServiceManager(
                new TextStrukturService(),
                new TextStatisticsService(),
                new WortSchatzAnalyseService(wordRepository)
        );
    }

    public AnalysisResult analyse(AnalysisRequest request) {
        validateRequest(request);

        Document document = textStrukturService.createDocument(request.getRawtext());

        for (Consumer<Document> analyseService : analyseServices) {
            analyseService.accept(document);
        }

        return AnalysisResult.from(document, request);
    }

    public AnalysisResult analyze(AnalysisRequest request) {
        return analyse(request);
    }

    private void validateRequest(AnalysisRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AnalysisRequest must not be null.");
        }

        if (request.getRawtext() == null || request.getRawtext().trim().isEmpty()) {
            throw new IllegalArgumentException("AnalysisRequest text must not be null or empty.");
        }

        if (request.isComparison() && (request.getStiltype() == null || request.getStiltype().trim().isEmpty())) {
            throw new IllegalArgumentException("AnalysisRequest style type must not be null or empty for comparisons.");
        }
    }

    public static final class AnalysisRequest {
        private final String rawtext;
        private final String stiltype;
        private final boolean export;
        private final boolean comparison;

        public AnalysisRequest(String rawtext, String stiltype, boolean isExport, boolean isComparison) {
            this.rawtext = rawtext;
            this.stiltype = stiltype;
            this.export = isExport;
            this.comparison = isComparison;
        }

        public AnalysisRequest(String rohtext, String stiltyp) {
            this(rohtext, stiltyp, false, false);
        }

        public static AnalysisRequest of(String rawtext, String stiltype, boolean isExport, boolean isComparison) {
            return new AnalysisRequest(rawtext, stiltype, isExport, isComparison);
        }

        public static AnalysisRequest of(String rohtext, String stiltyp) {
            return new AnalysisRequest(rohtext, stiltyp);
        }

        public String getRawtext() {
            return rawtext;
        }

        public String getRawText() {
            return rawtext;
        }

        public String getRohtext() {
            return rawtext;
        }

        public String getStiltype() {
            return stiltype;
        }

        public String getStilType() {
            return stiltype;
        }

        public String getStiltyp() {
            return stiltype;
        }

        public boolean isExport() {
            return export;
        }

        public boolean isComparison() {
            return comparison;
        }
    }

    public static final class AnalysisResult {
        private final String stiltype;
        private final boolean export;
        private final boolean comparison;
        private final int absatzAnzahl;
        private final int satzAnzahl;
        private final int wortAnzahl;
        private final Map<String, Long> interpunktion;
        private final double wortlaengenverteilung;
        private final double mittlereSatzlaenge;
        private final double satzlaengenunterschied;
        private final double funktionswoerterAnteil;
        private final double fuellwoerterAnteil;
        private final double typeTokenRatio;
        private final double lesbarkeitsindex;
        private final double mittleresSentiment;
        private final int hapaxLegomena;
        private final double adjektivVerbQuotient;
        private final double mittlereKonkretheit;

        private AnalysisResult(
                String stiltype,
                boolean isExport,
                boolean isComparison,
                int absatzAnzahl,
                int satzAnzahl,
                int wortAnzahl,
                Map<String, Long> interpunktion,
                double wortlaengenverteilung,
                double mittlereSatzlaenge,
                double satzlaengenunterschied,
                double funktionswoerterAnteil,
                double fuellwoerterAnteil,
                double typeTokenRatio,
                double lesbarkeitsindex,
                double mittleresSentiment,
                int hapaxLegomena,
                double adjektivVerbQuotient,
                double mittlereKonkretheit
        ) {
            this.stiltype = stiltype;
            this.export = isExport;
            this.comparison = isComparison;
            this.absatzAnzahl = absatzAnzahl;
            this.satzAnzahl = satzAnzahl;
            this.wortAnzahl = wortAnzahl;
            this.interpunktion = Collections.unmodifiableMap(new LinkedHashMap<>(interpunktion));
            this.wortlaengenverteilung = wortlaengenverteilung;
            this.mittlereSatzlaenge = mittlereSatzlaenge;
            this.satzlaengenunterschied = satzlaengenunterschied;
            this.funktionswoerterAnteil = funktionswoerterAnteil;
            this.fuellwoerterAnteil = fuellwoerterAnteil;
            this.typeTokenRatio = typeTokenRatio;
            this.lesbarkeitsindex = lesbarkeitsindex;
            this.mittleresSentiment = mittleresSentiment;
            this.hapaxLegomena = hapaxLegomena;
            this.adjektivVerbQuotient = adjektivVerbQuotient;
            this.mittlereKonkretheit = mittlereKonkretheit;
        }

        private static AnalysisResult from(Document document, AnalysisRequest request) {
            Map<String, Long> interpunktion = document.getInterpunktion() == null
                    ? Map.of()
                    : document.getInterpunktion();

            return new AnalysisResult(
                    request.getStiltype(),
                    request.isExport(),
                    request.isComparison(),
                    document.getAbsaetze() == null ? 0 : document.getAbsaetze().size(),
                    document.getSaetze() == null ? 0 : document.getSaetze().size(),
                    countNormaleWoerter(document),
                    interpunktion,
                    document.getWortlaengenverteilung(),
                    document.getMittlereSatzlaenge(),
                    document.getSatzlaengenunterschied(),
                    document.getFunktionswoerterAnteil(),
                    document.getFuellwoerterAnteil(),
                    document.getTypeTokenRatio(),
                    document.getLesbarkeitsindex(),
                    document.getMittleresSentiment(),
                    document.getHapaxLegomena(),
                    document.getAdjektivVerbQuotient(),
                    document.getMittlereKonkretheit()
            );
        }

        private static int countNormaleWoerter(Document document) {
            if (document.getWoerter() == null) {
                return 0;
            }

            return (int) document.getWoerter()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(wort -> !wort.isSatzzeichen())
                    .count();
        }

        public String getStiltype() {
            return stiltype;
        }

        public String getStilType() {
            return stiltype;
        }

        public String getStiltyp() {
            return stiltype;
        }

        public boolean isExport() {
            return export;
        }

        public boolean isComparison() {
            return comparison;
        }

        public int getAbsatzAnzahl() {
            return absatzAnzahl;
        }

        public int getSatzAnzahl() {
            return satzAnzahl;
        }

        public int getWortAnzahl() {
            return wortAnzahl;
        }

        public Map<String, Long> getInterpunktion() {
            return interpunktion;
        }

        public double getWortlaengenverteilung() {
            return wortlaengenverteilung;
        }

        public double getMittlereSatzlaenge() {
            return mittlereSatzlaenge;
        }

        public double getSatzlaengenunterschied() {
            return satzlaengenunterschied;
        }

        public double getFunktionswoerterAnteil() {
            return funktionswoerterAnteil;
        }

        public double getFuellwoerterAnteil() {
            return fuellwoerterAnteil;
        }

        public double getTypeTokenRatio() {
            return typeTokenRatio;
        }

        public double getLesbarkeitsindex() {
            return lesbarkeitsindex;
        }

        public double getMittleresSentiment() {
            return mittleresSentiment;
        }

        public int getHapaxLegomena() {
            return hapaxLegomena;
        }

        public double getAdjektivVerbQuotient() {
            return adjektivVerbQuotient;
        }

        public double getMittlereKonkretheit() {
            return mittlereKonkretheit;
        }
    }
}
