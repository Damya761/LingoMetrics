package de.lingoMetrics.Models;

import de.lingoMetrics.Service.ServiceManager;

import java.util.*;

public record AnalysisResult(
        Document document,
        String stiltyp,
        boolean export,
        boolean comparison,
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
        double mittlereKonkretheit,
        Integer score,
        String gesamtBewertung,
        List<String> hinweise
) {
    public AnalysisResult {
        interpunktion = interpunktion == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(interpunktion));
        hinweise = hinweise == null
                ? List.of()
                : List.copyOf(hinweise);
    }

    public static AnalysisResult from(
            Document document,
            ServiceManager.AnalysisRequest request,
            Integer score,
            String gesamtBewertung,
            List<String> hinweise
    ) {
        Map<String, Long> interpunktion = document.getInterpunktion() == null
                ? Map.of()
                : document.getInterpunktion();

        return new AnalysisResult(
                document,
                request.getStiltyp(),
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
                document.getMittlereKonkretheit(),
                score,
                gesamtBewertung,
                hinweise
        );
    }

    public boolean hasAuswertung() {
        return score != null;
    }

    private static int countNormaleWoerter(Document document) {
        if (document.getWoerter() == null) return 0;

        return (int) document.getWoerter()
                .stream()
                .filter(Objects::nonNull)
                .filter(wort -> !wort.isSatzzeichen())
                .count();
    }
}