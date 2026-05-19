package de.lingoMetrics.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lingoMetrics.Enums.WortTyp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

public class WordRepository {
    private List<Word> funktionswoerter;
    private List<Word> fuellwoerter;
    private List<Word> sentimentindex;
    private List<Word> verben;
    private List<Word> adjektive;
    public void load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try(InputStream Adjektiv = getClass().getResourceAsStream("/Datasets/Adjektiv.json");
            InputStream Fillwords = getClass().getResourceAsStream("/Datasets/Fillwords.json");
            InputStream sentiWS = getClass().getResourceAsStream("/Datasets/sentiWS.json");
            InputStream Stopwords = getClass().getResourceAsStream("/Datasets/Stopwords.json");
            InputStream Verb = getClass().getResourceAsStream("/Datasets/Verb.json");){
            this.funktionswoerter = mapper.readValue(Stopwords, new TypeReference<List<Word>>(){});
            this.fuellwoerter = mapper.readValue(Fillwords, new TypeReference<List<Word>>(){});
            this.sentimentindex = mapper.readValue(sentiWS, new TypeReference<List<Word>>(){});
            this.adjektive = mapper.readValue(Adjektiv, new TypeReference<List<Word>>(){});
            this.verben = mapper.readValue(Verb, new TypeReference<List<Word>>(){});
        }
    }


    public boolean isFunktionswort(String wort){
        if(funktionswoerter.stream().anyMatch(w -> Objects.equals(w.getWort(), wort))){
            return true;
        }else{
            return false;
        }
    }

    public boolean isFuellwort(String wort){
        if(fuellwoerter.stream().anyMatch(w -> Objects.equals(w.getWort(), wort))){
            return true;
        }else{
            return false;
        }
    }

    public double getSentiment(String wort){
        List<Word> sentimentWort = sentimentindex.stream()
                .filter(w -> w.getWort().equals(wort))
                .toList();
        if(sentimentWort.isEmpty()){
            return 0.0;
        }
        else {
            return sentimentWort.getFirst().getValue();
        }
    }

    public WortTyp getWortTyp(String wort){
        if(verben.stream().anyMatch(w -> Objects.equals(w.getWort(), wort))){
            return WortTyp.TYP_VERB;
        }else if(adjektive.stream().anyMatch(w -> Objects.equals(w.getWort(), wort))){
            return WortTyp.TYP_ADJEKTIV;
        }else{
            return WortTyp.TYP_OTHER;
        }
    }

}
