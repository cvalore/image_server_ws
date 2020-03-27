package rest.image_server.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
      //TODO
      //VEDERE SE VA BENE PER COSE CONCORRENTI (SE SI DEVE CONSIDERARE)

      //NO THREAD SAFE

      public static Connection getConnection() {
            //String dbUrl = System.getenv("JDBC_DATABASE_URL");
            try {
                  return DriverManager.getConnection("jdbc:postgresql://ec2-35-174-88-65.compute-1.amazonaws.com:5432/d2sp18jd7i75ad?user=hvunjftmpuozbr&password=89a0e80d4bed7fd8c68626d12b035acc5fcdbdbffc5437ded4cf585d1a82c496&sslmode=require");
            } catch (SQLException e) {
                  e.printStackTrace();
            }
            return null;
      }

      private static Map<String, User> users = new HashMap<>();
      private static Map<String, List<Image>> imagesByUsers = new HashMap<>();

      public static Map<String, User> getUsers() {
            return users;
      }
      public static Map<String, List<Image>> getImagesByUsers() { return imagesByUsers; }
}
