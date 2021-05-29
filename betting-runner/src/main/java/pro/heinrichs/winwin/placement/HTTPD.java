package pro.heinrichs.winwin.placement;

import fi.iki.elonen.NanoHTTPD;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class HTTPD extends NanoHTTPD {
    private static final String UPDATE_USER_TOKENS = "UPDATE users " +
        "SET token = token + ? " +
        "WHERE id = ?";
    private final DataSource source;

    public HTTPD(final int port, final DataSource source) throws IOException {
        super(port);
        this.source = source;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        log.info("Placement Server: http://localhost:{}/", port);
    }

    @Override
    public Response serve(final IHTTPSession session) {
        try {
            System.out.println(session.getUri());
            // TODO: Use real web framework. This is just quick and dirty
            switch (session.getUri()) {
                case "/tokens/gift":
                    return onGiftTokens(session);
                case "/tokens/bid":
                    return onBidding(session);
                case "/bets/update":
                    return distributeMoney(session);
                default:
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Operation not found");
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SQL error");
        } catch (final NumberFormatException ex) {
            return newFixedLengthResponse(Response.Status.NOT_ACCEPTABLE, MIME_PLAINTEXT, "Invalid Request");
        } catch (final Throwable ex) {
            ex.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "error");
        }
    }

    // TODO: Insert Bet

    // Needs to be called regularly (e.g. cron) For now limited to http method, so it can be tested more easily
    private Response distributeMoney(final IHTTPSession session) throws SQLException {
        try (final var con = this.source.getConnection()) {
            con.setAutoCommit(false);
            // Select bets that are in the past
            @Cleanup final var stmt = con.prepareStatement(
                "SELECT id, metric, start, end " +
                    "FROM bets b " +
                    "WHERE b.end < date('now') " +
                    "  and b.done = 0 " +
                    "LIMIT 1"
            );
            final var rs = stmt.executeQuery();
            if (rs.next()) {
                final var m = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                final var id = rs.getLong("id");
                final var metric = rs.getLong("metric");
                final var start = LocalDateTime
                    .from(m.parse(rs.getString("start").replace(' ', 'T')))
                    .toEpochSecond(ZoneOffset.UTC);
                final var end = LocalDateTime
                    .from(m.parse(rs.getString("end").replace(' ', 'T')))
                    .toEpochSecond(ZoneOffset.UTC);

                System.out.format("Bet %s - %s - %s%n", metric, start, end);
                // TODO: CALL http://localhost:9090/api/v1/query?query=official_stock_us_amzn&time=1622323800
                // Parse result (for start)
                // Parse result (for end)
                // Compare
                // Divide Proceeds
                @Cleanup final var marker = con.prepareStatement(
                    "UPDATE bets SET done = 1 WHERE id = ?"
                );
                marker.setLong(1, id);
                marker.execute();

//                con.commit();
                return newFixedLengthResponse("DONE");
            }
            con.rollback();
            return newFixedLengthResponse("NOTHING FOUND");
        }
    }

    // http://localhost:1337/tokens/bid?bet=1&uid=1&amount=100&positive=false
    private Response onBidding(final IHTTPSession session) throws SQLException {
        final var q = session.getParms();
        try (final var con = this.source.getConnection()) {
            con.setAutoCommit(false);

            final var user = Long.parseLong(q.get("uid"));
            final var amount = Long.parseLong(q.get("amount"));
            final var q1 = "INSERT INTO user_bets (bet, user, amount, is_positive) " +
                "VALUES (?, ?, ?, ?)";
            @Cleanup final var betting = con.prepareStatement(q1);
            betting.setLong(1, Long.parseLong(q.get("bet")));
            betting.setLong(2, user);
            betting.setLong(3, amount);
            betting.setBoolean(4, Boolean.parseBoolean(q.get("positive")));
            betting.execute();

            @Cleanup final var paying = con.prepareStatement(UPDATE_USER_TOKENS);
            paying.setLong(1, -amount);
            paying.setLong(2, user);
            paying.execute();

            @Cleanup final var select = con.prepareStatement(
                "SELECT token >= 0 FROM users WHERE id = ?"
            );
            select.setLong(1, user);
            final var rs = select.executeQuery();
            if (rs.next()) {
                final var i = rs.getBoolean(1);
                if (i) {
                    con.commit();
                    return newFixedLengthResponse("DONE");
                }
                con.rollback();
                return newFixedLengthResponse(
                    Response.Status.NOT_ACCEPTABLE,
                    MIME_PLAINTEXT,
                    "NOT ENOUGH BALANCE"
                );
            }

            con.rollback();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SQL error");
        }
    }

    // http://localhost:1337/tokens/gift?uid=2&gift=-20
    private Response onGiftTokens(final IHTTPSession session) throws SQLException {
        final var q = session.getParms();
        try (final var con = this.source.getConnection()) {
            try (final var stmt = con.prepareStatement(UPDATE_USER_TOKENS)) {
                stmt.setLong(1, Long.parseLong(q.get("gift")));
                stmt.setLong(2, Long.parseLong(q.get("uid")));
                if (stmt.executeUpdate() == 0) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 User not found");
                }
                return newFixedLengthResponse("DONE");
            }
        }
    }
}
