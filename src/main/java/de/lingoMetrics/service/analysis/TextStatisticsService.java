package de.lingoMetrics.service.analysis;


import de.lingoMetrics.domain.Document;
import de.lingoMetrics.domain.Paragraph;
import de.lingoMetrics.domain.Sentence;
import de.lingoMetrics.domain.Word;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

// Vorläufige Implementation des TextStatisticsService

public class TextStatisticsService {

    public void analyze(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document must not be null.");
        }

        List<Paragraph> paragraphs = document.getParagraphs();
        List<Sentence> sentences = getAllSentences(paragraphs);
        List<Word> words = document.getWords();

        double averageSentenceLength = calculateAverageSentenceLength(sentences);
        double sentenceLengthDifference = calculateSentenceLengthDifference(sentences);
        double wordLengthDistribution = calculateAverageWordLength(words);
        Map<String, Long> punctuationProfile = calculatePunctuationProfile(words);
        int hapaxLegomena = calculateHapaxLegomena(words);
        double readabilityIndex = calculateReadabilityIndex(sentences, words);

        document.setAverageSentenceLength(averageSentenceLength);
        document.setSentenceLengthDifference(sentenceLengthDifference);
        document.setWordLengthDistribution(wordLengthDistribution);
        document.setPunctuationProfile(punctuationProfile);
        document.setHapaxLegomena(hapaxLegomena);
        document.setReadabilityIndex(readabilityIndex);
    }


    // Metrikberechnungen
    private double calculateAverageSentenceLength(List<Sentence> sentences) {
        if (sentences.isEmpty()) {
            return 0.0;
        }

        int totalWords = sentences.stream()
                .mapToInt(Sentence::getWordCount)
                .sum();

        return (double) totalWords / sentences.size();
    }


    private double calculateSentenceLengthDifference(List<Sentence> sentences) {
        if (sentences.size() < 2) {
            return 0.0;
        }

        int totalDifference = 0;

        for (int i = 1; i < sentences.size(); i++) {
            int previousSentenceLength = sentences.get(i - 1).getWordCount();
            int currentSentenceLength = sentences.get(i).getWordCount();

            totalDifference += Math.abs(currentSentenceLength - previousSentenceLength);
        }

        return (double) totalDifference / (sentences.size() - 1);
    }


    private double calculateAverageWordLength(List<Word> words) {
        List<Word> normalWords = filterNormalWords(words);

        if (normalWords.isEmpty()) {
            return 0.0;
        }

        int totalLength = normalWords.stream()
                .mapToInt(Word::getLength)
                .sum();

        return (double) totalLength / normalWords.size();
    }


    private Map<String, Long> calculatePunctuationProfile(List<Word> words) {
        if (words == null) {
            return Map.of();
        }

        Map<String, Long> frequencies = words.stream()
                .filter(Objects::nonNull)
                .filter(Word::isPunctuationMark)
                .map(Word::getContent)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(content -> !content.isBlank())
                .collect(Collectors.groupingBy(
                        punctuationMark -> punctuationMark,
                        TreeMap::new,
                        Collectors.counting()
                ));

        return frequencies;
    }


    private int calculateHapaxLegomena(List<Word> words) {
        List<Word> normalWords = filterNormalWords(words);

        if (normalWords.isEmpty()) {
            return 0;
        }

        Map<String, Long> wordFrequencies = normalWords.stream()
                .map(Word::getContent)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(content -> !content.isBlank())
                .map(content -> content.toLowerCase(Locale.GERMAN))
                .collect(Collectors.groupingBy(
                        word -> word,
                        Collectors.counting()
                ));

        return (int) wordFrequencies.values().stream()
                .filter(count -> count == 1)
                .count();
    }


     //LIX = (word count / sentence count) + (long words * 100 / word count)
    private double calculateReadabilityIndex(List<Sentence> sentences, List<Word> words) {
        List<Word> normalWords = filterNormalWords(words);

        int sentenceCount = sentences.size();
        int wordCount = normalWords.size();

        if (sentenceCount == 0 || wordCount == 0) {
            return 0.0;
        }

        long longWords = normalWords.stream()
                .filter(word -> word.getLength() > 6)
                .count();

        return ((double) wordCount / sentenceCount)
                + ((double) longWords * 100 / wordCount);
    }



    //Hilfsmethoden
    private List<Sentence> getAllSentences(List<Paragraph> paragraphs) {
        if (paragraphs == null) {
            return List.of();
        }
        return paragraphs.stream()
                .filter(Objects::nonNull)
                .filter(paragraph -> paragraph.getSentences() != null)
                .flatMap(paragraph -> paragraph.getSentences().stream())
                .filter(Objects::nonNull)
                .toList();
    }

    //Filtert die Wort Liste nach echten Wörtern, Satzzeichen, Null Werte und leerer Text wird ignoriert
    private List<Word> filterNormalWords(List<Word> words) {
        if (words == null) {
            return List.of();
        }

        return words.stream()
                .filter(Objects::nonNull)
                .filter(word -> !word.isPunctuationMark())
                .filter(word -> word.getContent() != null)
                .filter(word -> !word.getContent().trim().isBlank())
                .toList();
    }
}