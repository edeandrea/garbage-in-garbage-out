package dev.ericdeandrea.docling.chat;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ChatAppTest {
    @Inject
    ChatApp chatApp;

    @Test
    void appStarts() {
        assertThat(chatApp).isNotNull();
    }
}
