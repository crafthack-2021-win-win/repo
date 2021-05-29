package pro.heinrichs.winwin.stocks;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public final class Message {
    private final List<DataRow> data = new ArrayList<>();
    private String type;

    @Data
    public static class DataRow {
        /**
         * Symbol.
         */
        private String s;

        /**
         * Last price.
         */
        private Number p;

        /**
         * UNIX milliseconds timestamp.
         */
        private String t;

        /**
         * Volume.
         */
        private String v;

        private List<String> c;
    }
}
