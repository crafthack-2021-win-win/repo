package pro.heinrichs.winwin.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.heinrichs.winwin.server.PrometheusNodeExporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Extension point used to export data retrieved from running a pipeline.
 */
public interface Exporter {
    // TODO: Remove
    Logger log = LoggerFactory.getLogger(Exporter.class);

    void export(Map<String, Object> metrics) throws IOException;

    // TODO: Use Service Discovery instead
    static Exporter getInstance(final String name, final String output) {
        if ("prometheus--node-exporter".equals(name)) {
            final var path = Paths.get(output);
            final var parent = path.getParent();
            if (!Files.exists(parent)) {
                log.error(
                    "Please create the following directory: {} for the File {}",
                    parent.toAbsolutePath(),
                    path.getFileName()
                );
                return null;
            }
            return new PrometheusNodeExporter(path);
        }
        return null;
    }
}
