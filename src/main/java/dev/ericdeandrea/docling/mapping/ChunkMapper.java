package dev.ericdeandrea.docling.mapping;

import java.time.Instant;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import dev.ericdeandrea.docling.model.ChunkMetadata;
import dev.ericdeandrea.docling.model.Mode;
import dev.ericdeandrea.docling.model.RetrievedChunk;
import dev.langchain4j.data.segment.TextSegment;

@Mapper(componentModel = ComponentModel.CDI)
public interface ChunkMapper {

    @Mapping(target = "text", expression = "java(segment.text())")
    @Mapping(target = "metadata", expression = "java(toMetadata(segment, relevanceScore, timestamp))")
    RetrievedChunk toRetrievedChunk(TextSegment segment, double relevanceScore, Instant timestamp);

    default ChunkMetadata toMetadata(TextSegment segment, double relevanceScore, Instant timestamp) {
        var metadata = segment.metadata();

        return new ChunkMetadata(
            metadata.getInteger("page_number"),
            metadata.getString("element_type"),
            metadata.getString("element_label"),
            toMode(metadata.getString("mode")),
            relevanceScore,
            timestamp
        );
    }

    default Mode toMode(String modeString) {
        return (modeString != null) ? Mode.valueOf(modeString) : null;
    }
}
