package de.lingoMetrics.Service;

import de.lingoMetrics.Enums.WortTyp;
import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Models.Wort;
import de.lingoMetrics.Repository.WordRepository;

import java.util.HashMap;


public class WortSchatzAnalyseService {
    public final WordRepository wordRepository;

    public WortSchatzAnalyseService(WordRepository wordRepository){
        this.wordRepository = wordRepository;
    }

    public void Analyze(Document document){
        funktionswoerterAnalyse(document);
        fuellwoerterAnalyse(document);
        wortTypAnalyse(document);
        sentimentAnalyse(document);
        haeufigkeitsAnalyse(document);
    }

    private void funktionswoerterAnalyse(Document document){
        for(Wort wort : document.getWoerter()){
            wort.setFunktionsWort(wordRepository.isFunktionswort(wort.getInhalt()));
        }
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
        for(Wort wort : document.getWoerter()){
            wort.setSentiment(wordRepository.getSentiment(wort.getInhalt()));
        }
    }

    private void fuellwoerterAnalyse(Document document){
        for(Wort wort : document.getWoerter()){
            wort.setFuellWort(wordRepository.isFuellwort(wort.getInhalt()));
        }
    }

    private void haeufigkeitsAnalyse(Document document){
        HashMap<String, Integer> woerter = new HashMap<>();
        for(Wort wort : document.getWoerter()){
            woerter.compute(wort.getInhalt(), (k, anzahl) -> anzahl == null ? 1 : anzahl + 1);
        }
        double haeufigkeit = woerter.values()
                .stream()
                .filter(v -> v == 1)
                .count();
        document.setTypeTokenRatio(haeufigkeit / document.getWoerter().size());
        document.setHapaxLegomena((int) haeufigkeit);
    }

}
