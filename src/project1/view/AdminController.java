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

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.beans.property.StringProperty;
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
    private Integer instrId;
    private Instructor selInstr;
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
                Alert alreadyAlert = new Alert(Alert.AlertType.ERROR);
                alreadyAlert.setTitle("This course already exists");
                alreadyAlert.setHeaderText(null);
                alreadyAlert.setContentText("This course already exists, please use another course name");
                alreadyAlert.showAndWait();
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
                    throw new IllegalStateException("Cannot connect the database!", e);
                }
            }

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
                Alert alreadyAlert = new Alert(Alert.AlertType.ERROR);
                alreadyAlert.setTitle("This email already exists");
                alreadyAlert.setHeaderText(null);
                alreadyAlert.setContentText("This email already exists, please use another email");
                alreadyAlert.showAndWait();
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
                    throw new IllegalStateException("Cannot connect the database!", e);
                }
            }
        }
        reloadInstructors();
    }

    private ContextMenu createAdminContextMenu(){
        ContextMenu rightClickMenu = new ContextMenu();
        MenuItem adminMenu = new MenuItem("Remove Admin Privileges");
        rightClickMenu.getItems().addAll(adminMenu);
        adminMenu.setOnAction(e -> flipAdmin());
        return rightClickMenu;
    }

    private ContextMenu createNotAdminContextMenu(){
        ContextMenu rightClickMenu = new ContextMenu();
        MenuItem adminMenu = new MenuItem("Add Admin Privileges");
        rightClickMenu.getItems().addAll(adminMenu);
        adminMenu.setOnAction(e -> flipAdmin());
        return rightClickMenu;
    }

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
            throw new IllegalStateException("Cannot connect the database!", e);
        }
        reloadInstructors();
    }

    private String formatString(String oldString){
        String newString = oldString.replaceAll("\\s+","");
        newString = newString.toUpperCase();
        return newString;
    }

//    private boolean checkEmail(){
//        String curEmail = instructorTableView.getSelectionModel().getSelectedItem().getEmail();
//        String emailExistQuery = "SELECT count(*) FROM instructor WHERE email = ?";
//        try (Connection connection = DriverManager.getConnection(url, username, password)) {
//            PreparedStatement ps = connection.prepareStatement(emailExistQuery);
//
//        } catch (SQLException e) {
//            throw new IllegalStateException("Cannot connect the database!", e);
//        }
//    }

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

    private void reloadCourse(){
        courseList.clear();
        loadCourse();
    }

    private void loadInstructors(){
        instructorTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        String loadInstrQuery = "SELECT * FROM instructor WHERE idinstructor != ?";
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
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    private void reloadInstructors(){
        instructorList.clear();
        loadInstructors();
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

    public void initId(Integer instrId){
        this.instrId = instrId;
    }


}
