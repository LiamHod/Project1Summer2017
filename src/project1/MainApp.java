package project1;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import project1.view.LoginController;
import java.io.IOException;

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

    /**
     * Initalized the whole layout
     */
    public void initlayout(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Login.fxml"));
            rootLayout = (AnchorPane) loader.load();
            Scene scene = new Scene(rootLayout);
            mainGui.setScene(scene);
            mainGui.show();

            LoginController controller = loader.getController();
            controller.setMainApp(this,mainGui);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
