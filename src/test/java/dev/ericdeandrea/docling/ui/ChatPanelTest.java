package dev.ericdeandrea.docling.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInput.SubmitEvent;
import com.vaadin.flow.component.messages.MessageList;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import com.vaadin.browserless.quarkus.QuarkusBrowserlessTest;

import io.smallrye.mutiny.Multi;

import dev.ericdeandrea.docling.ai.AssistantService;
import dev.ericdeandrea.docling.model.ChatResponseEvent.ChunksRetrievedEvent;
import dev.ericdeandrea.docling.model.ChatResponseEvent.CompletedEvent;
import dev.ericdeandrea.docling.model.ChatResponseEvent.TokenEvent;
import dev.ericdeandrea.docling.model.ChunkMetadata;
import dev.ericdeandrea.docling.model.Mode;
import dev.ericdeandrea.docling.model.RetrievedChunk;

// Tests the ChatPanel's interactive behavior with a mocked AssistantService.
// The mock returns a deterministic stream of TokenEvent, ChunksRetrievedEvent, and
// CompletedEvent — no real LLM or Qdrant needed. This lets us verify the UI logic
// (color bubbles, token accumulation, chunk grid, highlight-on-click) in isolation.
@QuarkusTest
class ChatPanelTest extends QuarkusBrowserlessTest {

    @InjectMock
    AssistantService assistantService;

    private static final List<RetrievedChunk> MOCK_CHUNKS = List.of(
        new RetrievedChunk("chunk one text", new ChunkMetadata(1, "PARAGRAPH", null, Mode.NAIVE, 0.95, Instant.now())),
        new RetrievedChunk("chunk two text", new ChunkMetadata(2, "TABLE", "Table 2", Mode.NAIVE, 0.87, Instant.now()))
    );

    @BeforeEach
    void setupMock() {
        when(this.assistantService.chat(eq(Mode.NAIVE), any(), any()))
            .thenReturn(Multi.createFrom().items(
                new TokenEvent("Hello "),
                new TokenEvent("world!"),
                new ChunksRetrievedEvent(MOCK_CHUNKS),
                new CompletedEvent()
            ));
    }

    // Each assistant response gets a rotating color index (round % 9) so
    // consecutive conversations are visually distinguishable in the message list.
    @Test
    void assistantMessageGetsColorIndex() {
        var view = navigate(ChatView.class);
        var panel = view.panels().get(Mode.NAIVE);

        fireSubmit(panel, "test question");

        var items = find(MessageList.class, panel).single().getItems();

        var assistantItem = items.stream()
            .filter(item -> Mode.NAIVE.displayLabel().equals(item.getUserName()))
            .findFirst()
            .orElseThrow();

        assertThat(assistantItem.getUserColorIndex())
            .as("First round color index should be 1 %% 9 = 1")
            .isEqualTo(1);
    }

    // When ChunksRetrievedEvent arrives, the retrieved chunks grid should
    // be populated with one row per chunk.
    @Test
    void chunksGridPopulatedAfterResponse() {
        var view = navigate(ChatView.class);
        var panel = view.panels().get(Mode.NAIVE);

        fireSubmit(panel, "test question");

        @SuppressWarnings("unchecked")
        var grid = (Grid<ChunkRow>) find(Grid.class, panel).single();

        assertThat(grid.getGenericDataView().getItems().toList())
            .hasSize(2);
    }

    // Streaming tokens should accumulate into the assistant message text
    // as they arrive, building up the full response progressively.
    @Test
    void assistantMessageAccumulatesTokens() {
        var view = navigate(ChatView.class);
        var panel = view.panels().get(Mode.NAIVE);

        fireSubmit(panel, "test question");

        var items = find(MessageList.class, panel).single().getItems();

        var assistantItem = items.stream()
            .filter(item -> Mode.NAIVE.displayLabel().equals(item.getUserName()))
            .findFirst()
            .orElseThrow();

        assertThat(assistantItem.getText()).isEqualTo("Hello world!");
    }

