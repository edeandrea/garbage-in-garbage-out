package dev.ericdeandrea.docling.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.arc.Arc;
import io.quarkus.test.junit.QuarkusTest;

import dev.ericdeandrea.docling.model.ChatResponseEvent.ChunksRetrievedEvent;
import dev.ericdeandrea.docling.model.Mode;

@QuarkusTest
class PlantedQuestionsValidationIT {

    private static final String QUESTION_TABLE2 =
        "What does Table 2 show, and what network architecture won overall?";

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
    void modeARetrievesChunksWithoutPageMetadata() {
        var chunks = retrieveChunks(Mode.NAIVE, QUESTION_TABLE2);

        assertThat(chunks).isNotNull();
        assertThat(chunks.chunks())
            .isNotEmpty()
            .allSatisfy(chunk -> {
                assertThat(chunk.metadata().pageNumber())
                    .as("Mode A has no page metadata")
                    .isNull();
                assertThat(chunk.metadata().elementType())
                    .as("Mode A has no element type metadata")
                    .isNull();
            });
    }

    @Test
    void modeBRetrievesChunksWithPageMetadata() {
        var chunks = retrieveChunks(Mode.DOCLING_NAIVE_CHUNK, QUESTION_TABLE2);

        assertThat(chunks).isNotNull();
        assertThat(chunks.chunks()).isNotEmpty();

        assertThat(chunks.chunks())
            .anySatisfy(chunk ->
                assertThat(chunk.metadata().pageNumber())
                    .as("Mode B should have page metadata on at least some chunks")
                    .isNotNull()
            );
    }

    @Test
    void modeCRetrievesChunksWithRichMetadata() {
        var chunks = retrieveChunks(Mode.DOCLING_HYBRID_CHUNK, QUESTION_TABLE2);

        assertThat(chunks).isNotNull();
        assertThat(chunks.chunks()).isNotEmpty();

        assertThat(chunks.chunks())
            .anySatisfy(chunk ->
                assertThat(chunk.metadata().pageNumber())
                    .as("Mode C should have page metadata")
                    .isNotNull()
            );
    }

    @Test
    void allModesReturnChunksForTable2Question() {
        for (var mode : Mode.values()) {
            var chunks = retrieveChunks(mode, QUESTION_TABLE2);

            assertThat(chunks)
                .as("Mode %s should return chunks", mode)
                .isNotNull();
            assertThat(chunks.chunks())
                .as("Mode %s should return non-empty chunks", mode)
                .isNotEmpty();
        }
    }

    private ChunksRetrievedEvent retrieveChunks(Mode mode, String question) {
        var events = assistantService.chat(mode, UUID.randomUUID(), question)
            .collect()
            .asList()
            .await()
            .indefinitely();

        return events.stream()
            .filter(ChunksRetrievedEvent.class::isInstance)
            .map(ChunksRetrievedEvent.class::cast)
            .findFirst()
            .orElse(null);
    }
}
