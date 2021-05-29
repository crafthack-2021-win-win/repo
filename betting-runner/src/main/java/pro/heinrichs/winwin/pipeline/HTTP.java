package pro.heinrichs.winwin.pipeline;

import org.jsoup.Jsoup;

import java.io.IOException;

public final class HTTP implements Step {
    private static final String URL = "url";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0";
    private static final String OUTPUT = "output";

    @Override
    public void execute(final Config config, final Variables variables) throws IOException {
        final var url = Verify.isString("URL", config.get(URL));
        final var output = Verify.isString("Output", config.get(OUTPUT));
        final var resp = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            // We dont care about the content
            .ignoreContentType(true)
            .execute();
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IllegalStateException("Response failed");
        }
        variables.set(output, resp.body());
    }
}
