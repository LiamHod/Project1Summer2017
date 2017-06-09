package project1.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import project1.model.SearchResults;
import project1.model.Tag;

import java.sql.*;

public class SearchController {

    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
    private String username = "root";
    private String password = "admin";
    private ObservableList<SearchResults> searchList = FXCollections.observableArrayList();

    @FXML
    private TextField searchTextBox;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<SearchResults> resultsTable;

    @FXML
    private TableColumn<SearchResults, String> classColumn;

    @FXML
    private TableColumn<SearchResults, String> fileColumn;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    private void initialize(){
        classColumn.setCellValueFactory(cellData -> cellData.getValue().classNameProperty());
        fileColumn.setCellValueFactory(cellData -> cellData.getValue().fileTitleProperty());
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currStage = (Stage) cancelButton.getScene().getWindow();
        currStage.close();
    }

    @FXML
    void handleOk(ActionEvent event) {
        Stage currStage = (Stage) okButton.getScene().getWindow();
        currStage.close();
    }

    @FXML
    void handleSearch(ActionEvent event) {
        resultsTable.getItems().clear();
        String searchQuery = "SELECT DISTINCT course.courname,document.title FROM project1.document,project1.course," +
                "project1.doctag,project1.tag,project1.instrcourdoc WHERE instrcourdoc.idinstructor = 1 AND " +
                "document.iddocument = instrcourdoc.iddocument AND course.idcourse = instrcourdoc.idcourse AND " +
                "tag.idtag = doctag.idtag AND document.iddocument = doctag.iddocument AND tagname = ?";
        if (checkInput()){
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                PreparedStatement ps = connection.prepareStatement(searchQuery);
                ps.setString(1,searchTextBox.getText());
                ResultSet rs = ps.executeQuery();
                while (rs.next()){
                    String curCourName = rs.getString(1);
                    String curDocTitle = rs.getString(2);
                    searchList.add(new SearchResults(curCourName,curDocTitle));
                    //fileTemp.add(curFile);
                }
                ps.close();
                rs.close();
                connection.close();
                resultsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                resultsTable.setItems(searchList);
            }catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }
        }
    }

    private Boolean checkInput(){
        if (searchTextBox.getText() == null || searchTextBox.getText().length() == 0){
            Alert searchAlert = new Alert(Alert.AlertType.WARNING);
            searchAlert.setTitle("Search box is empty");
            searchAlert.setHeaderText("Nothing in search box");
            searchAlert.setContentText("Please enter a tag to search for into the textbox");
            searchAlert.showAndWait();
            return false;
        }else{
            return true;
        }
    }

}
