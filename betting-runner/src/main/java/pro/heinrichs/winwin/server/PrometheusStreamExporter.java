package pro.heinrichs.winwin.server;

import lombok.RequiredArgsConstructor;
import pro.heinrichs.winwin.local.Exporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Allows exporting the metrics through prometheus exposition formats.
 *
 * @see <a href="https://prometheus.io/docs/instrumenting/exposition_formats/">Spec</a>
 */
@RequiredArgsConstructor
public final class PrometheusStreamExporter implements Exporter {
    private static final Pattern UPPER_CASE = Pattern.compile("([A-Z])");
    private final Supplier<BufferedWriter> writers;

    @Override
    public void export(Map<String, Object> metrics) throws IOException {
        try (final var m = this.writers.get()) {
            export(m, metrics);
        }
    }

    public static void export(final BufferedWriter m, final Map<String, Object> metrics) throws IOException {
        for (final var metric : metrics.entrySet()) {
            final var key = UPPER_CASE.matcher(metric.getKey())
                .replaceAll("_$1")
                .toLowerCase();
            m.append(key)
                .append(" ")
                .append(String.valueOf(metric.getValue()))
                .append('\n');
        }
    }
}
