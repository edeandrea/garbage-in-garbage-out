package dev.ericdeandrea.docling.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
class IngestionAppTest {
    @Test
    @Launch
    void appStarts(LaunchResult result) {
        assertThat(result.exitCode()).isZero();
    }
}