    // Verify the color index increments with each conversation round so
    // the user can visually distinguish which response belongs to which question.
    @Test
    void secondRoundGetsNextColorIndex() {
        var view = navigate(ChatView.class);
        var panel = view.panels().get(Mode.NAIVE);

        fireSubmit(panel, "first question");
        fireSubmit(panel, "second question");

        var items = find(MessageList.class, panel).single().getItems();

        var assistantItems = items.stream()
            .filter(item -> Mode.NAIVE.displayLabel().equals(item.getUserName()))
            .toList();

        assertThat(assistantItems).hasSize(2);
        assertThat(assistantItems.get(0).getUserColorIndex()).isEqualTo(1);
        assertThat(assistantItems.get(1).getUserColorIndex()).isEqualTo(2);
    }

    // Clicking a row in the chunks grid should add the "highlighted" CSS class
    // to the assistant message from that round, linking the chunk back to its response.
    @Test
    void clickingChunkRowHighlightsAssistantMessage() {
        var view = navigate(ChatView.class);
        var panel = view.panels().get(Mode.NAIVE);

        fireSubmit(panel, "test question");

        var messageList = find(MessageList.class, panel).single();
        var assistantItem = messageList.getItems().stream()
            .filter(item -> Mode.NAIVE.displayLabel().equals(item.getUserName()))
            .findFirst()
            .orElseThrow();

        assertThat(assistantItem.hasClassName("highlighted"))
            .as("Before click, no highlight")
            .isFalse();

        @SuppressWarnings("unchecked")
        var grid = (Grid<ChunkRow>) find(Grid.class, panel).single();
        test(grid).clickRow(0);

        assertThat(assistantItem.hasClassName("highlighted"))
            .as("After clicking chunk row, assistant message should be highlighted")
            .isTrue();
    }

    // When multiple rounds have been submitted, clicking chunks from different
    // rounds should move the highlight — only one assistant message is highlighted
    // at a time, and the previous highlight is removed.
    @Test
    void clickingDifferentRoundMovesHighlight() {
        when(this.assistantService.chat(eq(Mode.NAIVE), any(), eq("first")))
            .thenReturn(Multi.createFrom().items(
                new TokenEvent("Answer one"),
                new ChunksRetrievedEvent(MOCK_CHUNKS),
                new CompletedEvent()
            ));

        when(this.assistantService.chat(eq(Mode.NAIVE), any(), eq("second")))
            .thenReturn(Multi.createFrom().items(
                new TokenEvent("Answer two"),
                new ChunksRetrievedEvent(List.of(
                    new RetrievedChunk("different chunk", new ChunkMetadata(3, "PARAGRAPH", null, Mode.NAIVE, 0.9, Instant.now()))
                )),
                new CompletedEvent()
            ));

        var view = navigate(ChatView.class);
        var panel = view.panels().get(Mode.NAIVE);

        fireSubmit(panel, "first");
        fireSubmit(panel, "second");

        var messageList = find(MessageList.class, panel).single();
        var assistantItems = messageList.getItems().stream()
            .filter(item -> Mode.NAIVE.displayLabel().equals(item.getUserName()))
            .toList();

        assertThat(assistantItems).hasSize(2);

        @SuppressWarnings("unchecked")
        var grid = (Grid<ChunkRow>) find(Grid.class, panel).single();

        // Click a row from round 1 (chunks are prepended, so round 2 chunks are at indices 0, round 1 at index 1+)
        // Round 2 has 1 chunk (index 0), round 1 has 2 chunks (indices 1, 2)
        test(grid).clickRow(1);

        assertThat(assistantItems.get(0).hasClassName("highlighted"))
            .as("Round 1 message should be highlighted")
            .isTrue();

        assertThat(assistantItems.get(1).hasClassName("highlighted"))
            .as("Round 2 message should NOT be highlighted")
            .isFalse();

        // Now click a round 2 chunk
        test(grid).clickRow(0);

        assertThat(assistantItems.get(1).hasClassName("highlighted"))
            .as("Round 2 message should now be highlighted")
            .isTrue();

        assertThat(assistantItems.get(0).hasClassName("highlighted"))
            .as("Round 1 message should no longer be highlighted")
            .isFalse();
    }

    private void fireSubmit(ChatPanel panel, String message) {
        var messageInput = find(MessageInput.class, panel).single();
        ComponentUtil.fireEvent(messageInput, new SubmitEvent(messageInput, false, message));
    }
}
