package project1.view;

//Apache Commons IO
//        Copyright 2002-2016 The Apache Software Foundation
//
//        This product includes software developed at
//        The Apache Software Foundation (http://www.apache.org/).

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddFileDialogController {

    private String filePath;
    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
    private String username = "root";
    private String password = "admin";
    private String email;
    private Integer instrId;

    @FXML
    private TextField titleTextBox;

    @FXML
    private TextArea descTextBox;

    @FXML
    private ComboBox<String> courseComboBox;

    @FXML
    private TextArea tagTextBox;

    @FXML
    private TextField fileTextBox;

    @FXML
    private Button fileButton;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    @FXML
    private void initialize(){
        String newQuery = "SELECT courname FROM project1.course;";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            List<String> courses = new ArrayList<>();
            PreparedStatement preparedStatement = connection.prepareStatement(newQuery);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                String curClass = rs.getString(1);
                courses.add(curClass);
            }
            ObservableList<String> courseList = FXCollections.observableArrayList(courses);
            courseComboBox.getItems().clear();
            courseComboBox.setItems(courseList);
            preparedStatement.close();
            connection.close();
            rs.close();

        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currStage = (Stage) cancelButton.getScene().getWindow();
        currStage.close();
    }

    @FXML
    void handleChooseFile(ActionEvent event) {
        Stage currStage = (Stage) fileButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File to be Uploaded");
        File file = fileChooser.showOpenDialog(currStage);
        if (file != null && file.exists()) {
            try {
                filePath = file.getCanonicalPath();
                fileTextBox.setText(filePath);
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            Alert fileAlert = new Alert(Alert.AlertType.WARNING);
            fileAlert.setTitle("Invalid File");
            fileAlert.setHeaderText("Invalid File");
            fileAlert.setContentText("The file could not be accessed!");
            fileAlert.showAndWait();
        }
    }

    @FXML
    void handleOk(ActionEvent event) {
        Integer returnedId = null;
        if (checkInputs()){
            String newQuery = "INSERT INTO document (title,docdesc,filetype,uploader,uploaddate,docfile) " + "VALUES (?,?,?,?,?,?)";
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                connection.setAutoCommit(false);
                File newfile = new File(fileTextBox.getText());
                FileInputStream inputStream = new FileInputStream(newfile);
                PreparedStatement preparedStatement = connection.prepareStatement(newQuery,Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1,titleTextBox.getText());
                preparedStatement.setString(2,descTextBox.getText());
                String extension = FilenameUtils.getExtension(fileTextBox.getText());
                preparedStatement.setString(3,extension);
                preparedStatement.setString(4,email);
                preparedStatement.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
                //preparedStatement.setString(6,courseComboBox.getSelectionModel().getSelectedItem());
                preparedStatement.setBlob(6,inputStream);
                preparedStatement.executeUpdate();
                ResultSet keys = preparedStatement.getGeneratedKeys();
                keys.next();
                returnedId = keys.getInt(1);
                preparedStatement.close();
                keys.close();
                Stage currStage = (Stage) okButton.getScene().getWindow();
                currStage.close();
                Integer courseId = getCourseId(courseComboBox.getSelectionModel().getSelectedItem(),connection);
                updateIntermediateTable(returnedId,courseId,connection);
                connection.commit();
                connection.close();
            }  catch (IOException e){
                System.out.print("IOException");
            }catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);

            }
        }


    }

    private Integer getCourseId(String courseNum,Connection connection) throws SQLException{
        String courseQuery = "SELECT idcourse FROM project1.course WHERE courname = ?";
        PreparedStatement ps = connection.prepareStatement(courseQuery);
        ps.setString(1,courseNum);
        ResultSet rs  = ps.executeQuery();
        rs.next();
        Integer courseId = rs.getInt(1);
        ps.close();
        rs.close();
        return courseId;

    }

    private void updateIntermediateTable(Integer docId,Integer courseId, Connection connection) throws SQLException{
        String courDocQuery = "INSERT INTO courdoc (idcourse,iddocument) " + "VALUES (?,?)";
        String instrDocQuery = "INSERT INTO instrdoc (idinstructor,iddocument) " + "VALUES (?,?)";
        String checkCourQuery = "SELECT count(*) FROM project1.instrcour where idinstructor = ? AND idcourse = ?;";
        String instrCourQuery = "INSERT INTO instrcour (idinstructor,idcourse) " + "VALUES (?,?)";
        PreparedStatement psCD = connection.prepareStatement(courDocQuery);
        psCD.setInt(1,courseId);
        psCD.setInt(2,docId);
        psCD.executeUpdate();

        PreparedStatement psID = connection.prepareStatement(instrDocQuery);
        psID.setInt(1,instrId);
        psID.setInt(2,docId);
        psID.executeUpdate();


        PreparedStatement psCCheck = connection.prepareStatement(checkCourQuery);
        psCCheck.setInt(1,instrId);
        psCCheck.setInt(2,courseId);
        ResultSet rs = psCCheck.executeQuery();
        rs.next();
        Integer results = rs.getInt(1);
        if (results > 0) {
            PreparedStatement psIC = connection.prepareStatement(instrCourQuery);
            psIC.setInt(1, instrId);
            psIC.setInt(2, courseId);
            psIC.executeUpdate();
            psIC.close();
        }
        psCD.close();
        psID.close();
    }

    private Boolean checkInputs(){
        String errorMessage = "";
        if (titleTextBox.getText() == null || titleTextBox.getText().length() == 0){
            errorMessage += "- Not a valid title.\n";
        }
        if (descTextBox.getText() == null || descTextBox.getText().length() == 0){
            errorMessage += "- Not a valid description.\n";
        }
        if (courseComboBox.getSelectionModel().getSelectedItem() == null){
            errorMessage += "- Please select an option from course combobox.\n";
        }
        if (fileTextBox.getText() == null || fileTextBox.getText().length() == 0){
            errorMessage += "- Please select a file.\n";
        }

        if (errorMessage.length() == 0){
            return true;
        }else{
            Alert inputAlert = new Alert(Alert.AlertType.WARNING);
            inputAlert.setTitle("Incorrect Inputs");
            inputAlert.setHeaderText("Some of the inputs are incorrect");
            inputAlert.setContentText(errorMessage);
            inputAlert.showAndWait();
            return false;
        }
    }

    public void setEmailandID(String email,Integer instrId){
        this.email = email;
        this.instrId = instrId;
    }
}
