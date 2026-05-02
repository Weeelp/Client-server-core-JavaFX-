package common.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    private static int port = 8080;
    private static String host = "localhost";
    private static int timeout = 5000;

    private static String dbUrl = "jdbc:postgresql://localhost:5432/lab7";
    private static String dbUser = "postgres";
    private static String dbPassword = "1234";

    static {
        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get(".env")));
            
            port = Integer.parseInt(props.getProperty("PORT", "8080"));
            host = props.getProperty("HOST", "localhost");
            timeout = Integer.parseInt(props.getProperty("CLIENT_TIMEOUT", "5000"));
        
            dbUrl = props.getProperty("DB_URL", dbUrl);
            dbUser = props.getProperty("DB_USER", dbUser);
            dbPassword = props.getProperty("DB_PASSWORD", dbPassword);

            System.out.println("--- Config Loaded ---");
            System.out.println("DB URL: " + dbUrl);
            System.out.println("DB User: " + dbUser);
            System.out.println("---------------------");
        } catch (IOException | NumberFormatException e) {
            System.err.println("WARN: .env не найден! Работаю на дефолтах.");
        }
    }

    public static int getPort() { return port; }
    public static String getHost() { return host; }
    public static int getTimeout() { return timeout; }

    public static String getDbUrl() { return dbUrl; }
    public static String getDbUser() { return dbUser; }
    public static String getDbPassword() { return dbPassword; }
}
