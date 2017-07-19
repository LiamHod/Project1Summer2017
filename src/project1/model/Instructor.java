package project1.model;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Instructor {

    private final IntegerProperty instrId;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty faculty;
    private final StringProperty email;
    private final StringProperty password;
    private final IntegerProperty admin;

    public Instructor(Integer instrId, String firstName, String lastName, String faculty, String email, String password, Integer admin){
        this.instrId = new SimpleIntegerProperty(instrId);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.faculty = new SimpleStringProperty(faculty);
        this.email = new SimpleStringProperty(email);
        this.password = new SimpleStringProperty(password);
        this.admin = new SimpleIntegerProperty(admin);
    }

    public int getInstrId() {
        return instrId.get();
    }

    public IntegerProperty instrIdProperty() {
        return instrId;
    }

    public void setInstrId(int instrId) {
        this.instrId.set(instrId);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getFaculty() {
        return faculty.get();
    }

    public StringProperty facultyProperty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty.set(faculty);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public int getAdmin() {
        return admin.get();
    }

    public IntegerProperty adminProperty() {
        return admin;
    }

    public void setAdmin(int admin) {
        this.admin.set(admin);
    }
}
