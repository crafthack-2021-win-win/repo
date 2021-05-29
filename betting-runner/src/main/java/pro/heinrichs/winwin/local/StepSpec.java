package pro.heinrichs.winwin.local;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
public class StepSpec {
    @Getter(AccessLevel.NONE)
    private final Map<String, Object> map = new HashMap<>(0);
    @ToString.Exclude
    private final ConfigMap config = new ConfigMap(this.map);
    private String type;

    @JsonAnySetter
    public void set(final String name, final Object value) {
        this.map.put(name, value);
    }
}
