package pro.heinrichs.winwin.server;

import lombok.RequiredArgsConstructor;
import pro.heinrichs.winwin.local.Exporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Allows exporting the run results through prometheus node-exporter.
 *
 * @see <a href="https://github.com/prometheus/node_exporter#textfile-collector">Documentation</a>
 */
@RequiredArgsConstructor
public class PrometheusNodeExporter implements Exporter {
    private final Path output;

    @Override
    public void export(final Map<String, Object> metrics) throws IOException {
        final var tmp = Files.createTempFile("prometheus-node-exporter-generation-", ".tmp");
        try (final var m = Files.newBufferedWriter(tmp)) {
            PrometheusStreamExporter.export(m, metrics);
        }
        Files.move(tmp, this.output, StandardCopyOption.REPLACE_EXISTING);
    }
}
