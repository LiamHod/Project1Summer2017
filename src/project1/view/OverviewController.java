package project1.view;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import project1.MainApp;
import project1.model.DocFile;
import sample.Controller;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

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

    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
    private String username = "root";
    private String password = "admin";
    private String email;
    private MainApp mainApp;
    private LoginController logCont;
    private Stage mainGui;
    private Stage currentStage;
    private SplitPane mainLayout;
    private Scene scene;
    private Integer instrId;
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

    public ContextMenu fileContextMenu(){
        ContextMenu rightClickMenu = new ContextMenu();
        MenuItem downloadMenu = new MenuItem("Download...");
        MenuItem copytoMenu = new MenuItem("Copy to...");
        MenuItem addtagMenu = new MenuItem("Add Tag...");
        MenuItem shareMenu = new MenuItem("Share...");
        MenuItem deleteMenu = new MenuItem("Delete File");
        rightClickMenu.getItems().addAll(downloadMenu,copytoMenu,addtagMenu,shareMenu,deleteMenu);
        rightClickMenu.setAutoHide(true);
        downloadMenu.setOnAction(e -> downloadFile());
        copytoMenu.setOnAction(e -> copyFile());
        shareMenu.setOnAction(e -> shareFile());
        return rightClickMenu;
    }

    public ContextMenu notfileContextMenu(){
        ContextMenu rightClickMenu = new ContextMenu();
        MenuItem addfileMenu = new MenuItem("Add File...");
        MenuItem searchMenu = new MenuItem("Search...");
        rightClickMenu.getItems().addAll(addfileMenu,searchMenu);
        rightClickMenu.setAutoHide(true);
        addfileMenu.setOnAction(e -> {
            openAddFile();
        });
        return rightClickMenu;
    }

    public void openAddFile(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddFile.fxml"));
            AnchorPane addFilePage = (AnchorPane)fxmlLoader.load();
            Stage fileStage = new Stage();
            fileStage.setTitle("Add File");
            fileStage.initModality(Modality.WINDOW_MODAL);
            fileStage.initOwner(currentStage);
            Scene fileScene = new Scene(addFilePage);
            AddFileDialogController fileController = fxmlLoader.getController();
            fileController.setEmailandID(email,instrId);
            fileStage.setScene(fileScene);
            fileStage.showAndWait();
            loadData();
            runPage();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void downloadFile(){
        FileChooser fileChooser = new FileChooser();
        //FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        //fileChooser.getExtensionFilters().add(extFilter);
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
            copyFileController.setIdAndCurrCour(instrId,classes.getSelectionModel().getSelectedItem(),selFile.getDocid());
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
            shareFileController.initFileIdAndUserId(selFile.getDocid(),instrId,classes.getSelectionModel().getSelectedItem());
            shareStage.setScene(shareScene);
            shareStage.showAndWait();
            loadData();
            runPage();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setLogCont(LoginController logCont){
        this.logCont = logCont;

    }
    public Integer getUserId(){
        return this.instrId;
    }

    public void loadData(){
        String newQuery = "SELECT DISTINCT courname FROM project1.course,project1.instrcour WHERE idinstructor = ? AND course.idcourse = instrcour.idcourse;";
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

    public void setUserId(Integer userId,String email){
        //System.out.print("This is in setUserId:   ");
        //System.out.println(userId);
        this.instrId = userId;
        this.email = email;
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
            //System.out.println(newvalue);
            if (newvalue == null){
                queryFiles(oldvalue);
            }else {
                queryFiles(newvalue);
            }
            files.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            //files.setItems(FXCollections.observableList(fileList));
            files.setItems(fileList);
        });

    }

    public ObservableList<DocFile> queryFiles(String courseName){
        List<String> fileTemp = new ArrayList<String>();

        String fileQueryStr = "SELECT document.iddocument,title,uploader FROM instructor,document,course,courdoc,instrdoc WHERE " +
                "instructor.idinstructor = instrdoc.idinstructor AND instrdoc.iddocument = document.iddocument AND " +
                "courdoc.iddocument = document.iddocument AND courdoc.idcourse = course.idcourse AND course.courname = ?" +
                "AND instructor.idinstructor = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(fileQueryStr);
            preparedStatement.setString(1, courseName);
            preparedStatement.setInt(2,instrId);
            System.out.println(preparedStatement);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                Integer curFileID = rs.getInt(1);
                String curFileTitle = rs.getString(2);
                String curFileUploader = rs.getString(3);
                fileList.add(new DocFile(curFileID,curFileTitle,curFileUploader));
                //fileTemp.add(curFile);
            }
            //System.out.print("This is fileList: ");
            //System.out.println(fileList);
            preparedStatement.close();
            connection.close();
            rs.close();
            return fileList;
            //ObservableList<String> fileList = FXCollections.observableArrayList(fileTemp);


        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

//    public void loadClasses(){
//        String newQuery = "SELECT name FROM project1.course,project1.instrcour WHERE idinstructor = ? AND course.idcourse = instrcour.idcourse;";
//        try (Connection connection = DriverManager.getConnection(url, username, password)) {
//            System.out.println("Database connected!");
//            PreparedStatement preparedStatement = connection.prepareStatement(newQuery);
//            preparedStatement.setInt(1, instrId);
//            ResultSet rs = preparedStatement.executeQuery();
//            rs.next();
//            //
//            //System.out.println(rs.getString(1));
//            String curClass = rs.getString(1);
//            System.out.println("Current class::::::");
//            System.out.println(curClass);
//            //values.add(curClass); //??????
//            //}
//            System.out.print("Course list: ");
//            System.out.println(courseList);
//            System.out.print("Classes list: ");
//            System.out.println(classes);
//            preparedStatement.close();
//            connection.close();
//            rs.close();
//        }catch (SQLException e) {
//            throw new IllegalStateException("Cannot connect the database!", e);
//        }
//        //classes.setItems(FXCollections.observableList(Arrays.asList("1","2")));
//    }
}
