package pro.heinrichs.winwin.local;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import pro.heinrichs.winwin.pipeline.Runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.Callable;

// TODO: Create docker images for Server and Runner (docker-compose)
@CommandLine.Command(
    name = "local",
    description = "allows to run a single specification"
)
@Slf4j
@Data
public class LocalCommand implements Callable<Integer> {
    @CommandLine.Parameters(
        index = "0",
        description = "Specification to run"
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
        description = "Output format (depends on the exporter chosen)",
        required = true
    )
    private String output;

    @Override
    public Integer call() throws Exception {
        if (!Files.exists(this.input)) {
            log.error(
                "The provided file {} does not exist",
                this.input.toRealPath().toAbsolutePath()
            );
            return -1;
        }
        final var exporter = Exporter.getInstance(this.exporter, this.output);
        if (exporter == null) {
            log.error("Exporter {} not found", this.exporter);
            return 4;
        }

        final var metrics = new YamlParser()
            .parse(
                Files.newBufferedReader(this.input),
                Runner.createNewInstance(new MapVariables(new HashMap<>()))
            );
        if (metrics == null) {
            // Should never occur: Filter is not set
            log.error("Filter did not match!");
            return -1;
        }
        exporter.export(metrics);
        return 0;
    }

}
