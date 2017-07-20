package project1.model;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Courses {

    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty faculty;

    public Courses(Integer courId, String course, String faculty) {
        this.id = new SimpleIntegerProperty(courId);
        this.name = new SimpleStringProperty(course);
        this.faculty = new SimpleStringProperty(faculty);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
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

    @Override
    public String toString() {
        return name.get();
    }
}


