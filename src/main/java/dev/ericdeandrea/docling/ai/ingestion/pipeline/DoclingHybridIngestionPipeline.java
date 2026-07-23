package dev.ericdeandrea.docling.ai.ingestion.pipeline;

import java.nio.file.Path;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.langchain4j.EmbeddingStoreName;

import dev.ericdeandrea.docling.ai.ingestion.extraction.DoclingExtractor;
import dev.ericdeandrea.docling.model.Mode;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

@ApplicationScoped
class DoclingHybridIngestionPipeline implements IngestionPipeline {

    private final DoclingExtractor doclingExtractor;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> store;

    DoclingHybridIngestionPipeline(
            DoclingExtractor doclingExtractor,
            EmbeddingModel embeddingModel,
            @EmbeddingStoreName("docling-hybrid") EmbeddingStore<TextSegment> store) {
        this.doclingExtractor = doclingExtractor;
        this.embeddingModel = embeddingModel;
        this.store = store;
    }

    @Override
    public Mode mode() {
        return Mode.DOCLING_HYBRID_CHUNK;
    }

    @Override
    public String collectionName() {
        return Mode.DOCLING_HYBRID_CHUNK.storeName();
    }

    @Override
    public List<TextSegment> processAndStore(Path documentPath) {
        var segments = doclingExtractor.extractAndChunk(documentPath);

        EmbeddingStoreIngestor.builder()
            .embeddingStore(store)
            .embeddingModel(embeddingModel)
            .build()
            .ingest(segments.stream()
                .map(s -> Document.from(s.text(), s.metadata()))
                .toList());

        return segments;
    }
}
