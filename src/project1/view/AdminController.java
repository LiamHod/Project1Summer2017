package project1.view;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import project1.model.Courses;
import project1.model.DBCreds;
import project1.model.Instructor;

import java.sql.*;

public class AdminController {

    @FXML
    private TabPane adminTabPane;

    @FXML
    private Tab courseTab;

    @FXML
    private TableView<Courses> courseTableView;

//    @FXML
//    private TableColumn<Courses, Integer> courseIdColumn;

    @FXML
    private TableColumn<Courses, String> courseNameColumn;

    @FXML
    private TableColumn<Courses, String> facultyColumn;

    @FXML
    private TextField courseNameTextBox;

    @FXML
    private TextField facultyCourTextBox;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private Tab instrTab;

    @FXML
    private TableView<Instructor> instructorTableView;

//    @FXML
//    private TableColumn<Instructor, Integer> instrIdColumn;

    @FXML
    private TableColumn<Instructor, String> firstNameColumn;

    @FXML
    private TableColumn<Instructor, String> lastNameColumn;

    @FXML
    private TableColumn<Instructor, String> emailColumn;

    @FXML
    private TableColumn<Instructor, String> facultyInstrColumn;

//    @FXML
//    private TableColumn<Instructor, String> passwordColumn;

    @FXML
    private TableColumn<Instructor, Integer> adminColumn;

    @FXML
    private TextField firstNameTextBox;

    @FXML
    private TextField facultyInstrTextBox;

    @FXML
    private Button addInstrButton;

    @FXML
    private Button removeInstrButton;

    @FXML
    private TextField lastNameTextBox;

    @FXML
    private TextField emailTextBox;

    @FXML
    private PasswordField passBox;

    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private ObservableList<Courses> courseList = FXCollections.observableArrayList();
    private ObservableList<Instructor> instructorList = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        //idColumn.setCellValueFactory(cellData -> cellData.getValue().docidProperty().asObject());
        //courseIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        courseNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        facultyColumn.setCellValueFactory(cellData -> cellData.getValue().facultyProperty());

        //instrIdColumn.setCellValueFactory(cellData -> cellData.getValue().instrIdProperty().asObject());
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        facultyInstrColumn.setCellValueFactory(cellData -> cellData.getValue().facultyProperty());
        //passwordColumn.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());
        adminColumn.setCellValueFactory(cellData -> cellData.getValue().adminProperty().asObject());

        loadCourse();

        adminTabPane.getSelectionModel().selectedItemProperty().addListener( (v, oldValue, newValue) -> {
            if (newValue == instrTab){
                instructorList.clear();
                loadInstructors();
            }else{
                courseList.clear();
                loadCourse();
            }
        });

    }

    @FXML
    void handleAdd(ActionEvent event) {
        if (isCourInputsValid()){

        }
    }

    @FXML
    void handleRemove(ActionEvent event) {
        Courses curCour = courseTableView.getSelectionModel().getSelectedItem();
        if (curCour != null){
            String deleteCourse = "DELETE FROM course WHERE idcourse = ?";
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                PreparedStatement ps = connection.prepareStatement(deleteCourse);
                ps.setInt(1,curCour.getId());
                ps.executeUpdate();
                ps.close();
                connection.close();
            }catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }

        }
        courseList.clear();
        loadCourse();
    }

    @FXML
    void handleAddInstr(ActionEvent event) {
        if (isInstrInputsValid()){

        }
    }

    @FXML
    void handleRemoveInstr(ActionEvent event) {
        Instructor curInstr = instructorTableView.getSelectionModel().getSelectedItem();
        if (curInstr != null){
            String deleteInstr = "DELETE FROM instructor WHERE idinstructor = ?";
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                PreparedStatement ps = connection.prepareStatement(deleteInstr);
                ps.setInt(1, curInstr.getInstrId());
                ps.executeUpdate();
                ps.close();
                connection.close();
            }catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }
        }
        instructorList.clear();
        loadInstructors();
    }


    private void loadCourse(){
//        courseTableView.getSelectionModel().selectedItemProperty().addListener( (v, oldValue, newValue) -> {
//
//        });
        courseTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        String loadCourseQuery = "SELECT * FROM course";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement ps = connection.prepareStatement(loadCourseQuery);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int curCourId = rs.getInt(1);
                String curCourName = rs.getString(2);
                String curFaculty = rs.getString(3);
                courseList.add(new Courses(curCourId,curCourName,curFaculty));
            }
            ps.close();
            rs.close();
            connection.close();
            courseTableView.setItems(courseList);
        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    private void loadInstructors(){
        instructorTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        String loadInstrQuery = "SELECT * FROM instructor";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement ps = connection.prepareStatement(loadInstrQuery);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int curInstrId = rs.getInt(1);
                String fname = rs.getString(2);
                String lname = rs.getString(3);
                String faculty = rs.getString(4);
                String email = rs.getString(5);
                String password = rs.getString(6);
                int admin = rs.getInt(7);
                instructorList.add(new Instructor(curInstrId,fname,lname,faculty,email,password,admin));
            }
            ps.close();
            rs.close();
            connection.close();
            instructorTableView.setItems(instructorList);
        }catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    private Boolean isCourInputsValid(){
        String errorMessage = "";
        if (courseNameTextBox.getText() == null || courseNameTextBox.getText().length() == 0){
            errorMessage += "- No course name specified.\n";
        }
        if (facultyCourTextBox.getText() == null || facultyCourTextBox.getText().length() == 0){
            errorMessage += "- No faculty specified.\n";
        }
        if (errorMessage.length() == 0){
            return true;
        }else{
            Alert inputAlert = new Alert(Alert.AlertType.WARNING);
            inputAlert.setTitle("Incorrect Inputs");
            inputAlert.setHeaderText("Some of the inputs are incorrect");
            inputAlert.setContentText(errorMessage);
            inputAlert.showAndWait();
            return false;
        }
    }

    private Boolean isInstrInputsValid(){
        String errorMessage = "";
        if (firstNameTextBox.getText() == null || firstNameTextBox.getText().length() == 0){
            errorMessage += "- No first name specified.\n";
        }
        if (lastNameTextBox.getText() == null || lastNameTextBox.getText().length() == 0){
            errorMessage += "- No last name specified.\n";
        }
        if (facultyInstrTextBox.getText() == null || facultyInstrTextBox.getText().length() == 0){
            errorMessage += "- No faculty specified.\n";
        }
        if (emailTextBox.getText() == null || emailTextBox.getText().length() == 0){
            errorMessage += "- No email specified.\n";
        }
        if (passBox.getText() == null || passBox.getText().length() == 0){
            errorMessage += "- No password specified.\n";
        }
        if (errorMessage.length() == 0){
            return true;
        }else{
            Alert inputAlert = new Alert(Alert.AlertType.WARNING);
            inputAlert.setTitle("Incorrect Inputs");
            inputAlert.setHeaderText("Some of the inputs are incorrect");
            inputAlert.setContentText(errorMessage);
            inputAlert.showAndWait();
            return false;
        }
    }


}
