package dev.ericdeandrea.docling.ai.ingestion.pipeline;

import java.nio.file.Path;
import java.util.List;

import dev.ericdeandrea.docling.model.Mode;
import dev.langchain4j.data.segment.TextSegment;

public interface IngestionPipeline {
    Mode mode();

    String collectionName();

    List<TextSegment> processAndStore(Path documentPath);
}
