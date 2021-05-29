package pro.heinrichs.winwin.pipeline;

import java.util.Map;
import java.util.Set;

/**
 * Context used between different {@link Step}s.
 */
public interface Variables extends Config {
    void set(final String name, final Object value);

    Set<Map.Entry<String, Object>> entrySet();
}
