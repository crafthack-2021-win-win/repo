package pro.heinrichs.winwin.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import pro.heinrichs.winwin.pipeline.Runner;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@Slf4j
public class YamlParser {
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private Predicate<DataSourceSpec> filter;

    public Map<String, Object> parse(final Reader input, final Runner runner) throws IOException {
        final var source = this.mapper.readValue(
            input,
            DataSourceSpec.class
        );
        if (this.filter != null && !this.filter.test(source)) {
            return null;
        }
        log.info("Running Pipeline {}", source.getName());
        final var pipeline = source.getPipeline();
        for (final var step : pipeline) {
            runner.run(step.getType(), step.getConfig());
        }

        return runner.collectMetrics()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                e -> String.format(
                    "%s_%s",
                    source.getNamespace()
                        .replace("_", "")
                        .replace('.', '_'),
                    e.getKey()
                ),
                Map.Entry::getValue
            ));
    }
}
