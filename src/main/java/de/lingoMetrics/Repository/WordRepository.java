package de.lingoMetrics.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lingoMetrics.Enums.WortTyp;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class WordRepository {
    private List<DBword> funktionswoerter;
    private List<DBword> fuellwoerter;
    private List<DBword> sentimentindex;
    private List<DBword> verben;
    private List<DBword> adjektive;
    public void load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try(InputStream Adjektiv = getClass().getResourceAsStream("/Datasets/Adjektiv.json");
            InputStream Fillwords = getClass().getResourceAsStream("/Datasets/Fillwords.json");
            InputStream sentiWS = getClass().getResourceAsStream("/Datasets/sentiWS.json");
            InputStream Stopwords = getClass().getResourceAsStream("/Datasets/Stopwords.json");
            InputStream Verb = getClass().getResourceAsStream("/Datasets/Verb.json");){
            this.funktionswoerter = mapper.readValue(Stopwords, new TypeReference<List<DBword>>(){});
            this.fuellwoerter = mapper.readValue(Fillwords, new TypeReference<List<DBword>>(){});
            this.sentimentindex = mapper.readValue(sentiWS, new TypeReference<List<DBword>>(){});
            this.adjektive = mapper.readValue(Adjektiv, new TypeReference<List<DBword>>(){});
            this.verben = mapper.readValue(Verb, new TypeReference<List<DBword>>(){});
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
        List<DBword> sentimentWort = sentimentindex.stream()
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
