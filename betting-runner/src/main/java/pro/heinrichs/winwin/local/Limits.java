package pro.heinrichs.winwin.local;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class Limits {
    private final Request request = new Request();

    @Data
    public static class Request {
        private int minutes;

        public LocalDateTime from(final LocalDateTime last) {
            return last.plusMinutes(this.minutes);
        }

        public Duration getDuration() {
            return Duration.ZERO
                .plusMinutes(this.minutes);
        }
    }
}
