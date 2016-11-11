package ox.softeng.burst.util.test;

import javax.persistence.Persistence;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @since 10/11/2016
 */
public class GenerateSchema {

    public static void main(String[] args) {

        try {
            Map<String, String> extraConfig = new HashMap<>();
            extraConfig.put("javax.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/burst");
            //extraConfig.put("javax.persistence.schema-generation.database.action", "drop-and-create");
            extraConfig.put("javax.persistence.schema-generation.scripts.action", "update");
            extraConfig.put("javax.persistence.schema-generation.scripts.create-target", "burst_schema_update.ddl");

            Path p = Paths.get("src/test/resources/config.properties");
            if (!Files.exists(p)) p = Paths.get("burst-domain/src/test/resources/config.properties");

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(p));


            String user = (String) properties.get("hibernate.connection.user");
            String url = (String) properties.get("hibernate.connection.url");
            String password = (String) properties.get("hibernate.connection.password");

            if (user != null) extraConfig.put("hibernate.connection.user", user);
            if (password != null) extraConfig.put("hibernate.connection.password", password);
            if (url != null) extraConfig.put("hibernate.connection.url", url);

            Persistence.generateSchema("ox.softeng.burst", extraConfig);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }
}
