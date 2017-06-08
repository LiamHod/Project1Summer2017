package project1.view;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;

public class LoginWindow {

    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
    private String username = "root";
    private String password = "admin";
    private String tempemail = "test@gmail.com";
    private String temppass = "password";

    public void login(){
        Stage window = new Stage();
        window.setTitle("login");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25,25,25,25));

        Text welcomeText = new Text("Welcome");
        welcomeText.setFont(Font.font(20));
        grid.add(welcomeText,0,0,2,1);

        Label emailLabel = new Label("email: ");
        grid.add(emailLabel,0,1);
        TextField emailBox = new TextField();
        grid.add(emailBox,1,1);

        Label passwordLabel = new Label("password: ");
        grid.add(passwordLabel,0,2);
        PasswordField passwordField = new PasswordField();
        grid.add(passwordField,1,2);

        Button loginButton = new Button("Log In");
        grid.add(loginButton,2,3);

//        loginButton.setOnAction(e -> {
//            while(LoginAccess() == 0){
//                passwordField.clear();
//            }
//        });
//    }
//    public Integer LoginAccess(){
//        String newQuery = "SELECT password,idinstructor FROM instructor WHERE email = ?";
//        try (Connection connection = DriverManager.getConnection(url, username, password)) {
//            System.out.println("Database connected!");
//            PreparedStatement preparedStatement = connection.prepareStatement(newQuery);
//            preparedStatement.setString(1,tempemail);
//            ResultSet rs  = preparedStatement.executeQuery();
//            rs.next();
//            String passcheck = rs.getString(1);
//            //DO THE PASSWORD SECURITY CHECK
//            if (passcheck.equals(temppass)){
//                Integer instrid = rs.getInt(2);
//                return instrid;
//            }else{
//                System.out.println("INCORRECT PASSWORD");
//                return 0;
//
//            }
//
//
//
//        } catch (SQLException e) {
//            throw new IllegalStateException("Cannot connect the database!", e);
//        }


    }
}
