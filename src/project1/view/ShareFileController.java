package project1.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;

public class ShareFileController {

    private Integer docId;
    private Integer instrId;
    private String courseName;
    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
    private String username = "root";
    private String password = "admin";

    @FXML
    private void initialize(){

    }

    @FXML
    private TextField emailTextBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currStage = (Stage) cancelButton.getScene().getWindow();
        currStage.close();
    }

    @FXML
    void handleOk(ActionEvent event) {
        if (checkInput()) {
            System.out.println("Valid email");
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                checkIfCourse();
            } catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);

            }
        }
    }

    private void checkIfCourse(){

    }

    private Boolean checkInput(){
        if (emailTextBox.getText() == null || emailTextBox.getText().length() == 0){
            Alert emailAlert = new Alert(Alert.AlertType.WARNING);
            emailAlert.setTitle("Email missing");
            emailAlert.setHeaderText("Email is missing from textbox");
            emailAlert.setContentText("Please enter an email in the textbox");
            emailAlert.showAndWait();
            return false;
        }
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String emailCheckQuery = "SELECT count(*) FROM project1.instructor WHERE email = ? AND idinstructor != ?;";
            String alreadyCheckQuery = "SELECT count(*) FROM project1.instrdoc WHERE idinstructor = ? AND iddocument = ?;";
            String getIdQuery = "SELECT idinstructor FROM project1.instructor WHERE email = ?";
            PreparedStatement emailCheck = connection.prepareStatement(emailCheckQuery);
            PreparedStatement alreadyCheck = connection.prepareStatement(alreadyCheckQuery);
            PreparedStatement getIdPS = connection.prepareStatement(getIdQuery);

            getIdPS.setString(1,emailTextBox.getText());
            ResultSet rsID = getIdPS.executeQuery();
            rsID.next();
            Integer resipID = rsID.getInt(1);
            rsID.close();
            getIdPS.close();

            alreadyCheck.setInt(1,resipID);
            alreadyCheck.setInt(2,docId);
            ResultSet rsAL = alreadyCheck.executeQuery();
            rsAL.next();
            Integer haveFile = rsAL.getInt(1);
            rsAL.close();
            alreadyCheck.close();

            emailCheck.setString(1,emailTextBox.getText());
            emailCheck.setInt(2,instrId);
            ResultSet rs = emailCheck.executeQuery();
            rs.next();
            Integer results = rs.getInt(1);
            connection.close();
            emailCheck.close();
            rs.close();

            if (results == 0){
                validAlert();
                return false;
            }else if(haveFile != 0){
                alreadyAlert();
                return false;
            }
            else{
                return true;
            }
        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);

        }

    }

    private void validAlert(){
        Alert validAlert = new Alert(Alert.AlertType.WARNING);
        validAlert.setTitle("Enter a valid email");
        validAlert.setHeaderText("Email is incorrect");
        validAlert.setContentText("Please enter an email that is valid and/or not your own email");
        validAlert.showAndWait();
    }

    private void alreadyAlert(){
        Alert alreadyAlert = new Alert(Alert.AlertType.WARNING);
        alreadyAlert.setTitle("Invalid recipient");
        alreadyAlert.setHeaderText("Recipient already has that file");
        alreadyAlert.setContentText("Please enter an email of a recipient that doesn't have file");
        alreadyAlert.showAndWait();
    }


    public void initFileIdAndUserId(Integer docId,Integer instrId,String courseName){
        this.docId = docId;
        this.instrId = instrId;
        this.courseName = courseName;
    }

}
