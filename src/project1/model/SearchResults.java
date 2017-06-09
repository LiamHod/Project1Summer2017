package project1.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SearchResults {

    private final StringProperty className;
    private final StringProperty fileTitle;

    public SearchResults(String className, String fileTitle){
        this.className = new SimpleStringProperty(className);
        this.fileTitle = new SimpleStringProperty(fileTitle);
    }

    public String getClassName() {
        return className.get();
    }

    public StringProperty classNameProperty() {
        return className;
    }

    public void setClassName(String className) {
        this.className.set(className);
    }

    public String getFileTitle() {
        return fileTitle.get();
    }

    public StringProperty fileTitleProperty() {
        return fileTitle;
    }

    public void setFileTitle(String fileTitle) {
        this.fileTitle.set(fileTitle);
    }

    @Override
    public String toString() {
        return "SearchResults{" +
                "className=" + className +
                ", fileTitle=" + fileTitle +
                '}';
    }
}
