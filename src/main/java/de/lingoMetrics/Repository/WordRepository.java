package de.lingoMetrics.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lingoMetrics.Enums.WortTyp;

import java.io.File;
import java.io.IOException;
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
        File Adjektiv = new File("src/Datasets/Adjektiv.json");
        File Fillwords = new File("src/Datasets/Fillwords.json");
        File sentiWS = new File("src/Datasets/sentiWS.json");
        File Stopwords = new File("src/Datasets/Stopwords.json");
        File Verb = new File("src/Datasets/Verb.json");
        this.funktionswoerter = mapper.readValue(Stopwords, new TypeReference<List<Word>>(){});
        this.fuellwoerter = mapper.readValue(Stopwords, new TypeReference<List<Word>>(){});
        this.sentimentindex = mapper.readValue(Stopwords, new TypeReference<List<Word>>(){});
        this.adjektive = mapper.readValue(Stopwords, new TypeReference<List<Word>>(){});
        this.verben = mapper.readValue(Stopwords, new TypeReference<List<Word>>(){});
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
        return sentimentindex.stream()
                .filter(w -> w.getWort().equals(wort))
                .toList()
                .getFirst()
                .getValue();
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
