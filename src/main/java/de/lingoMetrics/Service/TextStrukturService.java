package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Absatz;
import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Models.Satz;
import de.lingoMetrics.Models.Wort;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextStrukturService {

    //Das Pattern ist bewusst einfach gehalten und deckt die für unsere erste Analyseversion relevanten Grundfälle ab. Sonderfälle wie Abkürzungen, Gedankenstriche oder komplexe technische Schreibweisen können später erweitert werden, ohne die restliche Service-Struktur zu ändern.
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "[\\p{L}\\p{M}]+(?:[-'][\\p{L}\\p{M}]+)*|\\d+(?:[,.]\\d+)*|[.!?,;:()\"„“»«…-]"
    );

    public Document createDocument(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text must not be null or empty.");
        }

        Document document = new Document();
        document.setAbsaetze(new ArrayList<>());
        document.setSaetze(new ArrayList<>());
        document.setWoerter(new ArrayList<>());

        String[] absatzTexte = text.trim().split("\\R\\s*\\R");

        for (String absatzText : absatzTexte) {
            if (absatzText == null || absatzText.trim().isEmpty()) {
                continue;
            }

            Absatz absatz = createAbsatz(absatzText.trim(), document);

            if (!absatz.getSatz().isEmpty()) {
                document.addAbsatz(absatz);
            }
        }

        return document;
    }

    private Absatz createAbsatz(String absatzText, Document document) {
        Absatz absatz = new Absatz();
        absatz.setSaetze(new ArrayList<>());

        Satz aktuellerSatz = createEmptySatz();

        Matcher matcher = TOKEN_PATTERN.matcher(absatzText);

        while (matcher.find()) {
            String token = matcher.group();

            Wort wort = createWort(token);

            aktuellerSatz.addWort(wort);
            document.addWort(wort);

            if (!wort.isSatzzeichen()) {
                aktuellerSatz.setWoerterAnzahl(aktuellerSatz.getWoerterAnzahl() + 1);
            }

            aktuellerSatz.setLaenge(aktuellerSatz.getLaenge() + wort.getLaenge());

            if (isSatzEnde(token)) {
                addSatzIfNotEmpty(aktuellerSatz, absatz, document);
                aktuellerSatz = createEmptySatz();
            }
        }

        addSatzIfNotEmpty(aktuellerSatz, absatz, document);

        return absatz;
    }

    private Satz createEmptySatz() {
        Satz satz = new Satz();
        satz.setWoerter(new ArrayList<>());
        satz.setLaenge(0);
        satz.setWoerterAnzahl(0);
        return satz;
    }

    private Wort createWort(String inhalt) {
        Wort wort = new Wort();

        wort.setInhalt(inhalt);
        wort.setSatzzeichen(isSatzzeichen(inhalt));

        if (wort.isSatzzeichen()) {
            wort.setLaenge(0);
        } else {
            wort.setLaenge(inhalt.length());
        }

        return wort;
    }

    private void addSatzIfNotEmpty(Satz satz, Absatz absatz, Document document) {
        if (satz.getWoerter() == null || satz.getWoerter().isEmpty()) {
            return;
        }

        absatz.addSatz(satz);
        document.addSatz(satz);
    }

    private boolean isSatzEnde(String token) {
        return token.equals(".")
                || token.equals("!")
                || token.equals("?")
                || token.equals("…");
    }

    private boolean isSatzzeichen(String token) {
        return token.length() == 1 && ".,!?;:()\"„“»«…-".contains(token);
    }
}