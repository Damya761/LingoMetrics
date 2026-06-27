package de.lingoMetrics.Service;

import de.lingoMetrics.Models.AnalysisResult;
import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Models.Wort;
import de.lingoMetrics.Enums.WortTyp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;

public class ExportService {

    private static String rtfUnicode(int charCode) {
        return "\\" + "u" + charCode + "?";
    }

    public static void exportToRtf(Document doc, AnalysisResult result, String rawText, File targetFile) throws IOException {
        StringBuilder rtf = new StringBuilder();

        // RTF Header
        rtf.append("{\\rtf1\\ansi\\deff0\n");
        rtf.append("{\\fonttbl{\\f0\\fnil\\fcharset0 Arial;}}\n");
        // Color table:
        // Index 1: Red (\red180\green0\blue0) - Negative Sentiment
        // Index 2: Green (\red0\green120\blue0) - Positive Sentiment
        // Index 3: Blue (\red0\green50\blue180) - Verbs
        // Index 4: Dark Green / Teal (\red0\green100\blue80) - Adjectives
        // Index 5: Gray (\red110\green110\blue110) - Function Words
        // Index 6: Light Yellow highlight (\red255\green240\blue150) - Filler Words
        rtf.append("{\\colortbl ;\\red180\\green0\\blue0;\\red0\\green120\\blue0;\\red0\\green50\\blue180;\\red0\\green100\\blue80;\\red110\\green110\\blue110;\\red255\\green240\\blue150;}\n");
        rtf.append("\\f0\\fs22\n");

        // Title
        rtf.append("\\fs32\\b LingoMetrics Textanalyse-Bericht\\b0\\fs22\\par\\par\n");

        // Metadata
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        rtf.append("Datum: \\b ").append(LocalDateTime.now().format(formatter)).append("\\b0\\par\n");
        if (result.comparison()) {
            rtf.append("Verglichen mit Textstil: \\b ").append(escapeRtf(result.stiltyp())).append("\\b0\\par\n");
            rtf.append("Score: \\b ").append(result.score()).append(" / 100\\b0\\par\n");
            rtf.append("Gesamtbewertung: \\b ").append(escapeRtf(result.gesamtBewertung())).append("\\b0\\par\n");
        }
        rtf.append("\\par\n");

        // Metrics Table
        rtf.append("\\fs24\\b Analyse-Ergebnisse:\\b0\\fs22\\par\n");
        rtf.append("- Abs" + rtfUnicode(228) + "tze: \\b ").append(result.absatzAnzahl()).append("\\b0\\par\n");
        rtf.append("- S" + rtfUnicode(228) + "tze: \\b ").append(result.satzAnzahl()).append("\\b0\\par\n");
        rtf.append("- W" + rtfUnicode(246) + "rter: \\b ").append(result.wortAnzahl()).append("\\b0\\par\n");
        rtf.append("- Wortl" + rtfUnicode(228) + "ngenverteilung: \\b ").append(formatDouble(result.wortlaengenverteilung())).append("\\b0\\par\n");
        rtf.append("- Mittlere Satzl" + rtfUnicode(228) + "nge: \\b ").append(formatDouble(result.mittlereSatzlaenge())).append(" W" + rtfUnicode(246) + "rter\\b0\\par\n");
        rtf.append("- Satzl" + rtfUnicode(228) + "ngenunterschied: \\b ").append(formatDouble(result.satzlaengenunterschied())).append("\\b0\\par\n");
        rtf.append("- Funktionsw" + rtfUnicode(246) + "rter-Anteil: \\b ").append(formatPercent(result.funktionswoerterAnteil())).append("\\b0\\par\n");
        rtf.append("- F" + rtfUnicode(252) + "llwort-Anteil: \\b ").append(formatPercent(result.fuellwoerterAnteil())).append("\\b0\\par\n");
        rtf.append("- Type-Token-Ratio (Wortvielfalt): \\b ").append(formatDouble(result.typeTokenRatio())).append("\\b0\\par\n");
        rtf.append("- Lesbarkeitsindex (LIX): \\b ").append(formatDouble(result.lesbarkeitsindex())).append("\\b0\\par\n");
        rtf.append("- Mittleres Sentiment: \\b ").append(formatDouble(result.mittleresSentiment())).append("\\b0\\par\n");
        rtf.append("- Hapax Legomena: \\b ").append(result.hapaxLegomena()).append("\\b0\\par\n");
        rtf.append("- Adjektiv-Verb-Quotient: \\b ").append(formatDouble(result.adjektivVerbQuotient())).append("\\b0\\par\n");
        rtf.append("\\par\n");

        // Hints
        if (result.comparison() && !result.hinweise().isEmpty()) {
            rtf.append("\\fs24\\b Hinweise zur Optimierung:\\b0\\fs22\\par\n");
            for (String hinweis : result.hinweise()) {
                rtf.append("- ").append(escapeRtf(hinweis)).append("\\par\n");
            }
            rtf.append("\\par\n");
        }

        // Color Legend
        rtf.append("\\fs24\\b Farbcode-Legende f" + rtfUnicode(252) + "r Markierungen:\\b0\\fs22\\par\n");
        rtf.append("- {\\highlight6 F" + rtfUnicode(252) + "llw" + rtfUnicode(246) + "rter} (Gelber Hintergrund)\\par\n");
        rtf.append("- {\\cf5 Funktionsw" + rtfUnicode(246) + "rter} (Grauer Text)\\par\n");
        rtf.append("- {\\cf3\\i Verben} (Blauer, kursiver Text)\\par\n");
        rtf.append("- {\\cf4\\ul Adjektive} (Gr" + rtfUnicode(252) + "ner, unterstrichener Text)\\par\n");
        rtf.append("- {\\cf2\\b Positives Sentiment} (Gr" + rtfUnicode(252) + "ner, fetter Text)\\par\n");
        rtf.append("- {\\cf1\\b Negatives Sentiment} (Roter, fetter Text)\\par\\par\n");

        // Analyzed Text Section
        rtf.append("\\fs24\\b Analysierter Text mit Hervorhebungen:\\b0\\fs22\\par\\par\n");

        // Reconstruction Loop
        int lastEnd = 0;
        int wordIndex = 0;
        java.util.List<Wort> words = doc.getWoerter();
        Matcher matcher = TextStrukturService.TOKEN_PATTERN.matcher(rawText);

        while (matcher.find() && wordIndex < words.size()) {
            // Write whitespace before token
            String whitespace = rawText.substring(lastEnd, matcher.start());
            rtf.append(formatWhitespaceRtf(whitespace));

            // Format and write word token
            Wort wort = words.get(wordIndex++);
            rtf.append(formatWortRtf(wort));

            lastEnd = matcher.end();
        }

        // Write remaining whitespace/text
        if (lastEnd < rawText.length()) {
            rtf.append(formatWhitespaceRtf(rawText.substring(lastEnd)));
        }

        rtf.append("\n}");

        Files.writeString(targetFile.toPath(), rtf.toString(), StandardCharsets.UTF_8);
    }

