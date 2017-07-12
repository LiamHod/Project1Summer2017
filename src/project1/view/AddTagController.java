package project1.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import project1.model.DBCreds;
import project1.model.DocFile;
import project1.model.Tag;

import java.sql.*;

public class AddTagController {

//    private String url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
//    private String username = "root";
//    private String password = "admin";
    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private Integer docId;
    private Integer tagsLeft;
    private ObservableList<Tag> tagList = FXCollections.observableArrayList();

    @FXML
    private TextArea tagTextBox;

    @FXML
    private Label tagsLeftLabel;

    @FXML
    private TableView<Tag> tagTableView;

    @FXML
    private TableColumn<Tag, Integer> tagIDColumn;

    @FXML
    private TableColumn<Tag, String> tagNameColumn;

    @FXML
    private Button removeButton;

    @FXML
    private Button addButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    private void initialize(){
        tagIDColumn.setCellValueFactory(cellData -> cellData.getValue().tagidProperty().asObject());
        tagNameColumn.setCellValueFactory(cellData -> cellData.getValue().tagnameProperty());
    }

    @FXML
    void handleAdd(ActionEvent event) {
        if (checkInput() && tagsLeft()){
            String totalTags = tagTextBox.getText();
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                Integer tagId;
                String newQuery = "INSERT INTO tag (tagname) " + "VALUES (?)";
                String tagIntQuery = "INSERT INTO doctag(iddocument,idtag) " + "VALUES (?,?)";
                PreparedStatement ps = connection.prepareStatement(newQuery,Statement.RETURN_GENERATED_KEYS);
                PreparedStatement ps2 = connection.prepareStatement(tagIntQuery);
                String[] sepTags = totalTags.split(" ");
                Integer maxVal;
                System.out.println("tags left:");
                System.out.println(tagsLeft);
                if (sepTags.length >= tagsLeft){
                    maxVal = tagsLeft;
                }else{
                    maxVal = sepTags.length;
                }
                System.out.println("maxval:");
                System.out.println(maxVal);
                for (int i = 0; i < maxVal; i++){
                    System.out.println("septags:");
                    System.out.println(sepTags[i]);
                    connection.setAutoCommit(false);
                    ps.setString(1,sepTags[i]);
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    keys.next();
                    tagId = keys.getInt(1);
                    ps2.setInt(1,docId);
                    ps2.setInt(2,tagId);
                    ps2.executeUpdate();
                    connection.commit();
                    connection.setAutoCommit(true);
                    keys.close();
                }
                ps.close();
                ps2.close();
                connection.close();
                tagTextBox.clear();
            }catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }

        }
        populateTagTable();
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
    void handleRemove(ActionEvent event) {
        Tag selTag = tagTableView.getSelectionModel().getSelectedItem();
        String delTagQuery = "DELETE FROM project1.tag WHERE idtag = ?;";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement ps = connection.prepareStatement(delTagQuery);
            ps.setInt(1,selTag.getTagid());
            ps.executeUpdate();
            ps.close();
            connection.close();
        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
        populateTagTable();
    }

    public void initValue(Integer docId){
        this.docId = docId;
    }

    public void populateTagTable(){
        tagTableView.getItems().clear();
        String newQuery = "SELECT doctag.idtag,tagname FROM project1.tag,project1.doctag WHERE iddocument = ? AND tag.idtag = doctag.idtag;";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement ps = connection.prepareStatement(newQuery);
            ps.setInt(1,docId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                Integer curTagID = rs.getInt(1);
                String curTagName = rs.getString(2);
                tagList.add(new Tag(curTagID,curTagName));
                //fileTemp.add(curFile);
            }
            ps.close();
            rs.close();
            connection.close();
            tagTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            tagTableView.setItems(tagList);
        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
        tagsLeft();
    }

    private Boolean checkInput(){
        if (tagTextBox.getText() == null || tagTextBox.getText().length() == 0){
            Alert tagAlert = new Alert(Alert.AlertType.WARNING);
            tagAlert.setTitle("New tags missing");
            tagAlert.setHeaderText("No tags in text box");
            tagAlert.setContentText("Please enter tags into the textbox");
            tagAlert.showAndWait();
            return false;
        }else{
            return true;
        }
    }

    private Boolean tagsLeft(){
        String checkTagQuery = "SELECT count(*) FROM project1.doctag WHERE iddocument = ?;";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement ps = connection.prepareStatement(checkTagQuery);
            ps.setInt(1,docId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            Integer totalTags = rs.getInt(1);
            tagsLeft = 10 - totalTags;
            if (tagsLeft == 0){
                Alert tagAlert = new Alert(Alert.AlertType.WARNING);
                tagAlert.setTitle("No tags remaining");
                tagAlert.setHeaderText("No tags left to add");
                tagAlert.setContentText("Please remove tags before adding more");
                tagAlert.showAndWait();
                return false;
            }
            connection.close();
            ps.close();
            rs.close();
            tagsLeftLabel.setText(tagsLeft.toString());
            return true;
        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }
}