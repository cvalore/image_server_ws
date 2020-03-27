package rest.image_server.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class tests {

    public static void main(String args[]) {

        File file = new File("/home/alessio/Scaricati/download.jpg");
        System.out.println("file length " + file.length());
        Connection conn = Database.getConnection();
        try {
            FileInputStream fis = new FileInputStream(file);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO pic VALUES (?, ?)");
            ps.setString(1, file.getName());
            ps.setBinaryStream(2, fis, (int)file.length());
            ps.executeUpdate();
            ps.close();
            fis.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }
}
