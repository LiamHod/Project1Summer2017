package project1.view;

//Apache Commons IO
//        Copyright 2002-2016 The Apache Software Foundation
//
//        This product includes software developed at
//        The Apache Software Foundation (http://www.apache.org/).

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.apache.commons.io.FilenameUtils;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import project1.model.Courses;
import project1.model.DBCreds;
import project1.model.DocFile;

import java.sql.*;

public class RenameController {

    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private Integer instrId;
    private String currClassName;
    private Integer courseId;
    private Integer docId;
    private String docName;

    @FXML
    private TextField renameTextBox;

    @FXML
    private Button renameButton;

    @FXML
    private Button cancelButton;

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currStage = (Stage) cancelButton.getScene().getWindow();
        currStage.close();
    }

    @FXML
    void handleRename(ActionEvent event) {
        if (isValid()) {
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                String curExt = FilenameUtils.getExtension(docName);
                String newFileName = checkFileName(connection, renameTextBox.getText()+"."+curExt);
                connection.setAutoCommit(false);
                String newQuery = "INSERT INTO document (title,docdesc,filetype,uploader,uploaddate,docfile) " +
                        "SELECT ?, docdesc, filetype, uploader, uploaddate, docfile FROM document where iddocument = ?";
                PreparedStatement ps = connection.prepareStatement(newQuery,Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, newFileName);
                ps.setInt(2,docId);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                Integer oldDocId = docId;
                docId = keys.getInt(1);
                transferTags(connection,oldDocId,docId);
                String instrCourDocQuery = "INSERT INTO instrcourdoc (idinstructor,idcourse,iddocument) " + "VALUES (?,?,?)";
                PreparedStatement psCD = connection.prepareStatement(instrCourDocQuery);
                psCD.setInt(1, instrId);
                psCD.setInt(2, courseId);
                psCD.setInt(3, docId);
                psCD.executeUpdate();
                removeOldFile(connection, oldDocId);
                connection.commit();
                connection.setAutoCommit(true);
                keys.close();
                ps.close();
                connection.close();
            } catch (SQLException e) {
                Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                sqlAlert.setTitle("Error renaming file");
                sqlAlert.setHeaderText(null);
                sqlAlert.setContentText("The program encountered an error and couldn't rename the file, check your connection and please try again");
                sqlAlert.showAndWait();
                throw new IllegalStateException("Cannot connect the database!", e);

            }
        }
        Stage currStage = (Stage) renameButton.getScene().getWindow();
        currStage.close();
    }

    /**
     * Initializes values for controller
     * @param instrId - user id
     * @param currClass - current selected class
     * @param selFile - current selected file
     */
    public void initValues(Integer instrId, Courses currClass, DocFile selFile){
        this.instrId = instrId;
        this.currClassName = currClass.getName();
        this.courseId = currClass.getId();
        this.docId = selFile.getDocid();
        this.docName = selFile.getDocname();
        renameTextBox.setText(FilenameUtils.getBaseName(selFile.getDocname()));
    }

    /**
     * Checks to see if file already exists, if it does it add "- Copy" to name till new filename found
     * @param connection - current connection
     * @param filename - filename to check
     * @return - new filename to use
     * @throws SQLException
     */
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
                filename = fileNameWithOutExt + " - Copy" + "." + ext;
            }
        }
        ps.close();
        return filename;

    }

    /**
     * Transfers tags to renamed file
     * @param connection - current connection
     * @param oldDocId - old file id
     * @param newDocId - new file id
     * @throws SQLException
     */
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

    /**
     * Checks to see if the old file needs to be removed (No one else is using it)
     * @param connection - current connection
     * @param oldDocId - old file id
     * @throws SQLException
     */
    private void removeOldFile(Connection connection, Integer oldDocId) throws SQLException{
        String removeQuery = "DELETE FROM instrcourdoc WHERE iddocument = ? AND idinstructor = ? AND idcourse = ?";
        String deleteQuery = "DELETE FROM document WHERE iddocument = ?;";
        PreparedStatement psRem = connection.prepareStatement(removeQuery);
        psRem.setInt(1,oldDocId);
        psRem.setInt(2,instrId);
        psRem.setInt(3,courseId);
        psRem.executeUpdate();
        String deleteCheck = "SELECT count(*) FROM instrcourdoc WHERE iddocument = ?;";
        PreparedStatement ps = connection.prepareStatement(deleteCheck);
        ps.setInt(1,oldDocId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        Integer results = rs.getInt(1);
        if (results == 0){
            PreparedStatement psDel = connection.prepareStatement(deleteQuery);
            psDel.setInt(1,oldDocId);
            psDel.executeUpdate();
            psDel.close();
        }
        rs.close();
        ps.close();
        psRem.close();
    }

    /**
     * Checks to see if inputs are valid
     * @return - boolean value of whether the inputs are valid or not
     */
    private boolean isValid(){
        if (renameTextBox.getText() == null || renameTextBox.getText().length() == 0){
            Alert inputAlert = new Alert(Alert.AlertType.WARNING);
            inputAlert.setTitle("No Name Entered");
            inputAlert.setHeaderText(null);
            inputAlert.setContentText("Please enter a new name for the file");
            inputAlert.showAndWait();
            return false;
        }else if (FilenameUtils.getExtension(renameTextBox.getText()).length() > 0){
            Alert extAlert = new Alert(Alert.AlertType.WARNING);
            extAlert.setTitle("Extension added");
            extAlert.setHeaderText(null);
            extAlert.setContentText("Please don't add extra extension to name");
            extAlert.showAndWait();
            return false;
        }else if (FilenameUtils.getBaseName(renameTextBox.getText()).equals(FilenameUtils.getBaseName(docName))){
            Stage currStage = (Stage) renameButton.getScene().getWindow();
            currStage.close();
            return false;
        }
        return true;

    }

}