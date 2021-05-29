package pro.heinrichs.winwin;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import pro.heinrichs.winwin.local.LocalCommand;
import pro.heinrichs.winwin.placement.PlacerCommand;
import pro.heinrichs.winwin.server.ServerCommand;
import pro.heinrichs.winwin.stocks.StocksCommand;

// TODO: Picocli integrate for Server and Runner
// TODO: Create docker images for Server and Runner (docker-compose)

@CommandLine.Command(
    name = "bets-runner",
    version = "1.0.0",
    description = "Runs specification files",
    mixinStandardHelpOptions = true,
    subcommands = {ServerCommand.class, LocalCommand.class, StocksCommand.class, PlacerCommand.class}
)
@Slf4j
public class Launcher {
    public static void main(final String... args) {
        System.exit(new CommandLine(new Launcher()).execute(args));
    }
}
