package rest.image_server.model;

import rest.image_server.exceptions.GenericException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;

public class test {

    public static void main(String args[]) {

        Connection conn = null;
        Statement stmt = null;
        String query;

        int width;
        int height;
        File file = new File("/home/alessio/Scaricati/download.jpg");

        try {
            conn = Database.getConnection();
            if(conn == null) {
                throw new GenericException("Cannot connect to db");
            }
            stmt = conn.createStatement();
            query = "SELECT Picture from Images WHERE useruuid = 'bcc102a9'";

            FileInputStream fis = new FileInputStream(file);
            query = "INSERT INTO Images (useruuid, imageuuid, title, width, height, picture) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, "userUuid");
            ps.setString(2, "uuid");
            ps.setString(3, "title");
            ps.setInt(4, 300);
            ps.setInt(5, 168);
            ps.setBinaryStream(6, fis, (int)file.length());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException | FileNotFoundException e) {e.printStackTrace();
        } catch (IOException e) {
            throw new GenericException("Exception while loading the file:\n" + e.getMessage());

        } finally {
            try {
                if (stmt != null){
                    stmt.close();
                }
            } catch (SQLException e) {e.printStackTrace();}
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
