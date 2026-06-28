package de.lingoMetrics.Service;

import de.lingoMetrics.Enums.Metrik;
import de.lingoMetrics.Enums.WortTyp;
import de.lingoMetrics.Models.AnalysisResult;
import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Models.Satz;
import de.lingoMetrics.Models.Wort;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

public class ExportService {

    public static void exportToRtf(Document doc,
                                   AnalysisResult result,
                                   String rawText,
                                   File targetFile) throws IOException {
        StringBuilder rtf = new StringBuilder();
        appendHeader(rtf);

        // Titel
        rtf.append("\\fs32\\b LingoMetrics Textanalyse-Bericht\\b0\\fs22\\par\\par\n");

        // Metadaten
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        rtf.append("Datum: \\b ").append(LocalDateTime.now().format(formatter)).append("\\b0\\par\n");
        if (result.comparison()) {
            rtf.append("Verglichen mit Textstil: \\b ")
                    .append(escapeRtf(result.stiltyp())).append("\\b0\\par\n");
            rtf.append("Score: \\b ").append(result.score()).append(" / 100\\b0\\par\n");
            rtf.append("Gesamtbewertung: \\b ")
                    .append(escapeRtf(result.gesamtBewertung())).append("\\b0\\par\n");
        }
        rtf.append("\\par\n");

        // Metriktabelle
        rtf.append("\\fs24\\b Analyse-Ergebnisse:\\b0\\fs22\\par\n");
        rtf.append("- Abs").append(uni(228)).append("tze: \\b ").append(result.absatzAnzahl()).append("\\b0\\par\n");
        rtf.append("- S").append(uni(228)).append("tze: \\b ").append(result.satzAnzahl()).append("\\b0\\par\n");
        rtf.append("- W").append(uni(246)).append("rter: \\b ").append(result.wortAnzahl()).append("\\b0\\par\n");
        rtf.append("- Wortl").append(uni(228)).append("ngenverteilung: \\b ").append(fmt(result.wortlaengenverteilung())).append("\\b0\\par\n");
        rtf.append("- Mittlere Satzl").append(uni(228)).append("nge: \\b ").append(fmt(result.mittlereSatzlaenge())).append(" W").append(uni(246)).append("rter\\b0\\par\n");
        rtf.append("- Satzl").append(uni(228)).append("ngenunterschied: \\b ").append(fmt(result.satzlaengenunterschied())).append("\\b0\\par\n");
        rtf.append("- Funktionsw").append(uni(246)).append("rter-Anteil: \\b ").append(fmtPercent(result.funktionswoerterAnteil())).append("\\b0\\par\n");
        rtf.append("- F").append(uni(252)).append("llwort-Anteil: \\b ").append(fmtPercent(result.fuellwoerterAnteil())).append("\\b0\\par\n");
        rtf.append("- Type-Token-Ratio: \\b ").append(fmt(result.typeTokenRatio())).append("\\b0\\par\n");
        rtf.append("- Lesbarkeitsindex (LIX): \\b ").append(fmt(result.lesbarkeitsindex())).append("\\b0\\par\n");
        rtf.append("- Mittleres Sentiment: \\b ").append(fmt(result.mittleresSentiment())).append("\\b0\\par\n");
        rtf.append("- Hapax Legomena: \\b ").append(result.hapaxLegomena()).append("\\b0\\par\n");
        rtf.append("- Adjektiv-Verb-Quotient: \\b ").append(fmt(result.adjektivVerbQuotient())).append("\\b0\\par\n");
        rtf.append("- Mittlere Konkretheit: \\b ").append(fmt(result.mittlereKonkretheit())).append("\\b0\\par\n");
        rtf.append("\\par\n");

        // Hinweise
        if (result.comparison() && !result.hinweise().isEmpty()) {
            rtf.append("\\fs24\\b Hinweise zur Optimierung:\\b0\\fs22\\par\n");
            for (String hinweis : result.hinweise()) {
                rtf.append("- ").append(escapeRtf(hinweis)).append("\\par\n");
            }
            rtf.append("\\par\n");
        }

        // Legende
        appendLegende(rtf);

        // Annotierter Text
        rtf.append("\\fs24\\b Analysierter Text mit Hervorhebungen:\\b0\\fs22\\par\\par\n");
        appendAnnotatedText(rtf, doc, rawText);

        rtf.append("\n}");
        Files.writeString(targetFile.toPath(), rtf.toString(), StandardCharsets.UTF_8);
    }

