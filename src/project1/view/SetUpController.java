package project1.view;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import project1.model.DBCreds;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SetUpController {

    private String dbURL;
    private String user;
    private String password;
    private Properties props = new Properties();

    @FXML
    private TextField dbURLTextbox;

    @FXML
    private TextField dbUserTextbox;

    @FXML
    private PasswordField dbPassBox;

    @FXML
    private Button testConnButton;

    @FXML
    private Label testConnLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    private void initialize(){
        try {
            props.load(new FileInputStream("db.properties"));

            dbURL = props.getProperty("dburl");
            user = props.getProperty("user");
            password = props.getProperty("password");
            dbURLTextbox.setText(dbURL);
            dbUserTextbox.setText(user);
            dbPassBox.setText(password);
        }catch(IOException e) {
            e.printStackTrace();
        }
        okButton.disableProperty().bind(
                Bindings.isEmpty(dbURLTextbox.textProperty())
                        .or(Bindings.isEmpty(dbUserTextbox.textProperty()))
        );
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currStage = (Stage) cancelButton.getScene().getWindow();
        currStage.close();
    }

    @FXML
    void handleOk(ActionEvent event) {
        props.setProperty("dburl",dbURLTextbox.getText());
        props.setProperty("user", dbUserTextbox.getText());
        props.setProperty("password", dbPassBox.getText());
        try {
            props.store(new FileOutputStream("db.properties"), null);
        }catch(IOException e) {
            e.printStackTrace();
        }
        Stage currStage = (Stage) okButton.getScene().getWindow();
        currStage.close();
    }

    @FXML
    void handleTest(ActionEvent event) {
        try (Connection connection = DriverManager.getConnection(dbURLTextbox.getText(), dbUserTextbox.getText(), dbPassBox.getText())) {
            testConnLabel.setVisible(true);
            testConnLabel.setText("Successful!");
            testConnLabel.setTextFill(Paint.valueOf("Green"));
        }catch (SQLException e) {
            testConnLabel.setVisible(true);
            testConnLabel.setText("Failed!");
            testConnLabel.setTextFill(Paint.valueOf("Red"));
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

}
