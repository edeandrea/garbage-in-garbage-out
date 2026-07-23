package dev.ericdeandrea.docling;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DoclingWiremockTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        if (Boolean.getBoolean("use.wiremock.docling")) {
            return Map.of(
                "quarkus.docling.base-url", "http://localhost:${quarkus.wiremock.devservices.port}",
                "quarkus.docling.devservices.enabled", "false"
            );
        }

        return Map.of();
    }
}
