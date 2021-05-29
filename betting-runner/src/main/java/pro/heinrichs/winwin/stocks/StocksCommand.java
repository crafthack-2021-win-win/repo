package pro.heinrichs.winwin.stocks;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "stocks",
    description = "Publishes real time stock-exchange symbols as metrics"
)
@Slf4j
public final class StocksCommand implements Callable<Integer> {
    @CommandLine.Option(
        names = {"-t", "--token"},
        description = "Authentication Token",
        required = true
    )
    private String token;

    @CommandLine.Option(
        names = {"-s", "--symbol"},
        description = "Defines Symbols to watch",
        required = true
    )
    private List<String> symbols;

    @CommandLine.Option(
        names = {"--offline"},
        description = "Does not connect to the Finnhub.io Servers. Useful for usage with --dev"
    )
    private boolean offline;

    @CommandLine.Option(
        names = {"--dev"},
        description = {
            "Initializes the Stock Trades with initial, random data.",
            "In conjunction with --offline, stock data will be changed every second"
        }
    )
    private boolean dev;

    @Override
    public Integer call() throws Exception {
        final var client = new FinnhubIOClient(
            this.token,
            this.symbols
        );
        if (this.dev) {
            log.warn("Development Mode activated: Value of prices may be pseudo-random!");
            final var n = new Randomizer(new Random(), client, this.offline);
            new Thread(n).start();
        }
        if (this.offline) {
            log.warn("Offline Mode activated: Values are not retrieved from an online system!");
        } else {
            client.connect();
        }

        // TODO: Publish Prometheus
        final var server = new HTTPD(client, 8080);
        client.getLatch().await();
        server.stop();
        return 0;
    }
}
