package project1.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CopyFileController {

    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
    private String username = "root";
    private String password = "admin";
    private Integer instrId;
    private String currCour;
    private Integer docId;

    @FXML
    private ComboBox<String> copyComboBox;

    @FXML
    private Button cancelButton;

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

    public void setIdAndCurrCour(Integer instrId,String currCour,Integer docId){
        this.instrId = instrId;
        this.currCour = currCour;
        this.docId = docId;
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
                Integer courseId = rs.getInt(1);
                String checkCourQuery = "SELECT COUNT";
            } catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);

            }
        }

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
