package dev.ericdeandrea.docling.ai.ingestion.chunking;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;

import dev.ericdeandrea.docling.config.DemoConfig;
import dev.ericdeandrea.docling.ai.ingestion.extraction.ExtractionResult;
import dev.ericdeandrea.docling.ai.ingestion.extraction.ProvenanceEntry;
import dev.ericdeandrea.docling.model.Mode;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.segment.TextSegment;

/**
 * Sentence-based document chunker used by Modes A and B.
 *
 * <p>Splits an extracted document into overlapping sentence-bounded segments using
 * {@link DocumentBySentenceSplitter}, then enriches each segment with:</p>
 * <ul>
 *   <li><strong>Surrounding context</strong> — the text of neighboring segments (2 before, 2 after)
 *       stored as {@code extended_content} metadata, so the embedding captures broader context
 *       than the segment alone.</li>
 *   <li><strong>Provenance metadata</strong> — if the {@link ExtractionResult} includes provenance
 *       (Docling extraction provides this; Tika does not), each segment is tagged with
 *       {@code page_number}, {@code element_type}, and {@code element_label} from the first
 *       overlapping provenance entry.</li>
 * </ul>
 *
 * <p>Chunk size ({@code maxTokens}) and overlap are configured via {@link DemoConfig}.</p>
 */
@ApplicationScoped
public class NaiveChunker {

    private final DemoConfig demoConfig;

    NaiveChunker(DemoConfig demoConfig) {
        this.demoConfig = demoConfig;
    }

    public List<TextSegment> chunk(ExtractionResult result, Mode mode) {
        // Split the full document text into sentence-bounded segments with overlap.
        // This is the same chunker used by both Mode A (Tika) and Mode B (Docling).
        // The key demo point: the chunker is identical — only the extraction quality differs.
        var splitter = new DocumentBySentenceSplitter(this.demoConfig.rag().maxTokens(), this.demoConfig.rag().overlap());
        var segments = splitter.split(result.document());

        // Store the text of neighboring segments (2 before, 2 after) as "extended_content"
        // metadata, so the embedding captures broader context than the segment alone.
        enrichWithSurroundingContext(segments, 2, 2);

        return segments.stream()
            .map(segment -> attachMetadata(segment, result, mode))
            .toList();
    }

    // Attach provenance metadata (page number, element type/label) to each segment.
    // Docling extraction provides provenance — Tika does not, so Mode A segments
    // will have no page/element metadata. This is visible in the demo UI's chunk grid.
    private TextSegment attachMetadata(TextSegment segment, ExtractionResult result, Mode mode) {
        var metadata = segment.metadata().copy();
        metadata.put("mode", mode.name());

        if (result.hasProvenance()) {
            // Match the segment's character range against the provenance entries to find
            // which document element (paragraph, table, heading, etc.) it came from.
            var fullText = result.document().text();
            var segmentStart = fullText.indexOf(segment.text());

            if (segmentStart >= 0) {
                var segmentEnd = segmentStart + segment.text().length();

                result.provenance().stream()
                    .filter(entry -> overlaps(entry, segmentStart, segmentEnd))
                    .findFirst()
                    .ifPresent(entry -> {
                        if (entry.pageNumber() != null) {
                            metadata.put("page_number", entry.pageNumber());
                        }
                        if (entry.elementType() != null) {
                            metadata.put("element_type", entry.elementType());
                        }
                        if (entry.elementLabel() != null) {
                            metadata.put("element_label", entry.elementLabel());
                        }
                    });
            }
        }

        return TextSegment.from(segment.text(), metadata);
    }

    private boolean overlaps(ProvenanceEntry entry, int segmentStart, int segmentEnd) {
        return (entry.startChar() < segmentEnd) && (entry.endChar() > segmentStart);
    }

    // Sliding-window context enrichment: for each segment, concatenate the text of its
    // neighbors into an "extended_content" metadata field. The embedding model sees this
    // broader window, improving retrieval without changing the segment boundaries.
    // https://docs.quarkiverse.io/quarkus-langchain4j/dev/rag-ingestion.html
    private void enrichWithSurroundingContext(List<TextSegment> segments, int before, int after) {
        for (int i = 0; i < segments.size(); i++) {
            var extendedContent = IntStream.rangeClosed(i - before, i + after)
                .filter(j -> (j >= 0) && (j < segments.size()))
                .mapToObj(j -> segments.get(j).text())
                .collect(Collectors.joining(" "));
            segments.get(i).metadata().put("extended_content", extendedContent);
        }
    }
}
