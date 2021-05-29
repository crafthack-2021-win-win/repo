package pro.heinrichs.winwin.pipeline;

import java.io.IOException;

/**
 * An action that is executed as part of a pipeline.
 */
public interface Step {
    void execute(Config config, Variables variables) throws IOException;
}
