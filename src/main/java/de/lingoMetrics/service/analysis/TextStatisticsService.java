package de.lingoMetrics.Service.analysis;

import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Models.Satz;
import de.lingoMetrics.Models.Wort;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

//Autor: Tarik Marton
public class TextStatisticsService {

    public void analyze(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document must not be null.");
        }

        List<Satz> saetze = document.getSaetze();
        List<Wort> woerter = document.getWoerter();

        double mittlereSatzlaenge = calculateMittlereSatzlaenge(saetze);
        double satzlaengenunterschied = calculateSatzlaengenunterschied(saetze);
        double wortlaengenverteilung = calculateDurchschnittlicheWortlaenge(woerter);
        Map<String, Long> interpunktion = calculateInterpunktionsProfil(woerter);
        double lesbarkeitsindex = calculateLesbarkeitsindex(saetze, woerter);

        document.setMittlereSatzlaenge(mittlereSatzlaenge);
        document.setSatzlaengenunterschied(satzlaengenunterschied);
        document.setWortlaengenverteilung(wortlaengenverteilung);
        document.setInterpunktion(interpunktion);
        document.setLesbarkeitsindex(lesbarkeitsindex);
    }

    private double calculateMittlereSatzlaenge(List<Satz> saetze) {
        if (saetze == null || saetze.isEmpty()) {
            return 0.0;
        }

        int gesamteWoerterAnzahl = saetze.stream()
                .filter(Objects::nonNull)
                .mapToInt(Satz::getWoerterAnzahl)
                .sum();

        return (double) gesamteWoerterAnzahl / saetze.size();
    }

    private double calculateSatzlaengenunterschied(List<Satz> saetze) {
        if (saetze == null || saetze.size() < 2) {
            return 0.0;
        }

        int gesamterUnterschied = 0;

        for (int i = 1; i < saetze.size(); i++) {
            int vorherigeSatzlaenge = saetze.get(i - 1).getWoerterAnzahl();
            int aktuelleSatzlaenge = saetze.get(i).getWoerterAnzahl();

            gesamterUnterschied += Math.abs(aktuelleSatzlaenge - vorherigeSatzlaenge);
        }

        return (double) gesamterUnterschied / (saetze.size() - 1);
    }

    private double calculateDurchschnittlicheWortlaenge(List<Wort> woerter) {
        List<Wort> normaleWoerter = filterNormaleWoerter(woerter);

        if (normaleWoerter.isEmpty()) {
            return 0.0;
        }

        int gesamteLaenge = normaleWoerter.stream()
                .mapToInt(Wort::getLaenge)
                .sum();

        return (double) gesamteLaenge / normaleWoerter.size();
    }

    private Map<String, Long> calculateInterpunktionsProfil(List<Wort> woerter) {
        if (woerter == null) {
            return Map.of();
        }

        return woerter.stream()
                .filter(Objects::nonNull)
                .filter(Wort::isSatzzeichen)
                .map(Wort::getInhalt)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(inhalt -> !inhalt.isBlank())
                .collect(Collectors.groupingBy(
                        satzzeichen -> satzzeichen,
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    // LIX = Wortanzahl / Satzanzahl + lange Wörter * 100 / Wortanzahl
    private double calculateLesbarkeitsindex(List<Satz> saetze, List<Wort> woerter) {
        List<Wort> normaleWoerter = filterNormaleWoerter(woerter);

        int satzAnzahl = saetze == null ? 0 : saetze.size();
        int wortAnzahl = normaleWoerter.size();

        if (satzAnzahl == 0 || wortAnzahl == 0) {
            return 0.0;
        }

        long langeWoerter = normaleWoerter.stream()
                .filter(wort -> wort.getLaenge() > 6)
                .count();

        return ((double) wortAnzahl / satzAnzahl)
                + ((double) langeWoerter * 100 / wortAnzahl);
    }

    private List<Wort> filterNormaleWoerter(List<Wort> woerter) {
        if (woerter == null) {
            return List.of();
        }

        return woerter.stream()
                .filter(Objects::nonNull)
                .filter(wort -> !wort.isSatzzeichen())
                .filter(wort -> wort.getInhalt() != null)
                .filter(wort -> !wort.getInhalt().trim().isBlank())
                .toList();
    }
}
