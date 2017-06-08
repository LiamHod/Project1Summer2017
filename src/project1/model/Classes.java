package project1.model;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Classes {

    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty faculty;

    public Classes(String course, String faculty){
        this.id = new SimpleIntegerProperty(0);
        this.name = new SimpleStringProperty(course);
        this.faculty = new SimpleStringProperty(faculty);
    }

    public String getName(){
        return name.get();
    }

    public String getFaculty() {
        return faculty.get();
    }

    public StringProperty nameProperty(){
        return name;
    }

    public StringProperty facultyProperty(){
        return faculty;
    }
}


