package project1;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import project1.view.LoginController;
import project1.view.OverviewController;
import sun.applet.Main;

import java.io.IOException;
import java.sql.*;

public class MainApp extends Application {

    private Stage mainGui;
    private AnchorPane rootLayout;
    private SplitPane homeLayout;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.mainGui = primaryStage;
        primaryStage.setTitle("Login");
        initlayout();
    }

    public static void main(String[] args){
        launch(args);
    }

    public void initlayout(){
        try{
            //Parent root = FXMLLoader.load(getClass().getResource("view/Overview.fxml"));
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Login.fxml"));
            rootLayout = (AnchorPane) loader.load();

            Scene scene = new Scene(rootLayout);
            mainGui.setScene(scene);
            mainGui.show();

            LoginController controller = loader.getController();
            controller.setMainApp(this,mainGui);
            Integer idReturn = controller.getUserID();
            if (idReturn != null){
//                loader = new FXMLLoader();
//                loader.setLocation(MainApp.class.getResource("view/Overview.fxml"));
//                homeLayout = (SplitPane) loader.load();
//                stage.setTitle("Home Screen");
//                stage.setScene(scene);
//                stage.show();
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }



    public ObservableList<String> courseList = FXCollections.observableArrayList();
}
