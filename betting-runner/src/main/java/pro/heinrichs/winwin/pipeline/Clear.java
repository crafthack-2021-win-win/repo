package pro.heinrichs.winwin.pipeline;

import java.io.IOException;
import java.util.Collection;

public final class Clear implements Step {
    @Override
    public void execute(final Config config, final Variables variables) throws IOException {
        final var input = config.get("input");
        if (input instanceof String) {
            variables.set((String) input, null);
            return;
        }

        if (input instanceof Collection) {
            for (final var in : (Collection<?>) input) {
                variables.set(Verify.isString("Input", in), null);
            }
            return;
        }
        throw new IOException("Invalid input. Expected only Collections or String");
    }
}
