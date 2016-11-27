/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 James Welch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ox.softeng.burst.domain.util;

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
