package dev.ericdeandrea.docling.ai.ingestion.extraction;

import java.nio.file.Path;

public interface ExtractionStrategy {
    ExtractionResult extract(Path documentPath);
}
