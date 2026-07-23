package dev.ericdeandrea.docling.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import dev.ericdeandrea.docling.model.Mode;

@QuarkusTest
class ChatServiceTest {

    @Inject
    ChatService chatService;

    @Inject
    CurrentMode currentMode;

    @Test
    void isInjectable() {
        assertThat(chatService).isNotNull();
    }

    @Test
    void chatStreamsEventsForEachMode() {
        Stream.of(Mode.values()).forEach(mode -> {
            this.currentMode.mode(mode);

            var events = this.chatService.chat(UUID.randomUUID(), "What is DocLayNet?")
                .collect().asList()
                .await().atMost(Duration.ofMinutes(5));

            assertThat(events)
                .as("Mode %s should produce chat events", mode)
                .isNotEmpty();
        });
    }

}
