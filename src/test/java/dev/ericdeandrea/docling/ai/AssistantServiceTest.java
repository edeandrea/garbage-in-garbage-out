package dev.ericdeandrea.docling.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import dev.ericdeandrea.docling.DoclingWiremockTestProfile;
import dev.ericdeandrea.docling.model.ChatResponseEvent;
import dev.ericdeandrea.docling.model.ChatResponseEvent.ChunksRetrievedEvent;
import dev.ericdeandrea.docling.model.ChatResponseEvent.CompletedEvent;
import dev.ericdeandrea.docling.model.ChatResponseEvent.TokenEvent;
import dev.ericdeandrea.docling.model.Mode;

@QuarkusTest
@TestProfile(DoclingWiremockTestProfile.class)
class AssistantServiceTest {

    @Inject
    AssistantService assistantService;

    @Test
    void producesTokenAndCompletedEvents() {
        var events = assistantService.chat(Mode.NAIVE, UUID.randomUUID(), "What is DocLayNet?")
            .collect().asList()
            .await().atMost(Duration.ofMinutes(5));

        assertThat(events)
            .isNotEmpty()
            .anySatisfy(event -> assertThat(event).isInstanceOf(TokenEvent.class))
            .last().isInstanceOf(CompletedEvent.class);
    }

    @Test
    void producesChunksRetrievedEvents() {
        var events = assistantService.chat(Mode.DOCLING_HYBRID_CHUNK, UUID.randomUUID(), "What does Table 2 show?")
            .collect().asList()
            .await().atMost(Duration.ofMinutes(5));

        assertThat(events)
            .anySatisfy(event -> assertThat(event).isInstanceOf(ChunksRetrievedEvent.class));

        var chunksEvent = events.stream()
            .filter(ChunksRetrievedEvent.class::isInstance)
            .map(ChunksRetrievedEvent.class::cast)
            .findFirst()
            .orElseThrow();

        assertThat(chunksEvent.chunks()).isNotEmpty();
    }

    @Test
    void switchesModesBetweenRequests() {
        var modeAEvents = assistantService.chat(Mode.NAIVE, UUID.randomUUID(), "What is DocLayNet?")
            .collect().asList()
            .await().atMost(Duration.ofMinutes(5));

        var modeAChunks = modeAEvents.stream()
            .filter(ChunksRetrievedEvent.class::isInstance)
            .map(ChunksRetrievedEvent.class::cast)
            .findFirst()
            .orElseThrow();

        assertThat(modeAChunks.chunks())
            .isNotEmpty()
            .allSatisfy(chunk -> assertThat(chunk.metadata().mode()).isEqualTo(Mode.NAIVE));

        var modeCEvents = assistantService.chat(Mode.DOCLING_HYBRID_CHUNK, UUID.randomUUID(), "What is DocLayNet?")
            .collect().asList()
            .await().atMost(Duration.ofMinutes(5));

        var modeCChunks = modeCEvents.stream()
            .filter(ChunksRetrievedEvent.class::isInstance)
            .map(ChunksRetrievedEvent.class::cast)
            .findFirst()
            .orElseThrow();

        assertThat(modeCChunks.chunks())
            .isNotEmpty()
            .allSatisfy(chunk -> assertThat(chunk.metadata().mode()).isEqualTo(Mode.DOCLING_HYBRID_CHUNK));
    }

    @Test
    void noLangChain4jTypesInEvents() {
        var events = assistantService.chat(Mode.NAIVE, UUID.randomUUID(), "What is DocLayNet?")
            .collect().asList()
            .await().atMost(Duration.ofMinutes(5));

        assertThat(events)
            .isNotEmpty()
            .allSatisfy(event -> assertThat(event).isInstanceOf(ChatResponseEvent.class));
    }
}
