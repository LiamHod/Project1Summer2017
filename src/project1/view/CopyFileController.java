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
import project1.model.DBCreds;
import project1.model.DocFile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class CopyFileController {

//    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
//    private String username = "root";
//    private String password = "admin";
    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private Integer instrId;
    private Integer courseId;
    private String currCour;
    private String selFileName;
    private Integer docId;

    @FXML
    private ComboBox<String> copyComboBox;

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
        String newQuery = "SELECT courname FROM project1.course WHERE courname != ?;";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            List<String> copylist = new ArrayList<>();
            PreparedStatement ps = connection.prepareStatement(newQuery);
            ps.setString(1, currCour);
            System.out.println(currCour);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String curClass = rs.getString(1);
                copylist.add(curClass);
            }
            ObservableList<String> fullCopyList = FXCollections.observableArrayList(copylist);
            //copyComboBox.getItems().clear();
            copyComboBox.setItems(fullCopyList);

            connection.close();
            ps.close();
            rs.close();

        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    public void setIdAndCurrCour(Integer instrId, String currCour, DocFile selFile){
        this.instrId = instrId;
        this.currCour = currCour;
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
                String courseQuery = "SELECT idcourse FROM project1.course WHERE courname = ?";
                PreparedStatement ps = connection.prepareStatement(courseQuery);
                ps.setString(1, copyComboBox.getSelectionModel().getSelectedItem());
                ResultSet rs = ps.executeQuery();
                rs.next();
                courseId = rs.getInt(1);
//                String checkCourQuery = "SELECT count(*) FROM project1.courdoc,project1.instrdoc where courdoc.iddocument = ?" +
//                        " AND courdoc.idcourse = ?  AND instrdoc.iddocument = ? AND instrdoc.idinstructor = ?;";
                String checkCourQuery = "SELECT count(*) FROM project1.instrcourdoc where instrcourdoc.iddocument = ?" +
                        " AND instrcourdoc.idcourse = ? AND instrcourdoc.idinstructor = ?;";
                ps = connection.prepareStatement(checkCourQuery);
                ps.setInt(1,docId);
                ps.setInt(2,courseId);
                ps.setInt(3,instrId);
                rs = ps.executeQuery();
                rs.next();
                Integer dupVal = rs.getInt(1);
                if (dupVal > 0) {
                    selFileName = checkFileName(connection, selFileName);
                    connection.setAutoCommit(false);
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
                Integer alreadyCour = checkCourse(connection, courseId);
                //String courDocQuery = "INSERT INTO courdoc (idcourse,iddocument) " + "VALUES (?,?)";
                //String instrCourQuery = "INSERT INTO instrcour (idinstructor,idcourse) " + "VALUES (?,?)";
                String instrCourDocQuery = "INSERT INTO instrcourdoc (idinstructor,idcourse,iddocument) " + "VALUES (?,?,?)";
                PreparedStatement psCD = connection.prepareStatement(instrCourDocQuery);
                psCD.setInt(1, instrId);
                psCD.setInt(2, courseId);
                psCD.setInt(3, docId);
                psCD.executeUpdate();
//                    PreparedStatement psCD = connection.prepareStatement(courDocQuery);
//                    psCD.setInt(1, courseId);
//                    psCD.setInt(2, docId);
//                    psCD.executeUpdate();
//
//                    if (alreadyCour <= 0) {
//                        PreparedStatement psIC = connection.prepareStatement(instrCourQuery);
//                        psIC.setInt(1, instrId);
//                        psIC.setInt(2, courseId);
//                        psIC.executeUpdate();
//                        psIC.close();
//                    }
//                    psCD.close();

                successAlert();

                ps.close();
                rs.close();
                connection.commit();
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
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
            if (result == 0){
                foundName = false;
            }else{
                System.out.println(fileNameWithOutExt + " - Copy" + "." + ext);
                filename = fileNameWithOutExt + " - Copy" + "." + ext;
            }
        }
        return filename;

    }

    /**
     * Pretty sure this is not useful anymore after table change
     * @param connection
     * @param courseId
     * @return
     * @throws SQLException
     */
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

    private void duplicateAlert(){
        Alert duplicateAlert = new Alert(Alert.AlertType.WARNING);
        duplicateAlert.setTitle("File already exists");
        duplicateAlert.setHeaderText("File already exists in selected directory");
        duplicateAlert.setContentText("- The file you chose to copy already exists in selected directory.");
        duplicateAlert.showAndWait();
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
            //errorMessage += "- Please select an option from course combobox.\n";
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
