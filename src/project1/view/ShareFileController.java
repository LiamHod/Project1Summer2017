package project1.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
//    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
//    private String username = "root";
//    private String password = "admin";
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
                Boolean hasCourse = checkIfCourse(connection);
//                String instrDocQuery = "INSERT INTO instrdoc (idinstructor,iddocument) " + "VALUES (?,?)";
//                String instrCourQuery = "INSERT INTO instrcour (idinstructor,idcourse) " + "VALUES (?,?)";
//                connection.setAutoCommit(false);
//                PreparedStatement psID = connection.prepareStatement(instrDocQuery);
//                psID.setInt(1,resipID);
//                psID.setInt(2,docId);
//                psID.executeUpdate();
//
//                if (! hasCourse) {
//                    PreparedStatement psIC = connection.prepareStatement(instrCourQuery);
//                    psIC.setInt(1, resipID);
//                    psIC.setInt(2, courseId);
//                    psIC.executeUpdate();
//                    psIC.close();
//                }
//                psID.close();
//                connection.commit();
//                connection.setAutoCommit(true);
                if (! hasCourse) {
                    String instrCourDocQuery = "INSERT INTO instrcourdoc (idinstructor,idcourse,iddocument) " + "VALUES (?,?,?)";
                    PreparedStatement psICD = connection.prepareStatement(instrCourDocQuery);
                    psICD.setInt(1, resipID);
                    psICD.setInt(2, courseId);
                    psICD.setInt(3, docId);
                    psICD.executeUpdate();
                    successAlert();
                }
                connection.close();
                Stage currStage = (Stage) okButton.getScene().getWindow();
                currStage.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);

            }
        }
    }

    private Boolean checkIfCourse(Connection connection) throws SQLException{
        String courseQuery = "SELECT idcourse FROM project1.course WHERE courname = ?";
        PreparedStatement ps = connection.prepareStatement(courseQuery);
        ps.setString(1,courseName);
        ResultSet rs = ps.executeQuery();
        rs.next();
        courseId = rs.getInt(1);
        //THIS IS OLD STUFF THAT WORKS IF NEEDING TO REVERT
        //String courseCheckQuery = "SELECT count(*) FROM project1.instrcour WHERE idinstructor = ? AND idcourse = ?;";
        String courseCheckQuery = "SELECT count(*) FROM project1.instrcourdoc WHERE idinstructor = ? AND idcourse = ? AND iddocument = ?;";
        ps = connection.prepareStatement(courseCheckQuery);
        ps.setInt(1,resipID);
        ps.setInt(2,courseId);
        ps.setInt(3,docId);
        rs = ps.executeQuery();
        rs.next();
        Integer hasCourse = rs.getInt(1);
        ps.close();
        rs.close();
        return (hasCourse > 0);
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
            //String emailCheckQuery = "SELECT count(*) FROM project1.instructor WHERE email = ? AND idinstructor != ?;";
            String alreadyCheckQuery = "SELECT count(*) FROM project1.instrcourdoc WHERE idinstructor = ? AND iddocument = ?;";
            //OLD GOOD STUFF BELOW
            //String alreadyCheckQuery = "SELECT count(*) FROM project1.instrdoc WHERE idinstructor = ? AND iddocument = ?;";
            String getIdQuery = "SELECT idinstructor FROM project1.instructor WHERE email = ?";
            //PreparedStatement emailCheck = connection.prepareStatement(emailCheckQuery);
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
            alreadyCheck.setInt(2,docId);
            ResultSet rsAL = alreadyCheck.executeQuery();
            rsAL.next();
            Integer haveFile = rsAL.getInt(1);
            rsAL.close();
            alreadyCheck.close();

            //emailCheck.setString(1,emailTextBox.getText());
            //emailCheck.setInt(2,instrId);
            //ResultSet rs = emailCheck.executeQuery();
            //rs.next();
            //Integer results = rs.getInt(1);
            rsAL.close();
            rsID.close();
            getIdPS.close();
            alreadyCheck.close();
            connection.close();
            //emailCheck.close();
            //rs.close();

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
        successAlert.setContentText("- Your file was successfully shared.");
        successAlert.showAndWait();
    }


    public void initFileIdAndUserId(DocFile selFile, Integer instrId, String courseName){
        this.docId = selFile.getDocid();
        this.docName = selFile.getDocname();
        this.instrId = instrId;
        this.courseName = courseName;
        fileNameLabel.setText(docName);
        fileNameLabel.setVisible(true);
    }

}
