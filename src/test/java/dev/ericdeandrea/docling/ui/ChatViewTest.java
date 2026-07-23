package dev.ericdeandrea.docling.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.vaadin.browserless.quarkus.QuarkusBrowserlessTest;

import io.quarkus.test.junit.QuarkusTest;

import dev.ericdeandrea.docling.model.Mode;

// Tests the ChatView's panel toggle behavior — adding, removing, and
// managing the three mode panels (A, B, C) via the toolbar toggle buttons.
@QuarkusTest
class ChatViewTest extends QuarkusBrowserlessTest {

    // On initial load, only Mode A should be visible — the "cold open" for the demo.
    @Test
    void defaultsToModeAOnly() {
        var view = navigate(ChatView.class);

        assertThat(view.panels())
            .hasSize(1)
            .containsKey(Mode.NAIVE);
    }

    // Toggling a new mode adds its panel alongside the existing one.
    @Test
    void togglesModeBPanel() {
        var view = navigate(ChatView.class);

        view.toggleMode(Mode.DOCLING_NAIVE_CHUNK);

        assertThat(view.panels())
            .hasSize(2)
            .containsKeys(Mode.NAIVE, Mode.DOCLING_NAIVE_CHUNK);
    }

    // Toggling an already-active mode removes its panel.
    @Test
    void togglesOffExistingPanel() {
        var view = navigate(ChatView.class);

        view.toggleMode(Mode.NAIVE);

        assertThat(view.panels()).isEmpty();
    }

    // Double-toggling a mode adds then removes it — no duplicates.
    @Test
    void maxOnePanelPerType() {
        var view = navigate(ChatView.class);

        view.toggleMode(Mode.DOCLING_HYBRID_CHUNK);
        view.toggleMode(Mode.DOCLING_HYBRID_CHUNK);

        assertThat(view.panels())
            .hasSize(1)
            .containsKey(Mode.NAIVE)
            .doesNotContainKey(Mode.DOCLING_HYBRID_CHUNK);
    }

    // All three modes can be active simultaneously for side-by-side comparison.
    @Test
    void allThreePanelsCanBeActive() {
        var view = navigate(ChatView.class);

        view.toggleMode(Mode.DOCLING_NAIVE_CHUNK);
        view.toggleMode(Mode.DOCLING_HYBRID_CHUNK);

        assertThat(view.panels())
            .hasSize(3)
            .containsKeys(Mode.NAIVE, Mode.DOCLING_NAIVE_CHUNK, Mode.DOCLING_HYBRID_CHUNK);
    }

    // Toggling a mode off and back on creates a fresh panel — conversation
    // state from the previous panel is not carried over.
    @Test
    void panelStatePreservesAfterToggle() {
        var view = navigate(ChatView.class);

        var panelBefore = view.panels().get(Mode.NAIVE);

        view.toggleMode(Mode.NAIVE);
        view.toggleMode(Mode.NAIVE);

        assertThat(view.panels().get(Mode.NAIVE))
            .as("Re-toggling creates a new panel instance")
            .isNotSameAs(panelBefore);
    }
}
