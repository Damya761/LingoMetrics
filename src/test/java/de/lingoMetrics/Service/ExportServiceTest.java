package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTest {

    @Test
    void exportToRtf_shouldCreateValidRtfFile(@TempDir Path tempDir) throws IOException {
        // Arrange
        ServiceManager serviceManager = ServiceManager.createDefault();
        String text = "Hallo Welt. Das ist ein Test mit Füllwörtern wie 'aber' und 'bloß' und Umlauten: ä, ö, ü.";
        ServiceManager.AnalysisRequest request = new ServiceManager.AnalysisRequest(text, "Artikel", true, true);
        ServiceManager.AnalysisResult result = serviceManager.analyse(request);
        Document document = result.getDocument();

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
}
