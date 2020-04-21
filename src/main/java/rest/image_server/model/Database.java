package rest.image_server.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
      //TODO
      //VEDERE SE VA BENE PER COSE CONCORRENTI (SE SI DEVE CONSIDERARE)

      //NO THREAD SAFE

      public static Connection getConnection() {
            //String dbUrl = System.getenv("JDBC_DATABASE_URL");
            try {
                  URI dbUri = new URI(System.getenv("DATABASE_URL"));
                  String username = dbUri.getUserInfo().split(":")[0];
                  String password = dbUri.getUserInfo().split(":")[1];
                  String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

                  return DriverManager.getConnection(dbUrl, username, password);
                  //return DriverManager.getConnection(dbUrl);
                  //return DriverManager.getConnection("jdbc:postgresql://ec2-35-174-88-65.compute-1.amazonaws.com:5432/d2sp18jd7i75ad?user=hvunjftmpuozbr&password=89a0e80d4bed7fd8c68626d12b035acc5fcdbdbffc5437ded4cf585d1a82c496&sslmode=require");
            } catch (SQLException | URISyntaxException e) {
                  e.printStackTrace();
            }
            return null;
      }
}
