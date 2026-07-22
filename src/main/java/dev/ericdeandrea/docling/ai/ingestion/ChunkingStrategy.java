package dev.ericdeandrea.docling.ai.ingestion;

import java.util.List;

import dev.ericdeandrea.docling.model.Mode;
import dev.langchain4j.data.segment.TextSegment;

interface ChunkingStrategy {
    List<TextSegment> chunk(ExtractionResult result, Mode mode);
}
