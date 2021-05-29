package pro.heinrichs.winwin.server;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import pro.heinrichs.winwin.local.DataSourceSpec;
import pro.heinrichs.winwin.local.Exporter;
import pro.heinrichs.winwin.local.Limits;
import pro.heinrichs.winwin.local.MapVariables;
import pro.heinrichs.winwin.local.YamlParser;
import pro.heinrichs.winwin.pipeline.Runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

@CommandLine.Command(
    name = "server",
    description = "Runs multiple specification files"
)
@Slf4j
public class ServerCommand implements Callable<Integer> {
    @CommandLine.Parameters(
        index = "0",
        description = "Folder for Specification(s) to run"
    )
    private Path input;

    @CommandLine.Option(
        names = {"-e", "--exporter"},
        description = "Name of the exporter to use",
        defaultValue = "prometheus--node-exporter",
        required = true
    )
    private String exporter;

    @CommandLine.Option(
        names = {"-o", "--output"},
        description = "Base Output format (depends on the exporter chosen)",
        required = true
    )
    private String output;

    private final Map<String, LocalDateTime> times = new HashMap<>();

    @Override
    public Integer call() throws Exception {
        if (!Files.exists(this.input)) {
            log.error(
                "The provided directory {} does not exist",
                this.input.toAbsolutePath()
            );
            return -1;
        }
        if (!Files.isDirectory(this.input)) {
            log.error(
                "The provided path {} is not a directory",
                this.input.toAbsolutePath()
            );
            return -2;
        }

        final var exporter = Exporter.getInstance(this.exporter, this.output);
        if (exporter == null) {
            log.error("Exporter {} not found", this.exporter);
            return -3;
        }

        final var rootMetrics = new RootMetrics();
        final var sleep = new AtomicLong(Long.MAX_VALUE);
        while (!Thread.interrupted()) {
            // TODO: This should run in parallel
            try (final var stream = Files.walk(this.input).skip(1)) {
                final var it = stream
                    .filter(i -> i.getFileName().toString().endsWith(".ds.yml"))
                    .iterator();
                final var parser = new YamlParser();
                while (it.hasNext()) {
                    final var path = it.next().toAbsolutePath();
                    final var key = path.toString();
                    final var last = this.times.get(key);
                    parser.setFilter(spec -> {
                        final var m = Optional
                            .of(spec)
                            .map(DataSourceSpec::getLimits)
                            .map(Limits::getRequest);
                        if (last == null) {
                            final var s = sleep.get();
                            final var dur = m
                                .map(Limits.Request::getDuration)
                                .stream()
                                .mapToLong(Duration::toMillis)
                                .findFirst()
                                .orElse(s);
                            if (dur < s) {
                                sleep.set(dur);
                            }
                            return true;
                        }
                        final var x = m.map(i -> i.from(last));
                        return x.isEmpty() || x.get().isBefore(LocalDateTime.now());
                    });
                    final var metrics = parser
                        .parse(
                            Files.newBufferedReader(path),
                            // Create a new Instance for each invocation => Clean state
                            Runner.createNewInstance(new MapVariables(new HashMap<>()))
                        );
                    if (metrics == null) {
                        continue;
                    }
                    this.times.put(key, LocalDateTime.now());
                    rootMetrics.putAll(metrics);
                }

            }
            log.info("Exporting Metrics...");
            exporter.export(rootMetrics);
            rootMetrics.clear();
            log.info("Sleeping for {}ms", sleep.get());
            Thread.sleep(sleep.get());
        }
        return 0;
    }
}
