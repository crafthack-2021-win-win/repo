package pro.heinrichs.winwin.stocks;

import lombok.Data;

@Data
public final class Subscription {
    private final String type = "subscribe";
    private final String symbol;
}
