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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import project1.model.Courses;
import project1.model.DBCreds;
import project1.model.DocFile;
import java.sql.*;



public class CopyFileController {

    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private Integer instrId;
    private Integer courseId;
    private Integer oldCourseId;
    private String currCourName;
    private String selFileName;
    private Integer docId;
    private ObservableList<Courses> courseList = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Courses> copyComboBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Label fileNameLabel;

    @FXML
    private Button okButton;

    @FXML
    private void initialize(){
    }

    public void loadComboBox(){
        String newQuery = "SELECT * FROM course WHERE idcourse != ? ORDER BY courname;";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement ps = connection.prepareStatement(newQuery);
            ps.setInt(1,oldCourseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                Integer curClassId = rs.getInt(1);
                String curClass = rs.getString(2);
                String curClassFac = rs.getString(3);
                courseList.add(new Courses(curClassId,curClass,curClassFac));
            }
            copyComboBox.getItems().clear();
            copyComboBox.setItems(courseList);
            ps.close();
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

    public void setIdAndCurrCour(Integer instrId, Courses currCour, DocFile selFile){
        this.instrId = instrId;
        this.currCourName = currCour.getName();
        this.oldCourseId = currCour.getId();
        this.docId = selFile.getDocid();
        this.selFileName = selFile.getDocname();
        fileNameLabel.setText(selFileName);
        fileNameLabel.setVisible(true);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currStage = (Stage) cancelButton.getScene().getWindow();
        currStage.close();
    }

    @FXML
    void handleOk(ActionEvent event) {
        if (checkInput()) {
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                courseId = copyComboBox.getSelectionModel().getSelectedItem().getId();
                String checkCourQuery = "SELECT count(*) FROM instrcourdoc where instrcourdoc.iddocument = ?" +
                        " AND instrcourdoc.idcourse = ? AND instrcourdoc.idinstructor = ?;";
                PreparedStatement ps = connection.prepareStatement(checkCourQuery);
                ps.setInt(1,docId);
                ps.setInt(2,courseId);
                ps.setInt(3,instrId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                Integer dupVal = rs.getInt(1);
                connection.setAutoCommit(false);
                if (dupVal > 0) {
                    selFileName = checkFileName(connection, selFileName);
                    String newQuery = "INSERT INTO document (title,docdesc,filetype,uploader,uploaddate,docfile) " +
                            "SELECT ?, docdesc, filetype, uploader, uploaddate, docfile FROM document where iddocument = ?";
                    PreparedStatement psNew = connection.prepareStatement(newQuery,Statement.RETURN_GENERATED_KEYS);
                    psNew.setString(1,selFileName);
                    psNew.setInt(2, docId);
                    psNew.executeUpdate();
                    ResultSet keys = psNew.getGeneratedKeys();
                    keys.next();
                    Integer oldDocId = docId;
                    docId = keys.getInt(1);
                    transferTags(connection,oldDocId,docId);
                    copyComboBox.getSelectionModel().clearSelection();
                }
                String instrCourDocQuery = "INSERT INTO instrcourdoc (idinstructor,idcourse,iddocument) " + "VALUES (?,?,?)";
                PreparedStatement psCD = connection.prepareStatement(instrCourDocQuery);
                psCD.setInt(1, instrId);
                psCD.setInt(2, courseId);
                psCD.setInt(3, docId);
                psCD.executeUpdate();

                successAlert();

                ps.close();
                rs.close();
                connection.commit();
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                sqlAlert.setTitle("Error copying file");
                sqlAlert.setHeaderText(null);
                sqlAlert.setContentText("The program encountered an error and couldn't copy the file, check your connection and please try again");
                sqlAlert.showAndWait();
                throw new IllegalStateException("Cannot connect the database!", e);

            }
        }
        Stage currStage = (Stage) okButton.getScene().getWindow();
        currStage.close();

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
            if (result == 0) {
                foundName = false;
            }else if(ext == null){
                filename = fileNameWithOutExt + " - Copy";
            }else{
                System.out.println(fileNameWithOutExt + " - Copy" + "." + ext);
                filename = fileNameWithOutExt + " - Copy" + "." + ext;
            }
        }
        return filename;

    }

    private void transferTags(Connection connection,Integer oldDocId, Integer newDocId) throws SQLException{
        int curTagId;
        String tagQuery = "SELECT idtag FROM doctag WHERE iddocument = ?;";
        String addTagQuery = "INSERT INTO doctag (iddocument,idtag) VALUES (?,?)";
        PreparedStatement psTag = connection.prepareStatement(tagQuery);
        PreparedStatement psAddTag = connection.prepareStatement(addTagQuery);
        psTag.setInt(1, oldDocId);
        ResultSet rs = psTag.executeQuery();
        while (rs.next()){
            curTagId = rs.getInt(1);
            psAddTag.setInt(1,newDocId);
            psAddTag.setInt(2,curTagId);
            psAddTag.executeUpdate();
        }
        rs.close();
        psAddTag.close();
        psTag.close();
    }

    private void successAlert(){
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Copy Completed");
        successAlert.setHeaderText("File was Copied");
        successAlert.setContentText("- Your file was successfully copied.");
        successAlert.showAndWait();
    }

    private Boolean checkInput(){
        if (copyComboBox.getSelectionModel().getSelectedItem() == null){
            Alert comboboxAlert = new Alert(Alert.AlertType.WARNING);
            comboboxAlert.setTitle("No Option Selected");
            comboboxAlert.setHeaderText("No Option was selected");
            comboboxAlert.setContentText("- Please select an option from combobox.");
            comboboxAlert.showAndWait();
            return false;
        }else
            return true;
    }
}
