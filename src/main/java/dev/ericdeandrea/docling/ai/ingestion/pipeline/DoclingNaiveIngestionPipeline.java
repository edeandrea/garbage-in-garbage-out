package dev.ericdeandrea.docling.ai.ingestion.pipeline;

import java.nio.file.Path;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.langchain4j.EmbeddingStoreName;

import dev.ericdeandrea.docling.ai.ingestion.chunking.NaiveChunker;
import dev.ericdeandrea.docling.ai.ingestion.extraction.DoclingExtractor;
import dev.ericdeandrea.docling.model.Mode;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

@ApplicationScoped
class DoclingNaiveIngestionPipeline implements IngestionPipeline {

    private final DoclingExtractor doclingExtractor;
    private final NaiveChunker naiveChunker;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> store;

    DoclingNaiveIngestionPipeline(
            DoclingExtractor doclingExtractor,
            NaiveChunker naiveChunker,
            EmbeddingModel embeddingModel,
            @EmbeddingStoreName("docling-naive") EmbeddingStore<TextSegment> store) {
        this.doclingExtractor = doclingExtractor;
        this.naiveChunker = naiveChunker;
        this.embeddingModel = embeddingModel;
        this.store = store;
    }

    @Override
    public Mode mode() {
        return Mode.DOCLING_NAIVE_CHUNK;
    }

    @Override
    public String collectionName() {
        return Mode.DOCLING_NAIVE_CHUNK.storeName();
    }

    @Override
    public List<TextSegment> processAndStore(Path documentPath) {
        var result = doclingExtractor.extract(documentPath);
        var segments = naiveChunker.chunk(result, Mode.DOCLING_NAIVE_CHUNK);

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
