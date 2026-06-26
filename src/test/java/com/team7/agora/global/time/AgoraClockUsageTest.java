package com.team7.agora.global.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgoraClockUsageTest {

    @Test
    void productionCodeUsesAgoraClockForLocalDateTimeNow() throws IOException {
        Path mainJava = Path.of("src/main/java");

        List<String> directNowUsages = Files.walk(mainJava)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> !path.toString().endsWith("AgoraClock.java"))
                .flatMap(path -> directNowUsages(path).stream())
                .toList();

        assertThat(directNowUsages).isEmpty();
    }

    private List<String> directNowUsages(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            return lines.stream()
                    .filter(line -> line.contains("LocalDateTime.now("))
                    .map(line -> path + ": " + line.trim())
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan " + path, e);
        }
    }
}
