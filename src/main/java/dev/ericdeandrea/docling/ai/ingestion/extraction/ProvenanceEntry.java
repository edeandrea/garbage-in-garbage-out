package dev.ericdeandrea.docling.ai.ingestion.extraction;

public record ProvenanceEntry(
    int startChar,
    int endChar,
    Integer pageNumber,
    String elementType,
    String elementLabel
) {
}
