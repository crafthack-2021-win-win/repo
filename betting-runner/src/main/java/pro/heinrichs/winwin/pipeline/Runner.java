package pro.heinrichs.winwin.pipeline;

import com.example.user.json.JsonUserTest;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Helper class to run one pipeline.
 */
@RequiredArgsConstructor
public final class Runner {
    private final Map<String, Step> steps;
    private final Variables variables;

    public Runner run(final String step, final Config config) throws IOException {
        final var s = this.steps.get(step);
        if (s == null) {
            throw new NoSuchElementException("Step was not found!");
        }
        s.execute(config, this.variables);
        return this;
    }

    public Map<String, Object> collectMetrics() {
        return this.variables.entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith("@"))
            .collect(Collectors.toMap(
                // Strip @ away
                e -> e.getKey().substring(1),
                // Metric value need to be numeric for comparison purposes
                e -> {
                    final var v = e.getValue();
                    if (v instanceof Number) {
                        return v;
                    }
                    throw new IllegalArgumentException("Metrics need to be numeric");
                }
            ));
    }

    // TODO: Use Service Discovery instead
    public static Runner createNewInstance(final Variables ctx) {
        return new Runner(
            Map.ofEntries(
                Map.entry("http", new HTTP()),
                Map.entry("html-2-xml", new HTML2XML()),
                Map.entry("xpath", new XPath()),
                Map.entry("clear", new Clear()),
                Map.entry("parse-int", new ParseInt()),
                // User supplied
                Map.entry("json-path", new JsonUserTest())
            ),
            ctx
        );
    }
}
