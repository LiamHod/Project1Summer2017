package project1.view;

//jBCrypt is subject to the following license:
/*
 * Copyright (c) 2006 Damien Miller <djm@mindrot.org>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.mindrot.jbcrypt.BCrypt;
import project1.model.Courses;
import project1.model.DBCreds;
import project1.model.Instructor;
import java.sql.*;
import java.util.Optional;

public class AdminController {

    @FXML
    private TabPane adminTabPane;

    @FXML
    private Tab courseTab;

    @FXML
    private TableView<Courses> courseTableView;

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

    @FXML
    private TableColumn<Instructor, String> firstNameColumn;

    @FXML
    private TableColumn<Instructor, String> lastNameColumn;

    @FXML
    private TableColumn<Instructor, String> emailColumn;

    @FXML
    private TableColumn<Instructor, String> facultyInstrColumn;

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
    private Integer instrId;
    private Instructor selInstr;
    private ObservableList<Courses> courseList = FXCollections.observableArrayList();
    private ObservableList<Instructor> instructorList = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        courseNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        facultyColumn.setCellValueFactory(cellData -> cellData.getValue().facultyProperty());

        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        facultyInstrColumn.setCellValueFactory(cellData -> cellData.getValue().facultyProperty());
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
        ContextMenu adminMenu = createAdminContextMenu();
        ContextMenu notAdminMenu = createNotAdminContextMenu();
        instructorTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton() == MouseButton.SECONDARY && (selInstr = instructorTableView.getSelectionModel().getSelectedItem()) != null && selInstr.getAdmin() == 1){
                    instructorTableView.getSelectionModel().clearSelection();
                    instructorTableView.setContextMenu(adminMenu);
                }else if(event.getButton() == MouseButton.SECONDARY && (selInstr = instructorTableView.getSelectionModel().getSelectedItem()) != null && selInstr.getAdmin() == 0){
                    instructorTableView.getSelectionModel().clearSelection();
                    instructorTableView.setContextMenu(notAdminMenu);
                }
            }
        });

    }

    @FXML
    void handleAdd(ActionEvent event) {
        if (isCourInputsValid()){
            String addCourQuery = "INSERT INTO course(courname,faculty) VALUES (?,?)";
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                PreparedStatement ps = connection.prepareStatement(addCourQuery);
                String courname = formatString(courseNameTextBox.getText());
                ps.setString(1,courname);
                ps.setString(2,facultyCourTextBox.getText());
                ps.executeUpdate();
                ps.close();
                connection.close();
                courseNameTextBox.clear();
                facultyCourTextBox.clear();
            } catch (SQLException e) {
                int errorCode = e.getErrorCode();
                if (errorCode == 1062) {
                    System.out.println(errorCode);
                    Alert alreadyAlert = new Alert(Alert.AlertType.ERROR);
                    alreadyAlert.setTitle("This course already exists");
                    alreadyAlert.setHeaderText(null);
                    alreadyAlert.setContentText("This course already exists, please use another course name");
                    alreadyAlert.showAndWait();
                }else{
                    Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                    sqlAlert.setTitle("Error adding course");
                    sqlAlert.setHeaderText(null);
                    sqlAlert.setContentText("The program encountered an error and couldn't add the course, check your connection and please try again");
                    sqlAlert.showAndWait();
                }
            }
        }
        reloadCourse();
    }

    @FXML
    void handleRemove(ActionEvent event) {
        Courses curCour = courseTableView.getSelectionModel().getSelectedItem();
        if (curCour != null){
            Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
            deleteAlert.setTitle("Delete This Course?");
            deleteAlert.setHeaderText(null);
            deleteAlert.setContentText("Are you sure you want to delete this course?");
            Optional<ButtonType> result = deleteAlert.showAndWait();
            if (result.get() == ButtonType.OK) {
                String deleteCourse = "DELETE FROM course WHERE idcourse = ?";
                try (Connection connection = DriverManager.getConnection(url, username, password)) {
                    PreparedStatement ps = connection.prepareStatement(deleteCourse);
                    ps.setInt(1, curCour.getId());
                    ps.executeUpdate();
                    ps.close();
                    connection.close();
                } catch (SQLException e) {
                    Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                    sqlAlert.setTitle("Error removing course");
                    sqlAlert.setHeaderText(null);
                    sqlAlert.setContentText("The program encountered an error and couldn't remove the course, check your connection and please try again");
                    sqlAlert.showAndWait();
                    throw new IllegalStateException("Cannot connect the database!", e);
                }
            }

        }else{
            Alert noSelAlert = new Alert(Alert.AlertType.WARNING);
            noSelAlert.setTitle("No course selected");
            noSelAlert.setHeaderText(null);
            noSelAlert.setContentText("Please select a course before clicking remove");
            noSelAlert.showAndWait();
        }
        reloadCourse();
    }

    @FXML
    void handleAddInstr(ActionEvent event) {
        if (isInstrInputsValid()){
            String addInstrQuery = "INSERT INTO instructor(fname,lname,faculty,email,password) VALUES (?,?,?,?,?)";
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                PreparedStatement ps = connection.prepareStatement(addInstrQuery);
                ps.setString(1,firstNameTextBox.getText());
                ps.setString(2,lastNameTextBox.getText());
                ps.setString(3,facultyInstrTextBox.getText());
                ps.setString(4,emailTextBox.getText());
                ps.setString(5,BCrypt.hashpw(passBox.getText(),BCrypt.gensalt()));
                ps.executeUpdate();
                ps.close();
                connection.close();
                firstNameTextBox.clear();
                lastNameTextBox.clear();
                facultyInstrTextBox.clear();
                emailTextBox.clear();
                passBox.clear();
            } catch (SQLException e) {
                int errorCode = e.getErrorCode();
                if (errorCode == 1062) {
                    Alert alreadyAlert = new Alert(Alert.AlertType.ERROR);
                    alreadyAlert.setTitle("This email already exists");
                    alreadyAlert.setHeaderText(null);
                    alreadyAlert.setContentText("This email already exists, please use another email");
                    alreadyAlert.showAndWait();
                }else{
                    Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                    sqlAlert.setTitle("Error adding instructor");
                    sqlAlert.setHeaderText(null);
                    sqlAlert.setContentText("The program encountered an error and couldn't add the instructor, check your connection and please try again");
                    sqlAlert.showAndWait();
                }
            }
        }
        reloadInstructors();
    }

    @FXML
    void handleRemoveInstr(ActionEvent event) {
        Instructor curInstr = instructorTableView.getSelectionModel().getSelectedItem();
        if (curInstr != null){
            Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
            deleteAlert.setTitle("Delete This Instructor?");
            deleteAlert.setHeaderText(null);
            deleteAlert.setContentText("Are you sure you want to delete this instructor?");
            Optional<ButtonType> result = deleteAlert.showAndWait();
            if (result.get() == ButtonType.OK) {
                String deleteInstr = "DELETE FROM instructor WHERE idinstructor = ?";
                try (Connection connection = DriverManager.getConnection(url, username, password)) {
                    PreparedStatement ps = connection.prepareStatement(deleteInstr);
                    ps.setInt(1, curInstr.getInstrId());
                    ps.executeUpdate();
                    ps.close();
                    connection.close();
                } catch (SQLException e) {
                    Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                    sqlAlert.setTitle("Error removing instructor");
                    sqlAlert.setHeaderText(null);
                    sqlAlert.setContentText("The program encountered an error and couldn't remove the instructor, check your connection and please try again");
                    sqlAlert.showAndWait();
                    throw new IllegalStateException("Cannot connect the database!", e);
                }
            }
        }else{
            Alert noSelAlert = new Alert(Alert.AlertType.WARNING);
            noSelAlert.setTitle("No instructor selected");
            noSelAlert.setHeaderText(null);
            noSelAlert.setContentText("Please select an instructor before clicking remove");
            noSelAlert.showAndWait();
        }
        reloadInstructors();
    }

    /**
     * Creates context menu if the user selected already has admin privileges
     * @return - the context menu
     */
    private ContextMenu createAdminContextMenu(){
        ContextMenu rightClickMenu = new ContextMenu();
        MenuItem adminMenu = new MenuItem("Remove Admin Privileges");
        rightClickMenu.getItems().addAll(adminMenu);
        adminMenu.setOnAction(e -> flipAdmin());
        return rightClickMenu;
    }

    /**
     * Creates context menu if the user selected does not have admin privileges
     * @return - the context menu
     */
    private ContextMenu createNotAdminContextMenu(){
        ContextMenu rightClickMenu = new ContextMenu();
        MenuItem adminMenu = new MenuItem("Add Admin Privileges");
        rightClickMenu.getItems().addAll(adminMenu);
        adminMenu.setOnAction(e -> flipAdmin());
        return rightClickMenu;
    }

    /**
     * Changes the selected users status as admin
     */
    private void flipAdmin(){
        int curAdmin = selInstr.getAdmin();
        curAdmin ^= 1;
        String flipAdminQuery = "UPDATE instructor SET admin = ? WHERE idinstructor = ?;";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement ps = connection.prepareStatement(flipAdminQuery);
            ps.setInt(1,curAdmin);
            ps.setInt(2,selInstr.getInstrId());
            ps.executeUpdate();
            ps.close();
            connection.close();
        }catch (SQLException e) {
            Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
            sqlAlert.setTitle("Error changing admin privileges");
            sqlAlert.setHeaderText(null);
            sqlAlert.setContentText("The program encountered an error and couldn't change admin privileges, check your connection and please try again");
            sqlAlert.showAndWait();
            throw new IllegalStateException("Cannot connect the database!", e);
        }
        reloadInstructors();
    }

    /**
     * Gets rid of spaces in string and forces uppercase
     * @param oldString - the string to be formatted
     * @return - the string that is correctly formatted
     */
    private String formatString(String oldString){
        String newString = oldString.replaceAll("\\s+","");
        newString = newString.toUpperCase();
        return newString;
    }

    /**
     * Loads the courses into the table
     */
    private void loadCourse(){
        courseTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        String loadCourseQuery = "SELECT * FROM course ORDER BY courname";
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
            Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
            sqlAlert.setTitle("Error loading courses");
            sqlAlert.setHeaderText(null);
            sqlAlert.setContentText("The program encountered an error and couldn't load the courses, check your connection and please try again");
            sqlAlert.showAndWait();
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    /**
     * Clears and reloads the courses in the table
     */
    private void reloadCourse(){
        courseList.clear();
        loadCourse();
    }

    /**
     * Loads the instructors into the table
     */
    private void loadInstructors(){
        instructorTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        String loadInstrQuery = "SELECT * FROM instructor WHERE idinstructor != ? ORDER BY lname";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement ps = connection.prepareStatement(loadInstrQuery);
            ps.setInt(1,instrId);
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
            Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
            sqlAlert.setTitle("Error loading instructors");
            sqlAlert.setHeaderText(null);
            sqlAlert.setContentText("The program encountered an error and couldn't load the instructors, check your connection and please try again");
            sqlAlert.showAndWait();
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    /**
     * Clears and reloads the intructors in the table
     */
    private void reloadInstructors(){
        instructorList.clear();
        loadInstructors();
    }

    /**
     * Checks if the course inputs are valid
     * @return - boolean value if the course inputs are valid or not
     */
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

    /**
     * Checks if the instructor inputs are valid
     * @return - boolean value if the instructor inputs are valid or not
     */
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

    /**
     * Initializes the current user id
     * @param instrId - the current users id
     */
    public void initId(Integer instrId){
        this.instrId = instrId;
    }


}