    public static void exportMetrikToRtf(Document doc,
                                         Metrik metrik,
                                         File targetFile) throws IOException {
        StringBuilder rtf = new StringBuilder();
        appendHeader(rtf);

        rtf.append("\\fs28\\b LingoMetrics – Einzelanalyse: ")
                .append(escapeRtf(metrik.name())).append("\\b0\\fs22\\par\\par\n");

        if (metrik == Metrik.Alle) {
            for (Metrik m : Metrik.values()) {
                if (m == Metrik.Alle) continue;
                appendMetrikAbschnitt(rtf, doc, m);
                rtf.append("\\par\n");
            }
        } else {
            appendMetrikAbschnitt(rtf, doc, metrik);
        }


        rtf.append("\n}");
        Files.writeString(targetFile.toPath(), rtf.toString(), StandardCharsets.UTF_8);
    }

    private static void appendAnnotatedText(StringBuilder rtf, Document doc, String rawText) {
        int lastEnd = 0;
        int wordIndex = 0;
        List<Wort> words = doc.getWoerter();
        Matcher matcher = TextStrukturService.TOKEN_PATTERN.matcher(rawText);

        while (matcher.find() && wordIndex < words.size()) {
            rtf.append(formatWhitespaceRtf(rawText.substring(lastEnd, matcher.start())));
            rtf.append(formatWortRtfGesamt(words.get(wordIndex++)));
            lastEnd = matcher.end();
        }

        if (lastEnd < rawText.length()) {
            rtf.append(formatWhitespaceRtf(rawText.substring(lastEnd)));
        }
    }

