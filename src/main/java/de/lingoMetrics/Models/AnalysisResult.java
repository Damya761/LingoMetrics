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



    public boolean hasAuswertung() {
        return score != null;
    }

}