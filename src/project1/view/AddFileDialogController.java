package project1.view;

//Apache Commons IO
//        Copyright 2002-2016 The Apache Software Foundation
//
//        This product includes software developed at
//        The Apache Software Foundation (http://www.apache.org/).

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import project1.model.Courses;
import project1.model.DBCreds;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;


public class AddFileDialogController {

    private String filePath;
    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private String email;
    private Integer instrId;
    private Integer courseId;
    private String courname;
    ObservableList<Courses> courseList = FXCollections.observableArrayList();

    @FXML
    private TextArea descTextBox;

    @FXML
    private ComboBox<Courses> courseComboBox;

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
        String newQuery = "SELECT * FROM course ORDER BY courname;";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(newQuery);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                Integer curClassId = rs.getInt(1);
                String curClass = rs.getString(2);
                String curClassFac = rs.getString(3);
                courseList.add(new Courses(curClassId,curClass,curClassFac));
            }
            courseComboBox.getItems().clear();
            courseComboBox.setItems(courseList);
            preparedStatement.close();
            connection.close();
            rs.close();

        }catch (SQLException e) {
            Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
            sqlAlert.setTitle("Error loading courses");
            sqlAlert.setHeaderText(null);
            sqlAlert.setContentText("The program encountered an error and couldn't load the courses, check your connection and please try again");
            sqlAlert.showAndWait();
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
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"));
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
                Integer courseId = courseComboBox.getSelectionModel().getSelectedItem().getId();
                String okFileName = checkFileName(connection,newfile.getName(), courseId);
                FileInputStream inputStream = new FileInputStream(newfile);
                PreparedStatement preparedStatement = connection.prepareStatement(newQuery,Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1,okFileName);
                preparedStatement.setString(2,descTextBox.getText());
                String extension = FilenameUtils.getExtension(fileTextBox.getText());
                preparedStatement.setString(3,extension);
                preparedStatement.setString(4,email);
                preparedStatement.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
                preparedStatement.setBlob(6,inputStream);
                preparedStatement.executeUpdate();
                ResultSet keys = preparedStatement.getGeneratedKeys();
                keys.next();
                returnedId = keys.getInt(1);
                preparedStatement.close();
                keys.close();
                Stage currStage = (Stage) okButton.getScene().getWindow();
                currStage.close();
                connection.setAutoCommit(false);
                updateIntermediateTable(returnedId,courseId,connection);
                connection.commit();
                connection.setAutoCommit(true);
                updateTags(connection,returnedId);
                connection.close();
            }  catch (IOException e){
                System.out.print("IOException");
            }catch (SQLException e) {
                Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                sqlAlert.setTitle("Error adding file");
                sqlAlert.setHeaderText(null);
                sqlAlert.setContentText("The program encountered an error and couldn't add file, check your connection and please try again");
                sqlAlert.showAndWait();
                Stage currStage = (Stage) okButton.getScene().getWindow();
                currStage.close();
                throw new IllegalStateException("Cannot connect the database!", e);

            }
        }
    }

    private String checkFileName(Connection connection, String filename, Integer courseId) throws SQLException{
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

    private void updateIntermediateTable(Integer docId,Integer courseId, Connection connection) throws SQLException{
        String instrCourDocQuery = "INSERT INTO instrcourdoc (idinstructor,idcourse,iddocument) " + "VALUES (?,?,?)";
        PreparedStatement psICD = connection.prepareStatement(instrCourDocQuery);
        psICD.setInt(1,instrId);
        psICD.setInt(2,courseId);
        psICD.setInt(3,docId);
        psICD.executeUpdate();
        psICD.close();
    }

    private Boolean checkInputs(){
        String errorMessage = "";
        if (fileTextBox.getText() == null || fileTextBox.getText().length() == 0){
            errorMessage += "- Please select a file.\n";
        }
        if (courseComboBox.getSelectionModel().getSelectedItem() == null){
            errorMessage += "- Please select a course from dropdown box.\n";
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

    public void setEmailandID(String email,Integer instrId, Courses selCourse){
        this.email = email;
        this.instrId = instrId;
        if (selCourse != null) {
            this.courseId = selCourse.getId();
            this.courname = selCourse.getName();
        }
        for (Courses curCourse: courseList){
            if (courseId != null && curCourse.getId() == courseId){
                courseComboBox.getSelectionModel().select(curCourse);

            }
        }

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
