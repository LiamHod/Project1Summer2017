package project1.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import project1.model.Courses;
import project1.model.DBCreds;
import project1.model.DocFile;

import java.sql.*;

public class ShareFileController {

    private Integer docId;
    private Integer instrId;
    private String courseName;
    private String docName;
    private Integer resipID;
    private Integer courseId;
    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();

    @FXML
    private void initialize(){

    }

    @FXML
    private TextField emailTextBox;

    @FXML
    private Label fileNameLabel;

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
                String instrCourDocQuery = "INSERT INTO instrcourdoc (idinstructor,idcourse,iddocument) " + "VALUES (?,?,?)";
                PreparedStatement psICD = connection.prepareStatement(instrCourDocQuery);
                psICD.setInt(1, resipID);
                psICD.setInt(2, courseId);
                psICD.setInt(3, docId);
                psICD.executeUpdate();
                successAlert();
                connection.close();
                Stage currStage = (Stage) okButton.getScene().getWindow();
                currStage.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);

            }
        }
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
            String alreadyCheckQuery = "SELECT count(*) FROM instrcourdoc WHERE idinstructor = ? AND idcourse = ? AND iddocument = ?;";
            String getIdQuery = "SELECT idinstructor FROM instructor WHERE email = ?";
            PreparedStatement alreadyCheck = connection.prepareStatement(alreadyCheckQuery);
            PreparedStatement getIdPS = connection.prepareStatement(getIdQuery);

            getIdPS.setString(1,emailTextBox.getText());
            ResultSet rsID = getIdPS.executeQuery();
            if (rsID.next()) {
                resipID = rsID.getInt(1);
            }else{
                validAlert();
                return false;
            }
            rsID.close();
            getIdPS.close();

            alreadyCheck.setInt(1,resipID);
            alreadyCheck.setInt(2,courseId);
            alreadyCheck.setInt(3,docId);
            ResultSet rsAL = alreadyCheck.executeQuery();
            rsAL.next();
            Integer haveFile = rsAL.getInt(1);
            rsAL.close();
            alreadyCheck.close();

            rsAL.close();
            rsID.close();
            getIdPS.close();
            alreadyCheck.close();
            connection.close();

            if(haveFile != 0){
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

    private void successAlert(){
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Share Completed");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Your file was successfully shared.");
        successAlert.showAndWait();
    }


    public void initFileIdAndUserId(DocFile selFile, Integer instrId, Courses curCourse){
        this.docId = selFile.getDocid();
        this.docName = selFile.getDocname();
        this.instrId = instrId;
        this.courseName = curCourse.getName();
        this.courseId = curCourse.getId();
        fileNameLabel.setText(docName);
        fileNameLabel.setVisible(true);
    }

}
