package pro.heinrichs.winwin.pipeline;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Whitelist;

import java.io.IOException;

public final class HTML2XML implements Step {
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";

    @Override
    public void execute(final Config config, final Variables variables) throws IOException {
        final var in = Verify.isString("input", config.get(INPUT));
        final var out = Verify.isString("output", config.get(OUTPUT));

        final var doc = Jsoup.parse(
            Jsoup.clean(
                Verify.isString(String.format("Variable (%s)", in), variables.get(in)),
                Whitelist.relaxed().addAttributes(":all", "class")
            )
        );
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml).syntax(Document.OutputSettings.Syntax.xml);

        variables.set(out, doc.html());
    }
}
