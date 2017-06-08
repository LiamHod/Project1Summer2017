package sample;
import java.io.*;
import java.sql.*;
import javax.naming.*;
import javax.sql.*;


/**
 * Created by Liam on 2017-05-23.
 */
public class ConnectionTest {
    public static void main(String[] args) throws FileNotFoundException{
        String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
        String username = "root";
        String password = "admin";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected!");
            Statement stmt = connection.createStatement();
            //stmt.execute("SELECT * FROM tabletest");
            ResultSet rs = stmt.executeQuery("SELECT * FROM document");
            //System.out.println(stmt);
            //System.out.println(rs);
            rs.next();
            String foundType = rs.getString(1);
            System.out.println(foundType);
            //rs.next();
            String foundType2 = rs.getString(2);
            System.out.println(foundType2);
            Blob imageBlob = rs.getBlob(8);
            //InputStream binaryStream = imageBlob.getBinaryStream(0,imageBlob.length());
            //System.out.println(imageBlob);
            InputStream is = imageBlob.getBinaryStream();
            FileOutputStream fos = new FileOutputStream("C:" + "\\" + "Users" + "\\" + "Liam" + "\\" + "Downloads"+"\\"+"testname1.pdf");
            int b = 0;
            while ((b = is.read()) != -1){
                fos.write(b);
            }
            File newfile = new File("C:\\Users\\Liam\\Downloads\\IMG_331100.jpg");
            FileInputStream inputStream = new FileInputStream(newfile);
            String sqlinsert = "INSERT INTO document (title,docdesc,filetype,uploader,uploaddate,course_num,docfile) " + "VALUES (?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlinsert);
            preparedStatement.setString(1,"newtitle");
            preparedStatement.setString(2,"newdesc");
            preparedStatement.setString(3,"filetype2");
            preparedStatement.setString(4,"John");
            preparedStatement.setDate(5,java.sql.Date.valueOf("1990-03-03"));
            preparedStatement.setString(6,"CMPT103");
            preparedStatement.setBlob(7,inputStream);
            preparedStatement.executeUpdate();
            stmt.close();
            connection.close();
            rs.close();

        }  catch (IOException e){
            System.out.print("IOException");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }

    }
}
