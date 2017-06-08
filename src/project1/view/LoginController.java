package project1.view;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import project1.MainApp;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Node;


public class LoginController {

    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
    private String username = "root";
    private String password = "admin";
    private String tempemail = "test@gmail.com";
    private String temppass = "password";
    private Integer instrid;

    private MainApp mainApp;
    private OverviewController homeScreen;
    private LoginController loginScreen;
    private Stage mainGui;
    private Scene scene;

    public LoginController(){

    }


    @FXML
    private TextField emailbox;

    @FXML
    private Button login;

    @FXML
    private PasswordField passwordbox;

    @FXML
    private void initialize(){
        emailbox.setText("test@gmail.com");
        passwordbox.setText("password");
        login.disableProperty().bind(
                Bindings.isEmpty(emailbox.textProperty())
                        .or(Bindings.isEmpty(passwordbox.textProperty()))
        );
    }

    @FXML
    void logintosystem(ActionEvent event) {
        String email = emailbox.getText();
        String pass = passwordbox.getText();
        String newQuery = "SELECT password,idinstructor FROM instructor WHERE email = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected!");
            PreparedStatement preparedStatement = connection.prepareStatement(newQuery,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1,email);
            ResultSet rs  = preparedStatement.executeQuery();

            if (rs.next()){

                String passcheck = rs.getString(1);
                //DO THE PASSWORD SECURITY CHECK
                //System.out.println(passcheck);
                //System.out.println(pass);
                if (passcheck.equals(pass)){
                    //System.out.println("HERE");
                    instrid = rs.getInt(2);
                    //System.out.println(instrid);
                    Stage oldstage = (Stage) login.getScene().getWindow();
                    oldstage.close();
                    initMainScreen(email);


                }else{
                    loginFailed();
                }
            }else{
                loginFailed();
            }
            preparedStatement.close();
            connection.close();
            rs.close();

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }





    }
    public void setMainApp(MainApp mainApp,Stage mainGui){
        this.mainApp = mainApp;
        this.mainGui = mainGui;

    }

    public Integer getUserID(){
        return instrid;
    }

    public void loginFailed(){
        //System.out.println("Password incorrect!");
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Login Failed");
        alert.setHeaderText("Login Failed");
        alert.setContentText("Your username and/or password is incorrect!");
        alert.showAndWait();
    }
    public void initMainScreen(String email){
        //homeScreen = new OverviewController();
        System.out.println(LocalDate.now());
        //homeScreen.setUserId(instrid);
        //homeScreen.initMainScreen(mainApp,mainGui,instrid);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Overview.fxml"));
            Parent root = (Parent)fxmlLoader.load();
            scene = new Scene(root);
            OverviewController newController = fxmlLoader.<OverviewController>getController();
            newController.setUserId(instrid,email);
            newController.loadData();
            newController.runPage();
            Stage homeStage = new Stage();
            newController.initMainScreen(mainApp,mainGui,homeStage);
            homeStage.setTitle("Home Screen");
            homeStage.setScene(scene);
            homeStage.show();
        }catch (IOException e){
            e.printStackTrace();
        }

        //loadClasses();

    }
}
