package pro.heinrichs.winwin.server;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;

public class RootMetrics extends AbstractMap<String, Object> {
    private final HashMap<String, Object> metrics = new HashMap<>();

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.metrics.entrySet();
    }

    @Override
    public Object put(final String key, final Object value) {
        final var prev = this.metrics.put(key, value);
        if (prev != null) {
            throw new IllegalStateException(String.format(
                "Mismatch: Metric %s is not unique!",
                key
            ));
        }
        return null;
    }

    @Override
    public void clear() {
        this.metrics.clear();
    }
}
