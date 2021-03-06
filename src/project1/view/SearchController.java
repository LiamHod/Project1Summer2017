package project1.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import project1.model.DBCreds;
import project1.model.SearchResults;

import java.sql.*;

public class SearchController {

    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private ObservableList<SearchResults> searchList = FXCollections.observableArrayList();
    private Integer instrId;

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
        String searchQuery = "SELECT DISTINCT course.courname,document.title FROM document,course," +
                "doctag,tag,instrcourdoc WHERE instrcourdoc.idinstructor = ? AND " +
                "document.iddocument = instrcourdoc.iddocument AND course.idcourse = instrcourdoc.idcourse AND " +
                "tag.idtag = doctag.idtag AND document.iddocument = doctag.iddocument AND tagname = ? ORDER BY courname";
        if (checkInput()){
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                PreparedStatement ps = connection.prepareStatement(searchQuery);
                ps.setInt(1,instrId);
                ps.setString(2,searchTextBox.getText());
                ResultSet rs = ps.executeQuery();
                while (rs.next()){
                    String curCourName = rs.getString(1);
                    String curDocTitle = rs.getString(2);
                    searchList.add(new SearchResults(curCourName,curDocTitle));
                }
                ps.close();
                rs.close();
                connection.close();
                resultsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                resultsTable.setItems(searchList);
            }catch (SQLException e) {
                Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                sqlAlert.setTitle("Error loading search results");
                sqlAlert.setHeaderText(null);
                sqlAlert.setContentText("The program encountered an error and couldn't load search results, check your connection and please try again");
                sqlAlert.showAndWait();
                throw new IllegalStateException("Cannot connect the database!", e);
            }
        }
    }

    /**
     * Checks to see if inputs are valid
     * @return - boolean value of whether the inputs are valid or not
     */
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

    /**
     * Initalizes value for controller
     * @param instrId - user id
     */
    public void initValue(Integer instrId){
        this.instrId = instrId;
    }

}
