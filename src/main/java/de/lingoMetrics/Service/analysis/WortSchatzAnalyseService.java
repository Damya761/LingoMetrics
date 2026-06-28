package de.lingoMetrics.Service.analysis;

import de.lingoMetrics.Enums.WortTyp;
import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Models.Wort;
import de.lingoMetrics.Repository.WordRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class WortSchatzAnalyseService {
    private final WordRepository wordRepository;

    public WortSchatzAnalyseService(WordRepository wordRepository){
        this.wordRepository = wordRepository;
    }

    public void analyze(Document document){
        funktionswoerterAnalyse(document);
        fuellwoerterAnalyse(document);
        wortTypAnalyse(document);
        sentimentAnalyse(document);
        haeufigkeitsAnalyse(document);
        konkretheitsAnalyse(document);
    }

    private void funktionswoerterAnalyse(Document document){
        List<Wort> normaleWoerter = filterNormaleWoerter(document);
        for(Wort wort : normaleWoerter){
            wort.setFunktionsWort(wordRepository.isFunktionswort(wort.getInhalt()));
        }

        long funktionswoerter = normaleWoerter.stream()
                .filter(Wort::isFunktionsWort)
                .count();

        document.setFunktionswoerterAnteil(calculateAnteil(funktionswoerter, normaleWoerter.size()));
    }

    private void wortTypAnalyse(Document document){
        int adjektive = 0;
        int verben = 0;
        for(Wort wort : document.getWoerter()){
            wort.setWortart(wordRepository.getWortTyp(wort.getInhalt()));
            if(wort.getWortart().equals(WortTyp.TYP_ADJEKTIV)){
                adjektive++;
            }
            if(wort.getWortart().equals(WortTyp.TYP_VERB)){
                verben++;
            }
        }
        document.setAdjektivVerbQuotient(verben == 0 ? 0.0 : (double) adjektive / verben);
    }

    private void sentimentAnalyse(Document document){
        List<Wort> normaleWoerter = filterNormaleWoerter(document);
        for(Wort wort : normaleWoerter){
            wort.setSentiment(wordRepository.getSentiment(wort.getInhalt()));
        }

        double mittleresSentiment = normaleWoerter.stream()
                .mapToDouble(Wort::getSentiment)
                .average()
                .orElse(0.0);

        document.setMittleresSentiment(mittleresSentiment);
    }

    private void fuellwoerterAnalyse(Document document){
        List<Wort> normaleWoerter = filterNormaleWoerter(document);
        for(Wort wort : normaleWoerter){
            wort.setFuellWort(wordRepository.isFuellwort(wort.getInhalt()));
        }

        long fuellwoerter = normaleWoerter.stream()
                .filter(Wort::isFuellWort)
                .count();

        document.setFuellwoerterAnteil(calculateAnteil(fuellwoerter, normaleWoerter.size()));
    }

    private void haeufigkeitsAnalyse(Document document) {
        List<Wort> normaleWoerter = filterNormaleWoerter(document); // statt getWoerter()
        HashMap<String, Integer> woerter = new HashMap<>();
        for (Wort wort : normaleWoerter) {
            woerter.compute(wort.getInhalt(), (k, anzahl) -> anzahl == null ? 1 : anzahl + 1);
        }
        for (Wort wort : normaleWoerter) {
            wort.setVorkommenInText(woerter.get(wort.getInhalt()));
        }
        double haeufigkeit = woerter.values().stream().filter(v -> v == 1).count();
        document.setTypeTokenRatio(haeufigkeit / normaleWoerter.size());
        document.setHapaxLegomena((int) haeufigkeit);
    }

    private List<Wort> filterNormaleWoerter(Document document) {
        if (document == null || document.getWoerter() == null) {
            return List.of();
        }

        return document.getWoerter().stream()
                .filter(Objects::nonNull)
                .filter(wort -> !wort.isSatzzeichen())
                .filter(wort -> wort.getInhalt() != null)
                .filter(wort -> !wort.getInhalt().trim().isEmpty())
                .toList();
    }

    private void konkretheitsAnalyse(Document document) {
        List<Wort> woerter = filterNormaleWoerter(document);

        if (woerter.isEmpty()) return;

        double summe = 0.0;
        for (Wort wort : woerter) {
            double k = wordRepository.getKonkretheit(wort.getInhalt());
            wort.setKonkretheit(k);
            summe += k;
        }

        document.setMittlereKonkretheit(summe / woerter.size());
    }

    private double calculateAnteil(long treffer, int gesamt) {
        if (gesamt == 0) {
            return 0.0;
        }

        return (double) treffer / gesamt;
    }

}
