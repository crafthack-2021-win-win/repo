package pro.heinrichs.winwin.stocks;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class Randomizer implements Runnable {
    private final Random random;
    private final FinnhubIOClient client;
    private final boolean loop;

    @Override
    // TODO: Proper handling
    @SneakyThrows(InterruptedException.class)
    public void run() {
        do {
            final var prices = this.client.getPrices();
            for (final var sym : this.client.getSymbols()) {

                final var input = prices.get(sym);
                final double price = input == null
                    ? Math.abs(this.random.nextInt(40_000) + this.random.nextDouble())
                    : (input.doubleValue() + (this.random.nextInt(100) + this.random.nextDouble()) * (this.random.nextBoolean() ? -1 : 1));
                prices.put(sym, Math.round(price * 100.0) / 100.0);
            }
        } while (this.loop && !this.client.getLatch().await(100, TimeUnit.MILLISECONDS));
    }
}
