package pro.heinrichs.winwin.placement;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.concurrent.Callable;

@Slf4j
@CommandLine.Command(
    name = "placer",
    description = "Starts the placer server"
)
public class PlacerCommand implements Callable<Integer> {
    @CommandLine.Option(
        names = {"-p", "--port"},
        description = "Overrides the port of the HTTP Server",
        required = true
    )
    private int port;

    @CommandLine.Option(
        names = {"--database"},
        description = "Overrides the port of the HTTP Server",
        defaultValue = "database.db"
    )
    private Path database;

    @Override
    public Integer call() throws Exception {
        final var dir = this.database.getParent();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        log.info("Using SQLite database at: {}", this.database);
        final var cfg = new SQLiteConfig();
        cfg.enforceForeignKeys(true);
        final var instantiated = !Files.exists(this.database);
        final var source = new SQLiteDataSource(cfg);
        source.setUrl(String.format(
            "jdbc:sqlite:%s",
            this.database.toAbsolutePath()
        ));
        if (instantiated) {
            @Cleanup final var stmt = source.getConnection().createStatement();
            // TODO: Use flyway instead
            stmt.executeUpdate(
                "create table users (" +
                    "id integer PRIMARY KEY AUTOINCREMENT," +
                    "username varchar(100) NOT NULL ," +
                    "token long NOT NULL" +
                    ")"
            );
            stmt.executeUpdate(
                "create table bets (" +
                    "id integer PRIMARY KEY AUTOINCREMENT, " +
                    "metric varchar(100)," +
                    "start datetime," +
                    "end datetime," +
                    "done tinyint" +
                    ")"
            );
            stmt.executeUpdate(
                "create table user_bets(" +
                    "id integer PRIMARY KEY AUTOINCREMENT," +
                    "bet integer  NOT NULL," +
                    "user integer NOT NULL," +
                    "amount long  NOT NULL," +
                    "is_positive tinyint  NOT NULL," +
                    "FOREIGN KEY (user) REFERENCES users(id)," +
                    "FOREIGN KEY (bet) REFERENCES bets(id)" +
                    ")"
            );

            stmt.execute("INSERT INTO users VALUES (1, 'one', 200)");
            stmt.execute("INSERT INTO users VALUES (2, 'two', 200)");
            stmt.execute(
                "INSERT INTO bets VALUES (" +
                    "1, " +
                    "'com_chess_stats_gothamchess_chess_blitz', " +
                    "datetime('2020-05-29 23:30:00'), " +
                    "datetime('2020-05-29 23:35:00')," +
                    "0" +
                    ")"
            );
        }
        final var httpd = new HTTPD(this.port, source);
        while (!Thread.interrupted()) {
            Thread.sleep(1000);
        }
        httpd.closeAllConnections();
        return 0;
    }
}
