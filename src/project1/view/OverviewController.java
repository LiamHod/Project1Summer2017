package project1.view;

//Apache Commons IO
//        Copyright 2002-2016 The Apache Software Foundation
//
//        This product includes software developed at
//        The Apache Software Foundation (http://www.apache.org/).


import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.apache.commons.io.FilenameUtils;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import project1.MainApp;
import project1.model.DBConn;
import project1.model.DBCreds;
import project1.model.DocFile;
import sample.Controller;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class OverviewController{

    @FXML
    private ListView<String> classes;

    @FXML
    private TableView<DocFile> files;

    @FXML
    private TableColumn<DocFile, Integer> idColumn;

    @FXML
    private TableColumn<DocFile, String> titleColumn;

    @FXML
    private TableColumn<DocFile, String> uploaderColumn;

    @FXML
    private TableColumn<DocFile, String> uploadDateColumn;

    @FXML
    private Hyperlink passwordChangeLink;

    @FXML
    private Hyperlink adminHyperLink;

//    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
//    private String username = "root";
//    private String password = "admin";
    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private String email;
    private MainApp mainApp;
    private LoginController logCont;
    private Stage mainGui;
    private Stage currentStage;
    private SplitPane mainLayout;
    private Scene scene;
    private Integer instrId;
    private Integer admin;
    private List<String> values;
    private DocFile selFile;
    private ObservableList<String> courseList = FXCollections.observableArrayList();
    private ObservableList<DocFile> fileList = FXCollections.observableArrayList();


    public OverviewController(){

    }

    @FXML
    public void initialize(){
        idColumn.setCellValueFactory(cellData -> cellData.getValue().docidProperty().asObject());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().docnameProperty());
        uploaderColumn.setCellValueFactory(cellData -> cellData.getValue().docdescProperty());
        uploadDateColumn.setCellValueFactory(cellData -> cellData.getValue().docdateaddedProperty());
        ContextMenu rightClickMenu = fileContextMenu();
        ContextMenu addFileMenu = notfileContextMenu();

        files.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton() == MouseButton.SECONDARY && (selFile = files.getSelectionModel().getSelectedItem()) != null){
                    //rightClickMenu.show(files,event.getScreenX(),event.getScreenY());
                    System.out.println(selFile);
                    files.getSelectionModel().clearSelection();
                    files.setContextMenu(rightClickMenu);

                }else if (event.getButton() == MouseButton.SECONDARY){
                    files.getSelectionModel().clearSelection();
                    files.setContextMenu(addFileMenu);
                }
            }
        });

    }

    @FXML
    void handlePassChange(ActionEvent event) {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChangePass.fxml"));
            AnchorPane changePassPage = (AnchorPane)fxmlLoader.load();
            Stage changePassStage = new Stage();
            changePassStage.setTitle("Change Password");
            changePassStage.initModality(Modality.WINDOW_MODAL);
            changePassStage.initOwner(currentStage);
            Scene changePassScene = new Scene(changePassPage);
            ChangePassController changePassController = fxmlLoader.getController();
            changePassController.initValues(instrId);
            changePassStage.setScene(changePassScene);
            changePassStage.showAndWait();
            loadData();
            runPage();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    void handleAdmin(ActionEvent event) {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Admin.fxml"));
            AnchorPane adminPage = (AnchorPane)fxmlLoader.load();
            Stage adminStage = new Stage();
            adminStage.setTitle("Admin");
            adminStage.initModality(Modality.WINDOW_MODAL);
            adminStage.initOwner(currentStage);
            Scene adminScene = new Scene(adminPage);
            AdminController adminController = fxmlLoader.getController();
            adminStage.setScene(adminScene);
            adminStage.showAndWait();
            loadData();
            runPage();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public ContextMenu fileContextMenu(){
        ContextMenu rightClickMenu = new ContextMenu();
        MenuItem renameMenu = new MenuItem("Rename...");
        MenuItem downloadMenu = new MenuItem("Download...");
        MenuItem copytoMenu = new MenuItem("Copy to...");
        MenuItem addtagMenu = new MenuItem("Add/Remove Tag...");
        MenuItem shareMenu = new MenuItem("Share...");
        MenuItem deleteMenu = new MenuItem("Delete File");
        rightClickMenu.getItems().addAll(renameMenu,downloadMenu,copytoMenu,addtagMenu,shareMenu,deleteMenu);
        rightClickMenu.setAutoHide(true);
        renameMenu.setOnAction(e -> renameFile());
        downloadMenu.setOnAction(e -> downloadFile());
        copytoMenu.setOnAction(e -> copyFile());
        shareMenu.setOnAction(e -> shareFile());
        addtagMenu.setOnAction(e -> addTag());
        deleteMenu.setOnAction(e -> deleteFile());
        return rightClickMenu;
    }

    public ContextMenu notfileContextMenu(){
        ContextMenu rightClickMenu = new ContextMenu();
        MenuItem addfileMenu = new MenuItem("Add File...");
        MenuItem searchMenu = new MenuItem("Search Files...");
        rightClickMenu.getItems().addAll(addfileMenu,searchMenu);
        rightClickMenu.setAutoHide(true);
        addfileMenu.setOnAction(e -> openAddFile());
        searchMenu.setOnAction(e -> searchFiles());
        return rightClickMenu;
    }

    public void renameFile(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Rename.fxml"));
            AnchorPane renameFilePage = (AnchorPane)fxmlLoader.load();
            Stage renameStage = new Stage();
            renameStage.setTitle("Rename File");
            renameStage.initModality(Modality.WINDOW_MODAL);
            renameStage.initOwner(currentStage);
            Scene renameScene = new Scene(renameFilePage);
            RenameController renameController = fxmlLoader.getController();
            renameController.initValues(instrId,classes.getSelectionModel().getSelectedItem(),selFile);
            renameStage.setScene(renameScene);
            renameStage.showAndWait();
            loadData();
            runPage();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void openAddFile(){
        if (classes.getSelectionModel().getSelectedItem() != null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddFile.fxml"));
                AnchorPane addFilePage = (AnchorPane) fxmlLoader.load();
                Stage fileStage = new Stage();
                fileStage.setTitle("Add File");
                fileStage.initModality(Modality.WINDOW_MODAL);
                fileStage.initOwner(currentStage);
                Scene fileScene = new Scene(addFilePage);
                AddFileDialogController fileController = fxmlLoader.getController();
                Integer courID;
                try (Connection connection = DriverManager.getConnection(url, username, password)) {
                    courID = getCourseId(connection);
                } catch (SQLException e) {
                    throw new IllegalStateException("Cannot connect the database!", e);
                }
                fileController.setEmailandID(email, instrId, courID, classes.getSelectionModel().getSelectedItem());
                fileStage.setScene(fileScene);
                fileStage.showAndWait();
                loadData();
                runPage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Alert noClassSel = new Alert(Alert.AlertType.INFORMATION);
            noClassSel.setTitle("No Class Selected");
            noClassSel.setHeaderText(null);
            noClassSel.setContentText("Please select a class first");
            noClassSel.showAndWait();
        }
    }

    public void downloadFile(){
        FileChooser fileChooser = new FileChooser();
        //FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        //fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(selFile.getDocname());
        File file = fileChooser.showSaveDialog(currentStage);
        if(file != null) {
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                String dwnQuery = "SELECT docfile FROM document WHERE iddocument = ?";
                PreparedStatement ps = connection.prepareStatement(dwnQuery);
                ps.setInt(1, selFile.getDocid());
                ResultSet rs  = ps.executeQuery();
                rs.next();
                Blob imageBlob = rs.getBlob(1);
                InputStream is = imageBlob.getBinaryStream();
                FileOutputStream fos = new FileOutputStream(file);
                int bytes = 0;
                while ((bytes = is.read()) != -1){
                    fos.write(bytes);
                }
                fos.close();
                connection.close();
                ps.close();
                rs.close();
            }  catch (IOException e){
                System.out.print("IOException");
            } catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }
        }
    }

    public void copyFile(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CopyFile.fxml"));
            AnchorPane copyFilePage = (AnchorPane)fxmlLoader.load();
            Stage copyStage = new Stage();
            copyStage.setTitle("Copy File");
            copyStage.initModality(Modality.WINDOW_MODAL);
            copyStage.initOwner(currentStage);
            Scene copyScene = new Scene(copyFilePage);
            CopyFileController copyFileController = fxmlLoader.getController();
            copyFileController.setIdAndCurrCour(instrId,classes.getSelectionModel().getSelectedItem(),selFile);
            copyFileController.loadComboBox();
            copyStage.setScene(copyScene);
            copyStage.showAndWait();
            loadData();
            runPage();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void shareFile(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ShareFile.fxml"));
            AnchorPane shareFilePage = (AnchorPane)fxmlLoader.load();
            Stage shareStage = new Stage();
            shareStage.setTitle("Share File");
            shareStage.initModality(Modality.WINDOW_MODAL);
            shareStage.initOwner(currentStage);
            Scene shareScene = new Scene(shareFilePage);
            ShareFileController shareFileController = fxmlLoader.getController();
            shareFileController.initFileIdAndUserId(selFile, instrId, classes.getSelectionModel().getSelectedItem());
            shareStage.setScene(shareScene);
            shareStage.showAndWait();
            loadData();
            runPage();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void addTag(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddTag.fxml"));
            AnchorPane addTagPage = (AnchorPane)fxmlLoader.load();
            Stage addTagStage = new Stage();
            addTagStage.setTitle("Add/Remove Tags");
            addTagStage.initModality(Modality.WINDOW_MODAL);
            addTagStage.initOwner(currentStage);
            Scene addTagScene = new Scene(addTagPage);
            AddTagController addTagController = fxmlLoader.getController();
            addTagController.initValue(selFile.getDocid());
            addTagController.populateTagTable();
            addTagStage.setScene(addTagScene);
            addTagStage.showAndWait();
            loadData();
            runPage();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void searchFiles(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Search.fxml"));
            AnchorPane searchPage = (AnchorPane)fxmlLoader.load();
            Stage searchStage = new Stage();
            searchStage.setTitle("Search Tags");
            searchStage.initModality(Modality.WINDOW_MODAL);
            searchStage.initOwner(currentStage);
            Scene searchScene = new Scene(searchPage);
            SearchController searchController = fxmlLoader.getController();
            searchController.initValue(instrId);
            searchStage.setScene(searchScene);
            searchStage.showAndWait();
            loadData();
            runPage();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void deleteFile(){
        Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
        deleteAlert.setTitle("Delete This File?");
        deleteAlert.setHeaderText("You are about to delete this file");
        deleteAlert.setContentText("Are you sure you want to delete this file?");
        String deleteQuery = "DELETE FROM document WHERE iddocument NOT IN (SELECT iddocument FROM instrcourdoc);";
        String deleteInterQuery = "DELETE FROM instrcourdoc WHERE iddocument = ? AND idinstructor = ? AND idcourse = ? ;";
        String deleteTagQuery = "DELETE FROM doctag WHERE iddocument = ?";
        //String deleteTagMain = "DELETE FROM tag WHERE ? NOT IN (SELECT idtag FROM doctag)";
        Optional<ButtonType> result = deleteAlert.showAndWait();
        if (result.get() == ButtonType.OK){
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                Integer courseId = getCourseId(connection);
                connection.setAutoCommit(false);
                PreparedStatement ps1 = connection.prepareStatement(deleteInterQuery);
                ps1.setInt(1,selFile.getDocid());
                ps1.setInt(2,instrId);
                ps1.setInt(3,courseId);
                ps1.executeUpdate();
                ps1.close();
                //if (docCheck(connection)){
                PreparedStatement ps = connection.prepareStatement(deleteQuery);
                //ps.setInt(1,selFile.getDocid());
                ps.executeUpdate();
                ps.close();

                PreparedStatement psIntTag = connection.prepareStatement(deleteTagQuery);
                psIntTag.setInt(1,selFile.getDocid());
                psIntTag.executeUpdate();
                psIntTag.close();
                //}
                connection.commit();
                connection.setAutoCommit(true);
                connection.close();
            }catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }
        } else {
            deleteAlert.close();
        }
        loadData();
        runPage();
    }

    private Integer getCourseId(Connection connection) throws SQLException{
        String courseQuery = "SELECT idcourse FROM project1.course WHERE courname = ?";
        PreparedStatement ps = connection.prepareStatement(courseQuery);
        ps.setString(1,classes.getSelectionModel().getSelectedItem());
        ResultSet rs = ps.executeQuery();
        rs.next();
        Integer courseId = rs.getInt(1);
        ps.close();
        rs.close();
        return courseId;
    }

    private Boolean docCheck(Connection connection) throws SQLException{
        String deleteCheck = "SELECT count(*) FROM project1.instrcourdoc WHERE iddocument = ?;";
        PreparedStatement ps = connection.prepareStatement(deleteCheck);
        ps.setInt(1,selFile.getDocid());
        ResultSet rs = ps.executeQuery();
        rs.next();
        Integer results = rs.getInt(1);
        ps.close();
        rs.close();
        return (results < 1);
    }


    public void setLogCont(LoginController logCont){
        this.logCont = logCont;

    }
    public Integer getUserId(){
        return this.instrId;
    }

    public void loadData(){
        //String newQuery = "SELECT DISTINCT courname FROM project1.course,project1.instrcour WHERE idinstructor = ? AND course.idcourse = instrcour.idcourse;";
        String newQuery = "SELECT DISTINCT courname FROM project1.course,project1.instrcourdoc WHERE idinstructor = ? AND course.idcourse = instrcourdoc.idcourse;";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            List<String> values = new ArrayList<>();
            PreparedStatement preparedStatement = connection.prepareStatement(newQuery);
            preparedStatement.setInt(1, instrId);
            //preparedStatement.setInt(1, 1);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                String curClass = rs.getString(1);
                values.add(curClass);
            }

            preparedStatement.close();
            connection.close();
            rs.close();
            ObservableList<String> courseList = FXCollections.observableArrayList(values);
            classes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            //classes.setItems(courseList);
            //assert classes != null : "fx:id=classes was not injected.";
            //List<String> values = Arrays.asList("one", "two", "three");
            classes.setItems(FXCollections.observableList(courseList));
        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    public void setUserId(Integer userId,String email, Integer admin){
        //System.out.print("This is in setUserId:   ");
        //System.out.println(userId);
        this.instrId = userId;
        this.email = email;
        this.admin = admin;
        if (admin == 1){
            adminHyperLink.setVisible(true);
        }
    }

    public void initMainScreen(MainApp mainApp,Stage mainGui,Stage currentStage){
        this.currentStage = currentStage;
        this.mainApp = mainApp;
        this.mainGui = mainGui;
    }

    public void runPage(){
        //https://www.youtube.com/watch?v=WZGyP57IH6M&list=PL6gx4Cwl9DGBzfXLWLSYVy8EbTdpGbUIG&index=13
        classes.getSelectionModel().selectedItemProperty().addListener( (v, oldvalue, newvalue) -> {
            files.getItems().clear();
            if (newvalue == null){
                queryFiles(oldvalue);
            }else {
                queryFiles(newvalue);
            }
            files.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            //SortedList<DocFile> sortedData = new SortedList<>(fileList);
            //sortedData.comparatorProperty().bind(files.comparatorProperty());
            //files.setItems(FXCollections.observableList(fileList));
            files.setItems(fileList);
        });

    }

    public ObservableList<DocFile> queryFiles(String courseName){
        String fileQueryStr = "SELECT document.iddocument,title,uploader,uploaddate FROM instructor,document,course,instrcourdoc WHERE " +
                "instructor.idinstructor = instrcourdoc.idinstructor AND instrcourdoc.iddocument = document.iddocument AND " +
                "instrcourdoc.iddocument = document.iddocument AND instrcourdoc.idcourse = course.idcourse AND course.courname = ?" +
                "AND instructor.idinstructor = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(fileQueryStr);
            preparedStatement.setString(1, courseName);
            preparedStatement.setInt(2,instrId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                Integer curFileID = rs.getInt(1);
                String curFileTitle = rs.getString(2);
                String curFileUploader = rs.getString(3);
                Date curUploadDate = rs.getDate(4);
                fileList.add(new DocFile(curFileID,curFileTitle,curFileUploader, curUploadDate));
            }
            preparedStatement.close();
            connection.close();
            rs.close();
            return fileList;
        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }
}
