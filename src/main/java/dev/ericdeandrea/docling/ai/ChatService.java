package dev.ericdeandrea.docling.ai;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.runtime.aiservice.ChatEvent;
import io.smallrye.mutiny.Multi;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

@ApplicationScoped
@RegisterAiService
interface ChatService {

    @SystemMessage("""
        Use the following context to answer the user's question.
        If the answer is not in the context, say you don't have enough information to answer.""")
    Multi<ChatEvent> chat(@MemoryId UUID conversationId, @UserMessage String message);
}
