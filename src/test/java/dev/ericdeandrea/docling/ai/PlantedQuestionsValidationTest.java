package dev.ericdeandrea.docling.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import dev.ericdeandrea.docling.model.ChatResponseEvent;
import dev.ericdeandrea.docling.model.ChatResponseEvent.ChunksRetrievedEvent;
import dev.ericdeandrea.docling.model.Mode;
import io.quarkus.arc.Arc;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PlantedQuestionsValidationTest {

    private static final String QUESTION_TABLE2 = "What does Table 2 show, and what network architecture won overall?";
    private static final String QUESTION_MAP_DELTA = "By how many mAP points does YOLOv5x outperform Faster R-CNN overall?";

    @Inject
    AssistantService assistantService;

    @BeforeEach
    void activateSessionContext() {
        Arc.container().sessionContext().activate();
    }

    @AfterEach
    void deactivateSessionContext() {
        Arc.container().sessionContext().deactivate();
    }

    @Test
    void modeARetrievesGarbledChunksForTable2Question() {
        var chunks = retrieveChunks(Mode.NAIVE, QUESTION_TABLE2);

        assertThat(chunks).isNotNull();
        assertThat(chunks.chunks()).isNotEmpty();

        var allText = chunks.chunks().stream()
            .map(c -> c.text())
            .reduce("", (a, b) -> a + " " + b);

        assertThat(allText)
            .as("Mode A chunks should not have pipe separators (table structure lost)")
            .doesNotContain(" | ");

        assertThat(chunks.chunks())
            .allSatisfy(chunk -> {
                assertThat(chunk.metadata().pageNumber())
                    .as("Mode A has no page metadata")
                    .isNull();
            });
    }

    @Test
    void modeBRetrievesCleanChunksButMissingColumnHeaders() {
        var chunks = retrieveChunks(Mode.DOCLING_NAIVE_CHUNK, QUESTION_TABLE2);

        assertThat(chunks).isNotNull();
        assertThat(chunks.chunks()).isNotEmpty();

        var chunkWithValues = chunks.chunks().stream()
            .filter(c -> c.text().contains("76.8") || c.text().contains("73.4"))
            .findFirst();

        assertThat(chunkWithValues)
            .as("Mode B should retrieve chunks containing Table 2 values")
            .isPresent();

        chunkWithValues.ifPresent(chunk ->
            assertThat(chunk.text())
                .as("Mode B chunk has values but not column headers")
                .doesNotContain("YOLOv5x6")
                .doesNotContain("FRCNN")
        );
    }

    @Test
    void modeCRetrievesSelfDescribingChunks() {
        var chunks = retrieveChunks(Mode.DOCLING_HYBRID_CHUNK, QUESTION_MAP_DELTA);

        assertThat(chunks).isNotNull();
        assertThat(chunks.chunks()).isNotEmpty();

        var chunkWithValues = chunks.chunks().stream()
            .filter(c -> c.text().contains("76.8") && c.text().contains("73.4"))
            .findFirst();

        assertThat(chunkWithValues)
            .as("Mode C should retrieve a chunk with both 76.8 and 73.4 together with model names")
            .isPresent();

        chunkWithValues.ifPresent(chunk ->
            assertThat(chunk.text())
                .as("Mode C chunk has self-describing values with model names inline")
                .contains("YOLO")
                .contains("FRCNN")
        );
    }

    @Test
    void modeBFragmentsTable2WhileModeCKeepsItIntact() {
        var modeBChunks = retrieveChunks(Mode.DOCLING_NAIVE_CHUNK, QUESTION_MAP_DELTA);
        var modeCChunks = retrieveChunks(Mode.DOCLING_HYBRID_CHUNK, QUESTION_MAP_DELTA);

        var modeBHasBothValues = modeBChunks.chunks().stream()
            .anyMatch(c -> c.text().contains("76.8") && c.text().contains("73.4")
                && c.text().contains("YOLO") && c.text().contains("FRCNN"));

        var modeCHasBothValues = modeCChunks.chunks().stream()
            .anyMatch(c -> c.text().contains("76.8") && c.text().contains("73.4")
                && c.text().contains("YOLO") && c.text().contains("FRCNN"));

        assertThat(modeBHasBothValues)
            .as("Mode B should NOT have values + column names in a single chunk")
            .isFalse();

        assertThat(modeCHasBothValues)
            .as("Mode C should have values + column names in a single chunk")
            .isTrue();
    }

    private ChunksRetrievedEvent retrieveChunks(Mode mode, String question) {
        var events = assistantService.chat(mode, UUID.randomUUID(), question)
            .collect().asList()
            .await().indefinitely();

        return events.stream()
            .filter(ChunksRetrievedEvent.class::isInstance)
            .map(ChunksRetrievedEvent.class::cast)
            .findFirst()
            .orElse(null);
    }
}
