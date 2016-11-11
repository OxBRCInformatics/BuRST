package ox.softeng.burst.util.test;

import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Properties;

public class UpdateDatabase {

    private static void migrateDatabase(String url, String user, String password) {

    }

    public static void main(String[] args) throws NoSuchAlgorithmException, SQLException, IOException {
        try {
            Path p = Paths.get("src/test/resources/config.properties");
            if (!Files.exists(p)) p = Paths.get("burst-domain/src/test/resources/config.properties");

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(p));


            String user = (String) properties.get("hibernate.connection.user");
            String url = (String) properties.get("hibernate.connection.url");
            String password = (String) properties.get("hibernate.connection.password");

            Flyway flyway = new Flyway();
            flyway.setDataSource(url, user, password);
            flyway.migrate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }
}
