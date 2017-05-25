package sample;
import java.sql.*;
import javax.naming.*;
import javax.sql.*;


/**
 * Created by Liam on 2017-05-23.
 */
public class ConnectionTest {
    public static void main(String[] args){
        String url = "jdbc:mysql://localhost:3306/project1test?useSSL=false";
        String username = "root";
        String password = "admin";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected!");
            Statement stmt = connection.createStatement();
            //stmt.execute("SELECT * FROM tabletest");
            ResultSet rs = stmt.executeQuery("SELECT * FROM tabletest");
            System.out.println(stmt);
            System.out.println(rs);
            rs.next();
            String foundType = rs.getString(1);
            System.out.println(foundType);
            //rs.next();
            String foundType2 = rs.getString(2);
            System.out.println(foundType2);


            stmt.close();
            connection.close();
            rs.close();

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }

    }
}
