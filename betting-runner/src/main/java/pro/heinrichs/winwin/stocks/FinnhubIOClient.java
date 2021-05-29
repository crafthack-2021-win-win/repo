package pro.heinrichs.winwin.stocks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class FinnhubIOClient extends WebSocketClient {
    // In free version this is limited to 50 Symbols
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Getter
    private final Map<String, Number> prices = new ConcurrentHashMap<>();
    @Getter
    private final List<String> symbols;
    @Getter
    private final CountDownLatch latch = new CountDownLatch(1);

    public FinnhubIOClient(final String token, final List<String> symbols) throws URISyntaxException {
        super(new URI("wss://ws.finnhub.io?token=" + token));
        this.symbols = symbols;
        if (symbols.isEmpty()) {
            throw new IllegalArgumentException("Symbols cannot be empty!");
        }
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public void onOpen(final ServerHandshake hs) {
        log.debug("onOpen: {}", this.symbols);
        final var writer = MAPPER.writer();
        for (final var symbol : this.symbols) {
            try {
                send(writer.writeValueAsString(new Subscription(symbol)));
            } catch (final JsonProcessingException ex) {
                // TODO: Log error properly
                System.out.println("Error!");
                ex.printStackTrace();
                closeBlocking();
            }
        }
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        this.latch.countDown();
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public void onMessage(final String message) {
        try {
            final var msg = MAPPER.reader().readValue(
                message,
                Message.class
            );
            final var type = msg.getType();
            switch (type) {
                default:
                    log.error("Invalid type provided: {}\n\t{}\n", type, message);
                    close();
                    return;
                case "ping":
                    // NOOP
                    return;
                case "trade":
                    break;
            }
            final var data = msg.getData();
            for (final var row : data) {
                this.prices.put(row.getS(), row.getP());
            }
        } catch (final IOException e) {
            // TODO: Log error properly
            System.err.println("Invalid JSON received!");
            e.printStackTrace();
            closeBlocking();
        }
    }

    @Override
    public void onError(final Exception ex) {
        close();
    }
}