    private static String formatWortRtf(Wort wort) {
        if (wort.isSatzzeichen()) {
            return escapeRtf(wort.getInhalt());
        }

        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();

        // Check Füllwort (yellow background highlight)
        if (wort.isFuellWort()) {
            prefix.append("\\highlight6 ");
            suffix.insert(0, "\\highlight0 ");
        }

        // Check Sentiment
        if (wort.getSentiment() < 0) {
            prefix.append("\\cf1\\b ");
            suffix.insert(0, "\\b0\\cf0 ");
        } else if (wort.getSentiment() > 0) {
            prefix.append("\\cf2\\b ");
            suffix.insert(0, "\\b0\\cf0 ");
        } else {
            // Wortart coloring
            if (wort.getWortart() == WortTyp.TYP_VERB) {
                prefix.append("\\cf3\\i ");
                suffix.insert(0, "\\i0\\cf0 ");
            } else if (wort.getWortart() == WortTyp.TYP_ADJEKTIV) {
                prefix.append("\\cf4\\ul ");
                suffix.insert(0, "\\ulnone\\cf0 ");
            } else if (wort.isFunktionsWort()) {
                prefix.append("\\cf5 ");
                suffix.insert(0, "\\cf0 ");
            }
        }

        return prefix.toString() + escapeRtf(wort.getInhalt()) + suffix.toString();
    }

    private static String formatWhitespaceRtf(String whitespace) {
        if (whitespace == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < whitespace.length(); i++) {
            char c = whitespace.charAt(i);
            if (c == '\n') {
                sb.append("\\par ");
            } else if (c == '\r') {
                if (i + 1 < whitespace.length() && whitespace.charAt(i + 1) == '\n') {
                    continue;
                }
                sb.append("\\par ");
            } else if (c == '\t') {
                sb.append("\\tab ");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String escapeRtf(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '{') {
                sb.append("\\{");
            } else if (c == '}') {
                sb.append("\\}");
            } else if (c > 127) {
                sb.append("\\" + "u").append((int) c).append('?');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String formatDouble(double value) {
        return String.format(Locale.GERMANY, "%.4f", value);
    }

    private static String formatPercent(double value) {
        return String.format(Locale.GERMANY, "%.2f%%", value * 100);
    }
}
