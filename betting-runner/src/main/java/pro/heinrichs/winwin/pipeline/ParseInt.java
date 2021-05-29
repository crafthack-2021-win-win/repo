package pro.heinrichs.winwin.pipeline;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class ParseInt implements Step {
    private static final NumberFormat INT = DecimalFormat.getNumberInstance(Locale.ENGLISH);

    @Override
    public void execute(final Config config, final Variables variables) throws IOException {
        try {
            final var input = Verify.isString("input", config.get("input"));
            final var output = Verify.isString("output", config.get("output"));
            final var value = Verify.isString("value", variables.get(input));
            final var parsed = INT.parse(value.trim());
            variables.set(output, parsed);
        } catch (final ParseException ex) {
            throw new IOException("Error parsing number");
        }
    }
}