    private static String formatWortRtfGesamt(Wort wort) {
        if (wort.isSatzzeichen()) return escapeRtf(wort.getInhalt());

        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();

        if (wort.isFuellWort()) {
            prefix.append(RtfHighlight.FUELLWORT.open());
            suffix.insert(0, RtfHighlight.FUELLWORT.close());
        }

        if (wort.getSentiment() < 0) {
            prefix.append("\\cf1\\b ");
            suffix.insert(0, "\\b0\\cf0 ");
        } else if (wort.getSentiment() > 0) {
            prefix.append("\\cf2\\b ");
            suffix.insert(0, "\\b0\\cf0 ");
        } else {
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

        return prefix + escapeRtf(wort.getInhalt()) + suffix;
    }

    private static void appendLegende(StringBuilder rtf) {
        rtf.append("\\fs24\\b Farbcode-Legende:\\b0\\fs22\\par\n");
        rtf.append("- {").append(RtfHighlight.FUELLWORT.open()).append("F").append(uni(252)).append("llw").append(uni(246)).append("rter").append(RtfHighlight.FUELLWORT.close()).append("} (Gelber Hintergrund)\\par\n");
        rtf.append("- {\\cf5 Funktionsw").append(uni(246)).append("rter} (Grauer Text)\\par\n");
        rtf.append("- {\\cf3\\i Verben} (Blauer, kursiver Text)\\par\n");
        rtf.append("- {\\cf4\\ul Adjektive} (Gr").append(uni(252)).append("ner, unterstrichener Text)\\par\n");
        rtf.append("- {\\cf2\\b Positives Sentiment} (Gr").append(uni(252)).append("ner, fetter Text)\\par\n");
        rtf.append("- {\\cf1\\b Negatives Sentiment} (Roter, fetter Text)\\par\\par\n");
    }

    private static void appendMetrikAbschnitt(StringBuilder rtf, Document doc, Metrik metrik) {
        MetrikInfo info = METRIK_INFO.get(metrik);

        // Titel
        rtf.append("\\fs24\\b ").append(escapeRtf(metrik.name())).append("\\b0\\fs22\\par\n");

        // Beschreibung
        if (info != null && !info.beschreibung().isBlank()) {
            rtf.append("\\i ").append(escapeRtf(info.beschreibung())).append("\\i0\\par\n");
        }

        // Legende
        if (info != null && !info.legende().isBlank()) {
            rtf.append("\\fs20 Legende: \\b ")
                    .append(escapeRtf(info.legende()))
                    .append("\\b0\\fs22\\par\n");
        }

        rtf.append("\\par\n");

        // Annotierter Text
        rtf.append(convertMetrik(doc, metrik));
        rtf.append("\\par\n");
    }



    private static String convertMetrik(Document doc, Metrik metrik) {
        return switch (metrik) {
            case Füllwörteranalyse    -> füllwoerter(doc);
            case SentimentAnalyse     -> sentiment(doc);
            case AdjektivVerbQuotient -> adjektivVerb(doc);
            case Konkretheitsindex    -> konkretheit(doc);
            case Funktionswörteranalyse -> funktionswoerter(doc);
            case Interpunktation      -> interpunktation(doc);
            case MittlereSatzlänge    -> mittlereSatzlaenge(doc);
            case Satzlängenunterschied -> satzlaengenunterschied(doc);
            case Wortlängenverteilung -> wortlaengen(doc);
            case TypeTokenRatio       -> typeTokenRatio(doc);
            case Lesbarkeitsindex     -> lesbarkeitsindex(doc);
            case Alle                 -> ""; // wird im Aufrufer behandelt
        };
    }

    private static String füllwoerter(Document doc) {
        StringBuilder sb = new StringBuilder();
        for (Wort wort : doc.getWoerter()) {
            if (wort.isFuellWort()) {
                sb.append(RtfHighlight.RED.apply(escapeRtf(wort.getInhalt())));
            } else {
                sb.append(escapeRtf(wort.getInhalt()));
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String funktionswoerter(Document doc) {
        StringBuilder sb = new StringBuilder();
        for (Wort wort : doc.getWoerter()) {
            if (wort.isFunktionsWort()) {
                sb.append(RtfHighlight.YELLOW.apply(escapeRtf(wort.getInhalt())));
            } else {
                sb.append(escapeRtf(wort.getInhalt()));
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String sentiment(Document doc) {
        StringBuilder sb = new StringBuilder();
        for (Wort wort : doc.getWoerter()) {
            double s = wort.getSentiment();
            if (s == 0.0) {
                sb.append(escapeRtf(wort.getInhalt()));
            } else if (s < -0.5) {
                sb.append(RtfHighlight.SCALE_ORANGE1.apply(escapeRtf(wort.getInhalt())));
            } else if (s < -0.15) {
                sb.append(RtfHighlight.SCALE_ORANGE2.apply(escapeRtf(wort.getInhalt())));
            } else if (s < 0.15) {
                sb.append(RtfHighlight.SCALE_NEUTRAL.apply(escapeRtf(wort.getInhalt())));
            } else if (s < 0.5) {
                sb.append(RtfHighlight.SCALE_BLUE1.apply(escapeRtf(wort.getInhalt())));
            } else {
                sb.append(RtfHighlight.SCALE_BLUE2.apply(escapeRtf(wort.getInhalt())));
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String konkretheit(Document doc) {
        StringBuilder sb = new StringBuilder();
        for (Wort wort : doc.getWoerter()) {
            if (wort.isSatzzeichen()) {
                sb.append(escapeRtf(wort.getInhalt()));
                continue;
            }
            double k = wort.getKonkretheit();
            if (k == 0.0) {
                sb.append(escapeRtf(wort.getInhalt()));
            } else if (k < 0.3) {
                sb.append(RtfHighlight.SCALE_ORANGE1.apply(escapeRtf(wort.getInhalt())));
            } else if (k < 0.45) {
                sb.append(RtfHighlight.SCALE_ORANGE2.apply(escapeRtf(wort.getInhalt())));
            } else if (k < 0.55) {
                sb.append(RtfHighlight.SCALE_NEUTRAL.apply(escapeRtf(wort.getInhalt())));
            } else if (k < 0.7) {
                sb.append(RtfHighlight.SCALE_BLUE1.apply(escapeRtf(wort.getInhalt())));
            } else {
                sb.append(RtfHighlight.SCALE_BLUE2.apply(escapeRtf(wort.getInhalt())));
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String adjektivVerb(Document doc) {
        StringBuilder sb = new StringBuilder();
        for (Wort wort : doc.getWoerter()) {
            if (wort.getWortart() == WortTyp.TYP_ADJEKTIV) {
                sb.append(RtfHighlight.MAGENTA.apply(escapeRtf(wort.getInhalt())));
            } else if (wort.getWortart() == WortTyp.TYP_VERB) {
                sb.append(RtfHighlight.YELLOW.apply(escapeRtf(wort.getInhalt())));
            } else {
                sb.append(escapeRtf(wort.getInhalt()));
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String interpunktation(Document doc) {
        StringBuilder sb = new StringBuilder();
        for (Wort wort : doc.getWoerter()) {
            String inhalt = escapeRtf(wort.getInhalt());
            if (!wort.isSatzzeichen()) {
                sb.append(inhalt);
            } else {
                sb.append(switch (wort.getInhalt()) {
                    case "."  -> RtfHighlight.MAGENTA.apply(inhalt);
                    case ","  -> RtfHighlight.RED.apply(inhalt);
                    case "!"  -> RtfHighlight.CYAN.apply(inhalt);
                    case "?"  -> RtfHighlight.GREEN.apply(inhalt);
                    case ";"  -> RtfHighlight.ORANGE.apply(inhalt);
                    case ":"  -> RtfHighlight.PURPLE.apply(inhalt);
                    default   -> RtfHighlight.YELLOW.apply(inhalt);
                });
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String mittlereSatzlaenge(Document doc) {
        StringBuilder sb = new StringBuilder();
        double mLaenge = doc.getMittlereSatzlaenge();
        if (mLaenge == 0) return "";
        for (Satz satz : doc.getSaetze()) {
            double ratio = satz.getWoerterAnzahl() / mLaenge;
            String inhalt = satzZuRtf(satz);
            sb.append(skaliert(ratio, inhalt)).append("\\par ");
        }
        return sb.toString();
    }

    private static String satzlaengenunterschied(Document doc) {
        StringBuilder sb = new StringBuilder();
        double mUnterschied = doc.getSatzlaengenunterschied();
        if (mUnterschied == 0) return "";
        int vorherige = 0;
        for (Satz satz : doc.getSaetze()) {
            double ratio = Math.abs(satz.getWoerterAnzahl() - vorherige) / mUnterschied;
            String inhalt = satzZuRtf(satz);
            sb.append(skaliert(ratio, inhalt)).append("\\par ");
            vorherige = satz.getWoerterAnzahl();
        }
        return sb.toString();
    }

    private static String wortlaengen(Document doc) {
        StringBuilder sb = new StringBuilder();
        double mLaenge = doc.getWortlaengenverteilung();
        if (mLaenge == 0) return "";
        for (Wort wort : doc.getWoerter()) {
            if (wort.isSatzzeichen()) {
                sb.append(escapeRtf(wort.getInhalt()));
            } else {
                double ratio = wort.getLaenge() / mLaenge;
                sb.append(skaliert(ratio, escapeRtf(wort.getInhalt())));
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String typeTokenRatio(Document doc) {
        StringBuilder sb = new StringBuilder();
        for (Wort wort : doc.getWoerter()) {
            if (wort.isSatzzeichen()) {
                sb.append(escapeRtf(wort.getInhalt()));
            } else {
                int v = wort.getVorkommenInText();
                String inhalt = escapeRtf(wort.getInhalt());
                if (v == 1) {
                    sb.append(RtfHighlight.SCALE_ORANGE1.apply(inhalt));
                } else if (v <= 3) {
                    sb.append(RtfHighlight.SCALE_NEUTRAL.apply(inhalt));
                } else {
                    sb.append(RtfHighlight.SCALE_BLUE2.apply(inhalt));
                }
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String lesbarkeitsindex(Document doc) {
        // LIX basiert auf langen Wörtern (>6 Zeichen) — diese hervorheben
        StringBuilder sb = new StringBuilder();
        for (Wort wort : doc.getWoerter()) {
            if (wort.isSatzzeichen()) {
                sb.append(escapeRtf(wort.getInhalt()));
            } else if (wort.getLaenge() > 6) {
                sb.append(RtfHighlight.SCALE_ORANGE1.apply(escapeRtf(wort.getInhalt())));
            } else {
                sb.append(escapeRtf(wort.getInhalt()));
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    /** Farbskala blau→orange abhängig vom Verhältnis zum Durchschnitt. */
    private static String skaliert(double ratio, String inhalt) {
        if (ratio < 0.5)       return RtfHighlight.SCALE_ORANGE1.apply(inhalt);
        else if (ratio < 0.85) return RtfHighlight.SCALE_ORANGE2.apply(inhalt);
        else if (ratio < 1.15) return RtfHighlight.SCALE_NEUTRAL.apply(inhalt);
        else if (ratio < 1.5)  return RtfHighlight.SCALE_BLUE1.apply(inhalt);
        else                   return RtfHighlight.SCALE_BLUE2.apply(inhalt);
    }

    private static String satzZuRtf(Satz satz) {
        StringBuilder sb = new StringBuilder();
        for (Wort wort : satz.getWoerter()) {
            sb.append(escapeRtf(wort.getInhalt())).append(" ");
        }
        return sb.toString().trim();
    }

    private static void appendHeader(StringBuilder rtf) {
        rtf.append("{\\rtf1\\ansi\\deff0\n");
        rtf.append("{\\fonttbl{\\f0\\fnil\\fcharset0 Arial;}}\n");
        rtf.append("{\\colortbl ;\n");
        // Gesamtbericht-Farben (cf1–cf5)
        rtf.append("\\red180\\green0\\blue0;");       // 1  - Negatives Sentiment (rot)
        rtf.append("\\red0\\green120\\blue0;");        // 2  - Positives Sentiment (grün)
        rtf.append("\\red0\\green50\\blue180;");       // 3  - Verben (blau)
        rtf.append("\\red0\\green100\\blue80;");       // 4  - Adjektive (blaugrün)
        rtf.append("\\red110\\green110\\blue110;");    // 5  - Funktionswörter (grau)
        // Einzelmetrik-Farben (highlight 6–12)
        rtf.append("\\red255\\green240\\blue150;");    // 6  - Füllwörter (hellgelb)
        rtf.append("\\red255\\green255\\blue0;");      // 7  - YELLOW
        rtf.append("\\red0\\green200\\blue0;");        // 8  - GREEN
        rtf.append("\\red220\\green0\\blue0;");        // 9  - RED
        rtf.append("\\red0\\green220\\blue220;");      // 10 - CYAN
        rtf.append("\\red200\\green0\\blue200;");      // 11 - MAGENTA
        rtf.append("\\red255\\green140\\blue0;");      // 12 - ORANGE
        rtf.append("\\red140\\green0\\blue220;");      // 13 - PURPLE
        rtf.append("\\red0\\green100\\blue255;");      // 14 - SCALE_BLUE2
        rtf.append("\\red100\\green180\\blue255;");    // 15 - SCALE_BLUE1
        rtf.append("\\red255\\green255\\blue180;");    // 16 - SCALE_NEUTRAL
        rtf.append("\\red255\\green180\\blue80;");     // 17 - SCALE_ORANGE2
        rtf.append("\\red255\\green100\\blue0;");      // 18 - SCALE_ORANGE1
        rtf.append("}\n");
        rtf.append("\\f0\\fs22\n");
    }

    private static String formatWhitespaceRtf(String whitespace) {
        if (whitespace == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < whitespace.length(); i++) {
            char c = whitespace.charAt(i);
            if (c == '\n') {
                sb.append("\\par ");
            } else if (c == '\r') {
                if (i + 1 < whitespace.length() && whitespace.charAt(i + 1) == '\n') continue;
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
            if      (c == '\\') sb.append("\\\\");
            else if (c == '{')  sb.append("\\{");
            else if (c == '}')  sb.append("\\}");
            else if (c > 127)   sb.append("\\u").append((int) c).append('?');
            else                sb.append(c);
        }
        return sb.toString();
    }

    /** RTF Unicode-Escape für Sonderzeichen. */
    private static String uni(int codePoint) {
        return "\\u" + codePoint + "?";
    }

    private static String fmt(double value) {
        return String.format(Locale.GERMANY, "%.4f", value);
    }

    private static String fmtPercent(double value) {
        return String.format(Locale.GERMANY, "%.2f%%", value * 100);
    }

    public enum RtfHighlight {

        FUELLWORT(6),


        YELLOW (7),
        GREEN  (8),
        RED    (9),
        CYAN   (10),
        MAGENTA(11),
        ORANGE (12),
        PURPLE (13),

        // Farbskala blau → orange
        SCALE_BLUE2   (14),
        SCALE_BLUE1   (15),
        SCALE_NEUTRAL (16),
        SCALE_ORANGE2 (17),
        SCALE_ORANGE1 (18);

        private final int index;

        RtfHighlight(int index) {
            this.index = index;
        }

        public String apply(String text) {
            return open() + text + close();
        }

        public String open() {
            return "\\highlight" + index + " ";
        }

        public String close() {
            return "\\highlight0 ";
        }

    }

    private record MetrikInfo(String beschreibung, String legende) {}

    private static final Map<Metrik, MetrikInfo> METRIK_INFO = Map.ofEntries(
            Map.entry(Metrik.Füllwörteranalyse, new MetrikInfo(
                    "Misst den Anteil an Füllwörtern im Text. Füllwörter tragen wenig zur " +
                            "inhaltlichen Aussage bei und können den Text abschwächen.",
                    "Rot: Füllwort"
            )),
            Map.entry(Metrik.SentimentAnalyse, new MetrikInfo(
                    "Bewertet die emotionale Färbung einzelner Wörter auf Basis des deutschen " +
                            "Sentiment-Lexikons SentiWS.",
                    "Orange (stark negativ) → Gelb (neutral) → Blau (stark positiv) — Ohne Markierung: kein Sentiment-Eintrag"
            )),
            Map.entry(Metrik.TypeTokenRatio, new MetrikInfo(
                    "Verhältnis von einzigartigen Wörtern zur Gesamtwortanzahl. " +
                            "Ein hoher Wert deutet auf einen abwechslungsreichen Wortschatz hin.",
                    "Orange: kommt nur einmal vor — Gelb: 2–3 Mal — Blau: 4 Mal oder häufiger"
            )),
            Map.entry(Metrik.AdjektivVerbQuotient, new MetrikInfo(
                    "Verhältnis von Adjektiven zu Verben. Hohe Werte deuten auf einen nominalen, " +
                            "beschreibenden Stil hin; niedrige auf einen dynamischen Stil.",
                    "Lila: Adjektiv — Gelb: Verb — Ohne Markierung: andere Wortarten"
            )),
            Map.entry(Metrik.Konkretheitsindex, new MetrikInfo(
                    "Bewertet wie konkret oder abstrakt einzelne Wörter sind, basierend auf dem " +
                            "Datensatz von Köper & Schulte im Walde (2016).",
                    "Orange: abstrakt (< 0.3) — Gelb: neutral — Blau: konkret (> 0.7) — Ohne Markierung: kein Eintrag"
            )),
            Map.entry(Metrik.Funktionswörteranalyse, new MetrikInfo(
                    "Misst den Anteil an Funktionswörtern wie Artikel, Präpositionen und Konjunktionen. " +
                            "Ein hoher Anteil ist typisch für komplexe Satzkonstruktionen.",
                    "Gelb: Funktionswort"
            )),
            Map.entry(Metrik.Interpunktation, new MetrikInfo(
                    "Zeigt die Verteilung der Satzzeichen im Text. Auffällige Häufungen einzelner " +
                            "Satzzeichen können auf stilistische Muster hinweisen.",
                    "Lila: Punkt — Rot: Komma — Cyan: Ausrufezeichen — Grün: Fragezeichen — " +
                            "Orange: Semikolon — Magenta: Doppelpunkt — Gelb: sonstige"
            )),
            Map.entry(Metrik.MittlereSatzlänge, new MetrikInfo(
                    "Vergleicht jeden Satz mit der durchschnittlichen Satzlänge des Textes. " +
                            "Sehr kurze oder sehr lange Sätze werden hervorgehoben.",
                    "Orange: deutlich kürzer als Durchschnitt — Gelb: leicht kürzer — " +
                            "Grau: durchschnittlich — Hellblau: leicht länger — Blau: deutlich länger"
            )),
            Map.entry(Metrik.Satzlängenunterschied, new MetrikInfo(
                    "Misst wie stark sich aufeinanderfolgende Sätze in ihrer Länge unterscheiden. " +
                            "Hohe Variation sorgt für Lesefluss; zu wenig wirkt monoton.",
                    "Orange: kaum Variation zum Vorgängersatz — Blau: starke Variation"
            )),
            Map.entry(Metrik.Lesbarkeitsindex, new MetrikInfo(
                    "Berechnet den LIX-Wert basierend auf Satzlänge und dem Anteil langer Wörter. " +
                            "Niedrige Werte bedeuten leichtere Lesbarkeit.",
                    "Orange: Wörter mit mehr als 6 Zeichen (erhöhen den LIX-Wert)"
            )),
            Map.entry(Metrik.Wortlängenverteilung, new MetrikInfo(
                    "Zeigt die Verteilung der Wortlängen relativ zum Durchschnitt. " +
                            "Viele lange Wörter erhöhen die kognitive Last beim Lesen.",
                    "Orange: kürzer als Durchschnitt — Grau: durchschnittlich — Blau: länger als Durchschnitt"
            )),
            Map.entry(Metrik.Alle, new MetrikInfo(
                    "Gibt alle Metriken als aufeinanderfolgende Abschnitte aus.", ""
            ))
    );
}