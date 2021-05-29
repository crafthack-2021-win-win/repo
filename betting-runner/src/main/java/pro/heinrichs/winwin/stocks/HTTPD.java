package pro.heinrichs.winwin.stocks;

import fi.iki.elonen.NanoHTTPD;
import lombok.extern.slf4j.Slf4j;
import pro.heinrichs.winwin.server.PrometheusStreamExporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class HTTPD extends NanoHTTPD {
    private static final String NAMESPACE = "official_stock_us_";
    private final FinnhubIOClient client;

    public HTTPD(final FinnhubIOClient client, final int port) throws IOException {
        super(port);
        this.client = client;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        log.info("Stock Server: http://localhost:{}/", port);
    }

    @Override
    public Response serve(final IHTTPSession session) {
        try {
            final var m = new StringWriter();
            final var buffer = new BufferedWriter(m);
            PrometheusStreamExporter.export(
                buffer,
                client.getPrices().entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        e -> NAMESPACE + e.getKey().toLowerCase(),
                        Map.Entry::getValue
                    ))
            );
            buffer.flush();
            return newFixedLengthResponse(m.toString());
        } catch (final IOException e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Error");
        }
    }
}
