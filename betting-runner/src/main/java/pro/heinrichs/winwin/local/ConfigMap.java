package pro.heinrichs.winwin.local;

import lombok.RequiredArgsConstructor;
import pro.heinrichs.winwin.pipeline.Config;

import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public final class ConfigMap implements Config {
    private final Map<String, Object> map;

    @Override
    public Object get(final String key) {
        return Objects.requireNonNull(this.map.get(key), "Key " + key + " is null");
    }
}
