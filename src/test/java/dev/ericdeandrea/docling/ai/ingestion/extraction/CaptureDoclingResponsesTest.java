package dev.ericdeandrea.docling.ai.ingestion.extraction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ai.docling.serve.api.convert.request.options.OutputFormat;

import io.quarkus.test.junit.QuarkusTest;

import io.quarkiverse.docling.runtime.client.DoclingService;

// Utility test that captures real Docling Serve responses and writes them to
// src/test/resources/__files/ — the directory WireMock serves stub responses from.
//
// Skipped by default. Run with -Dcapture.docling.responses=true when you need to
// refresh the captured JSON (e.g. after upgrading Docling Serve or changing the
// fixture PDF). Requires a running Docling Serve instance (dev services will start one).
//
// The captured files are:
//   - docling-convert-response.json  (~986KB) — used by the "docling-convert" WireMock stub
//   - docling-chunk-response.json    (~90KB)  — used by the "docling-chunk-hybrid" WireMock stub
@QuarkusTest
@EnabledIfSystemProperty(named = "capture.docling.responses", matches = "true")
class CaptureDoclingResponsesTest {

    private static final Path FIXTURE = Path.of("fixtures/doclaynet-2206.01062v1.pdf");
    private static final Path OUTPUT_DIR = Path.of("src/test/resources/__files");

    @Inject
    DoclingService doclingService;

    @Inject
    ObjectMapper objectMapper;

    @Test
    void captureConversionResponse() throws IOException {
        var response = doclingService.convertFile(FIXTURE, OutputFormat.JSON);
        var json = objectMapper.copy()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .writeValueAsString(response);
        Files.writeString(OUTPUT_DIR.resolve("docling-convert-response.json"), json);
        System.out.println("Captured conversion response: %d bytes".formatted(json.length()));
    }

    @Test
    void captureChunkingResponse() throws IOException {
        var response = doclingService.chunkFileHybrid(FIXTURE, OutputFormat.JSON);
        var json = objectMapper.copy()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .writeValueAsString(response);
        Files.writeString(OUTPUT_DIR.resolve("docling-chunk-response.json"), json);
        System.out.println("Captured chunking response: %d bytes".formatted(json.length()));
    }
}
