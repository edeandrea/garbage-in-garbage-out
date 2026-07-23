package dev.ericdeandrea.docling.ai.ingestion.chunking;

import java.util.List;

import dev.ericdeandrea.docling.ai.ingestion.extraction.ExtractionResult;
import dev.ericdeandrea.docling.model.Mode;
import dev.langchain4j.data.segment.TextSegment;

public interface ChunkingStrategy {
    List<TextSegment> chunk(ExtractionResult result, Mode mode);
}
