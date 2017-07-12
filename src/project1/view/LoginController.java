package project1.view;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import project1.MainApp;
import project1.model.DBCreds;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Node;


public class LoginController {

//    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
//    private String username = "root";
//    private String password = "admin";
    private String tempemail = "test@gmail.com";
    private String temppass = "password";
    private Integer instrid;
    private String dbURL;
    private String user;
    private String dbpassword;
    private DBCreds dbCreds = DBCreds.INSTANCE;

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
    private Hyperlink databaseLink;

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
    void handleSetUp(ActionEvent event) {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SetUp.fxml"));
            AnchorPane setUpPage = (AnchorPane)fxmlLoader.load();
            Stage setUpPageStage = new Stage();
            setUpPageStage.setTitle("Set up Database Connection");
            setUpPageStage.initModality(Modality.WINDOW_MODAL);
            setUpPageStage.initOwner(mainGui);
            Scene setUpPageScene = new Scene(setUpPage);
            SetUpController setUpController = fxmlLoader.getController();
            setUpPageStage.setScene(setUpPageScene);
            setUpPageStage.showAndWait();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    void logintosystem(ActionEvent event) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("db.properties"));

            dbURL = props.getProperty("dburl");
            user = props.getProperty("user");
            dbpassword = props.getProperty("password");
            dbCreds.setUrl(dbURL);
            dbCreds.setUsername(user);
            dbCreds.setPassword(dbpassword);
        }catch(IOException e) {
            e.printStackTrace();
        }
        String email = emailbox.getText();
        String pass = passwordbox.getText();
        String newQuery = "SELECT password,idinstructor FROM instructor WHERE email = ?";
        try (Connection connection = DriverManager.getConnection(dbCreds.getUrl(), dbCreds.getUsername(), dbCreds.getPassword())) {
            System.out.println("Database connected!");
            PreparedStatement preparedStatement = connection.prepareStatement(newQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, email);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {

                String passcheck = rs.getString(1);
                //DO THE PASSWORD SECURITY CHECK
                //System.out.println(passcheck);
                //System.out.println(pass);
                if (passcheck.equals(pass)) {
                    //System.out.println("HERE");
                    instrid = rs.getInt(2);
                    //System.out.println(instrid);
                    Stage oldstage = (Stage) login.getScene().getWindow();
                    oldstage.close();
                    initMainScreen(email);


                } else {
                    loginFailed();
                }
            } else {
                loginFailed();
            }
            preparedStatement.close();
            connection.close();
            rs.close();

        } catch (SQLException e) {
            Alert cantConnect = new Alert(Alert.AlertType.WARNING);
            cantConnect.setTitle("Cannot Connect to Database");
            cantConnect.setHeaderText("Cannot Connect to Database");
            cantConnect.setContentText("There was a problem connecting to the database. Please check your database settings");
            cantConnect.showAndWait();
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
