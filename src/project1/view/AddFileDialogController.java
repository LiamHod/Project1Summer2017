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
import project1.model.DBCreds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddFileDialogController {

    private String filePath;
//    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
//    private String username = "root";
//    private String password = "admin";
    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private String email;
    private Integer instrId;
    private Integer courseId;
    private String courname;

    @FXML
    private TextArea descTextBox;

    @FXML
    private Label courseLabel;

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
//        String newQuery = "SELECT courname FROM project1.course;";
//        try (Connection connection = DriverManager.getConnection(url, username, password)) {
//            List<String> courses = new ArrayList<>();
//            PreparedStatement preparedStatement = connection.prepareStatement(newQuery);
//            ResultSet rs = preparedStatement.executeQuery();
//            while (rs.next()){
//                String curClass = rs.getString(1);
//                courses.add(curClass);
//            }
//            ObservableList<String> courseList = FXCollections.observableArrayList(courses);
//            courseComboBox.getItems().clear();
//            courseComboBox.setItems(courseList);
//            preparedStatement.close();
//            connection.close();
//            rs.close();
//
//        }catch (SQLException e) {
//            throw new IllegalStateException("Cannot connect the database!", e);
//        }
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
            System.out.println(file.length());
            if (file.length() > 10485760){
                Alert fileAlert = new Alert(Alert.AlertType.WARNING);
                fileAlert.setTitle("File too large");
                fileAlert.setHeaderText(null);
                fileAlert.setContentText("Please choose a smaller file or zip the current file!");
                fileAlert.showAndWait();
                return;
            }
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
                File newfile = new File(fileTextBox.getText());
                String okFileName = checkFileName(connection,newfile.getName());
                FileInputStream inputStream = new FileInputStream(newfile);
                PreparedStatement preparedStatement = connection.prepareStatement(newQuery,Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1,okFileName);
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
                //Integer courseId = getCourseId(courseComboBox.getSelectionModel().getSelectedItem(),connection);
                Integer results = checkCourse(connection,courseId);
                connection.setAutoCommit(false);
                updateIntermediateTable(returnedId,courseId,connection,results);
                connection.commit();
                connection.setAutoCommit(true);
                updateTags(connection,returnedId);
                connection.close();
            }  catch (IOException e){
                System.out.print("IOException");
            }catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);

            }
        }
    }

    private String checkFileName(Connection connection, String filename) throws SQLException{
        String fileExistQuery = "SELECT COUNT(*) FROM document,instrcourdoc WHERE instrcourdoc.iddocument = " +
                "document.iddocument AND instrcourdoc.idinstructor = ? AND instrcourdoc.idcourse = ? AND title = ?";
        String ext = FilenameUtils.getExtension(filename);
        PreparedStatement ps = connection.prepareStatement(fileExistQuery);
        boolean foundName = true;
        while (foundName){
            String fileNameWithOutExt = FilenameUtils.removeExtension(filename);
            ps.setInt(1,instrId);
            ps.setInt(2,courseId);
            ps.setString(3,filename);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int result = rs.getInt(1);
            if (result == 0){
                foundName = false;
            }else{
                System.out.println(fileNameWithOutExt + " - Copy" + "." + ext);
                filename = fileNameWithOutExt + " - Copy" + "." + ext;
            }
        }
        ps.close();
        return filename;

    }

    private Integer checkCourse(Connection connection,Integer courseId) throws SQLException{
        //String checkCourQuery = "SELECT count(*) FROM project1.instrcour where idinstructor = ? AND idcourse = ?;";
        String checkCourQuery = "SELECT count(*) FROM project1.instrcourdoc where idinstructor = ? AND idcourse = ?;";
        PreparedStatement psCCheck = connection.prepareStatement(checkCourQuery);
        psCCheck.setInt(1,instrId);
        psCCheck.setInt(2,courseId);
        ResultSet rs = psCCheck.executeQuery();
        rs.next();
        Integer results = rs.getInt(1);
        System.out.println(results);
        return results;
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

    private void updateIntermediateTable(Integer docId,Integer courseId, Connection connection,Integer results) throws SQLException{
//        String courDocQuery = "INSERT INTO courdoc (idcourse,iddocument) " + "VALUES (?,?)";
//        String instrDocQuery = "INSERT INTO instrdoc (idinstructor,iddocument) " + "VALUES (?,?)";
//        String instrCourQuery = "INSERT INTO instrcour (idinstructor,idcourse) " + "VALUES (?,?)";
//        PreparedStatement psCD = connection.prepareStatement(courDocQuery);
//        psCD.setInt(1,courseId);
//        psCD.setInt(2,docId);
//        psCD.executeUpdate();
//
//        PreparedStatement psID = connection.prepareStatement(instrDocQuery);
//        psID.setInt(1,instrId);
//        psID.setInt(2,docId);
//        psID.executeUpdate();
//
//        if (results <= 0) {
//            PreparedStatement psIC = connection.prepareStatement(instrCourQuery);
//            psIC.setInt(1, instrId);
//            psIC.setInt(2, courseId);
//            psIC.executeUpdate();
//            psIC.close();
//        }
//        psCD.close();
//        psID.close();
        String instrCourDocQuery = "INSERT INTO instrcourdoc (idinstructor,idcourse,iddocument) " + "VALUES (?,?,?)";
        PreparedStatement psICD = connection.prepareStatement(instrCourDocQuery);
        psICD.setInt(1,instrId);
        psICD.setInt(2,courseId);
        psICD.setInt(3,docId);
        psICD.executeUpdate();
    }

    private Boolean checkInputs(){
        String errorMessage = "";
        if (fileTextBox.getText() == null || fileTextBox.getText().length() == 0){
            errorMessage += "- Please select a file.\n";
        }
        if (tagTextBox.getText() == null || tagTextBox.getText().length() == 0){
            errorMessage += "- Please enter at least one tag.\n";
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

    public void setEmailandID(String email,Integer instrId, Integer courID, String courname){
        this.email = email;
        this.instrId = instrId;
        this.courseId = courID;
        this.courname = courname;
        courseLabel.setText(courname);
        courseLabel.setVisible(true);
    }

    private void updateTags(Connection connection,Integer docId) throws SQLException{
        String totalTags = tagTextBox.getText();
        Integer tagId;
        String newQuery = "INSERT INTO tag (tagname) " + "VALUES (?)";
        String tagIntQuery = "INSERT INTO doctag(iddocument,idtag) " + "VALUES (?,?)";
        String tagExistsQuery = "SELECT idtag FROM tag WHERE tagname = ?;";
        PreparedStatement ps = connection.prepareStatement(newQuery,Statement.RETURN_GENERATED_KEYS);
        PreparedStatement ps2 = connection.prepareStatement(tagIntQuery);
        PreparedStatement existsps = connection.prepareStatement(tagExistsQuery);
        String[] sepTags = totalTags.split(" ");
        Integer maxVal;
        if (sepTags.length >= 10){
            maxVal = 10;
        }else{
            maxVal = sepTags.length;
        }
        for (int i = 0; i < maxVal; i++){
            connection.setAutoCommit(false);
            existsps.setString(1,sepTags[i]);
            ResultSet existsRs = existsps.executeQuery();
            if (existsRs.next()){
                tagId = existsRs.getInt(1);
                existsRs.close();

            }else {
                ps.setString(1, sepTags[i]);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                tagId = keys.getInt(1);
                keys.close();
            }
            ps2.setInt(1,docId);
            ps2.setInt(2,tagId);
            ps2.executeUpdate();
            connection.commit();
            connection.setAutoCommit(true);
        }
        ps.close();
        ps2.close();
        existsps.close();

    }
}
