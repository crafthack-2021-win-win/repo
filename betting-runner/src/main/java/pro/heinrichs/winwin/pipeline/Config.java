package pro.heinrichs.winwin.pipeline;

/**
 * The (dynamic) configuration of a given {@link Step}.
 */
public interface Config {
    /**
     * Retrieves the config value or throws an error if it does not exist.
     *
     * @param key Key
     * @return Value stored
     */
    Object get(String key);
}
