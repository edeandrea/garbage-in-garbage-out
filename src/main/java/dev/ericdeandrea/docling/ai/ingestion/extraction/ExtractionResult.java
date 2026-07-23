package dev.ericdeandrea.docling.ai.ingestion.extraction;

import java.util.List;

import dev.langchain4j.data.document.Document;

public record ExtractionResult(
    Document document,
    List<ProvenanceEntry> provenance
) {

    public ExtractionResult(Document document) {
        this(document, List.of());
    }

    public boolean hasProvenance() {
        return !provenance.isEmpty();
    }
}
