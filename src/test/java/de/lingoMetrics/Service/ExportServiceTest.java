package de.lingoMetrics.Service;

import de.lingoMetrics.ApplicationContext;
import de.lingoMetrics.Models.AnalysisResult;
import de.lingoMetrics.Models.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTest {

    @Test
    void exportToRtf_shouldCreateValidRtfFile(@TempDir Path tempDir) throws IOException {
        // Arrange
        ServiceManager serviceManager = new ApplicationContext().getServiceManager();
        String text = "Hallo Welt. Das ist ein Test mit Füllwörtern wie 'aber' und 'bloß' und Umlauten: ä, ö, ü.";
        ServiceManager.AnalysisRequest request = new ServiceManager.AnalysisRequest(text, "Artikel", true, true);
        AnalysisResult result = serviceManager.analyse(request);
        Document document = result.document();

        File outputFile = tempDir.resolve("test_export.rtf").toFile();

        // Act
        ExportService.exportToRtf(document, result, text, outputFile);

        // Assert
        assertTrue(outputFile.exists());
        String content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);

        // Check RTF markers
        assertTrue(content.startsWith("{\\rtf1"));
        assertTrue(content.trim().endsWith("}"));

        // Check metadata & metrics presence
        assertTrue(content.contains("LingoMetrics"));
        assertTrue(content.contains("Verglichen mit Textstil"));
        assertTrue(content.contains("Artikel"));

        // Check that German characters are escaped using RTF Unicode escaping
        // ä is Unicode code point 228
        assertTrue(content.contains("\\u228?"));
        // ö is Unicode code point 246
        assertTrue(content.contains("\\u246?"));
        // ü is Unicode code point 252
        assertTrue(content.contains("\\u252?"));
    }

    @Test
    void exportToRtf_schreibtVergleichsDaten_wennComparisonAktivIst(@TempDir Path tempDir) throws IOException {
        // Arrange
        // Wir simulieren ein Ergebnis, bei dem ein Vergleich mit "Mails" stattgefunden hat
        AnalysisResult result = new AnalysisResult(
                new Document(),
                "Mails",
                true,
                true,
                85,             // score
                10, 5, null, 0.1, 15.0, 3.5, 0.4, 0.05, 0.5, 50.0, 0.0, 2, 0.8, 0.6,
                85,
                "Gut",
                List.of("Zu viele Füllwörter.", "Sätze zu lang.")
        );

        File outputFile = tempDir.resolve("vergleich_export.rtf").toFile();
        String rawText = "Test Text.";

        // Act
        ExportService.exportToRtf(new Document(), result, rawText, outputFile);

        // Assert
        String content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);

        assertTrue(content.contains("Verglichen mit Textstil: \\b Mails\\b0"));
        assertTrue(content.contains("Score: \\b 85 / 100\\b0"));
        assertTrue(content.contains("Gesamtbewertung: \\b Gut\\b0"));
        assertTrue(content.contains("Hinweise zur Optimierung:"));
        assertTrue(content.contains("Zu viele F\\u252?llw\\u246?rter."));
    }
}
