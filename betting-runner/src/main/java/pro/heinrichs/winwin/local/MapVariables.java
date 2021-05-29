package pro.heinrichs.winwin.local;

import lombok.Data;
import pro.heinrichs.winwin.pipeline.Variables;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Data
public final class MapVariables implements Variables {
    private final Map<String, Object> values;

    @Override
    public void set(final String name, final Object value) {
        if (value == null) {
            this.values.remove(name);
            return;
        }
        this.values.put(name, value);
    }

    @Override
    public Object get(final String key) {
        if (!this.values.containsKey(key)) {
            throw new NoSuchElementException("Expected Key is missing: " + key);
        }
        return this.values.get(key);
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return this.values.entrySet();
    }
}
