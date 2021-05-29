package pro.heinrichs.winwin.pipeline;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class Verify {
    public String isString(final String name, final Object value) {
        Objects.requireNonNull(value, name + " must not be null");
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(name + " must be a String but is " + value.getClass());
        }
        return (String) value;
    }

    public static Collection<?> isCollection(final String name, final Object value) {
        Objects.requireNonNull(value, name + " must not be null");
        if (!(value instanceof Collection)) {
            throw new IllegalArgumentException(name + " must be a Collection but is " + value.getClass());
        }
        return (Collection<?>) value;
    }

    public static Map<?, ?> isMap(final String name, final Object value) {
        Objects.requireNonNull(value, name + " must not be null");
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException(name + " must be a Map but is " + value.getClass());
        }
        return (Map<?, ?>) value;
    }
}
