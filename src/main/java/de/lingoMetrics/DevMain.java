package de.lingoMetrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.lingoMetrics.Models.AnalysisResult;
import de.lingoMetrics.Service.ServiceManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//Autor: Simon Ortlieb
public class DevMain {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: DevMain <input-file> [output-file]");
            System.err.println("  input-file:  Textdatei, eine Zeile pro Text");
            System.err.println("  output-file: Ziel-JSON (optional, default: stdout)");
            System.exit(1);
        }

        // Texte einlesen — eine Zeile pro Text, Leerzeilen überspringen
        List<String> texte = Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8)
                .stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        System.err.println("Lade Services...");
        ApplicationContext context = new ApplicationContext();
        ServiceManager serviceManager = context.getServiceManager();
        System.err.println("Analysiere " + texte.size() + " Texte...");

        // Analyse
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode results = mapper.createArrayNode();

        for (int i = 0; i < texte.size(); i++) {
            String text = texte.get(i);
            try {
                ServiceManager.AnalysisRequest request =
                        new ServiceManager.AnalysisRequest(text, null, false, false);
                AnalysisResult result = serviceManager.analyse(request);
                results.add(toJson(mapper, result, text));
            } catch (Exception e) {
                // Fehlerhaften Text überspringen, aber protokollieren
                ObjectNode error = mapper.createObjectNode();
                error.put("index", i);
                error.put("text_preview", text.substring(0, Math.min(50, text.length())));
                error.put("error", e.getMessage());
                results.add(error);
            }

            if ((i + 1) % 100 == 0) {
                System.err.println((i + 1) + " / " + texte.size() + " fertig");
            }
        }

        // Ausgabe
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(results);

        if (args.length >= 2) {
            Files.writeString(Path.of(args[1]), json, StandardCharsets.UTF_8);
            System.err.println("Ergebnisse gespeichert in: " + args[1]);
        } else {
            System.out.println(json);
        }
    }

    private static ObjectNode toJson(ObjectMapper mapper, AnalysisResult r, String text) {
        ObjectNode node = mapper.createObjectNode();
        node.put("text_preview", text.substring(0, Math.min(80, text.length())));
        node.put("absatzAnzahl",            r.absatzAnzahl());
        node.put("satzAnzahl",              r.satzAnzahl());
        node.put("wortAnzahl",              r.wortAnzahl());
        node.put("mittlereSatzlaenge",      r.mittlereSatzlaenge());
        node.put("satzlaengenunterschied",  r.satzlaengenunterschied());
        node.put("wortlaengenverteilung",   r.wortlaengenverteilung());
        node.put("funktionswoerterAnteil",  r.funktionswoerterAnteil());
        node.put("fuellwoerterAnteil",      r.fuellwoerterAnteil());
        node.put("typeTokenRatio",          r.typeTokenRatio());
        node.put("lesbarkeitsindex",        r.lesbarkeitsindex());
        node.put("mittleresSentiment",      r.mittleresSentiment());
        node.put("hapaxLegomena",           r.hapaxLegomena());
        node.put("adjektivVerbQuotient",    r.adjektivVerbQuotient());
        node.put("mittlereKonkretheit",     r.mittlereKonkretheit());
        return node;
    }
}