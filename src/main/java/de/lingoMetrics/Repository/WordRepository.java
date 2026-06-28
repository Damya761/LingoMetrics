package de.lingoMetrics.Repository;

import de.lingoMetrics.Enums.WortTyp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class WordRepository {

    private Set<String> funktionswoerter;
    private Set<String> fuellwoerter;
    private Set<String> adjektive;
    private Set<String> verben;
    private Map<String, Double> sentimentindex;
    private Map<String, Double> konkretheitsindex;

    public void load() throws IOException {
        funktionswoerter  = loadWordSet("/Datasets/Stopwords.csv");
        fuellwoerter      = loadWordSet("/Datasets/Fillwords.csv");
        adjektive         = loadWordSet("/Datasets/Adjektiv.csv");
        verben            = loadWordSet("/Datasets/Verb.csv");
        sentimentindex    = loadSentimentMap("/Datasets/sentiWS.csv");
        konkretheitsindex = loadKonkretheitsMap("/Datasets/konkretheitsindex.csv");
    }

    private Set<String> loadWordSet(String resourcePath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }

    private Map<String, Double> loadSentimentMap(String resourcePath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

            return reader.lines()
                    .skip(1)                           // Header überspringen
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(",", 2))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(
                            parts -> parts[0].trim(),
                            parts -> Double.parseDouble(parts[1].trim()),
                            (a, b) -> a                // Duplikate: ersten behalten
                    ));
        }
    }

    private Map<String, Double> loadKonkretheitsMap(String resourcePath) throws IOException {
        //  Konkretheit -> [0..1]
        try (InputStream is = getClass().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

            return reader.lines()
                    .skip(1)                           // Header: "Word, AbstConc, Arou, IMG, Val"
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length >= 2)
                    .collect(Collectors.toMap(
                            parts -> parts[0].trim().toLowerCase(),
                            parts -> {
                                double abstConc = Double.parseDouble(parts[1].trim());
                                return (8.0 - abstConc) / 6.0;
                            },
                            (a, b) -> a
                    ));
        }
    }

    // --- Lookups ---

    public boolean isFunktionswort(String wort) {
        return funktionswoerter.contains(wort);
    }

    public boolean isFuellwort(String wort) {
        return fuellwoerter.contains(wort);
    }

    public double getSentiment(String wort) {
        return sentimentindex.getOrDefault(wort, 0.0);
    }

    public WortTyp getWortTyp(String wort) {
        if (verben.contains(wort))    return WortTyp.TYP_VERB;
        if (adjektive.contains(wort)) return WortTyp.TYP_ADJEKTIV;
        return WortTyp.TYP_OTHER;
    }

    public double getKonkretheit(String wort) {
        return konkretheitsindex.getOrDefault(wort.toLowerCase(), 0.0);
    }
}