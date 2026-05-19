package de.lingoMetrics.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WordTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testDeserializeFromPlainString() throws Exception {
        String json = "\"hallo\"";
        Word word = mapper.readValue(json, Word.class);

        assertEquals("hallo", word.getWort());
        assertEquals(0.0, word.getValue());
    }

    @Test
    void testDeserializeFromObject() throws Exception {
        String json = "{\"wort\":\"hallo\",\"value\":2.5}";
        Word word = mapper.readValue(json, Word.class);

        assertEquals("hallo", word.getWort());
        assertEquals(2.5, word.getValue());
    }
}