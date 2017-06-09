package project1.model;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Tag {

    private final IntegerProperty tagid;
    private final StringProperty tagname;

    public Tag(Integer tagid, String tagname){
        this.tagid = new SimpleIntegerProperty(tagid);
        this.tagname = new SimpleStringProperty(tagname);
    }

    public int getTagid() {
        return tagid.get();
    }

    public IntegerProperty tagidProperty() {
        return tagid;
    }

    public void setTagid(int tagid) {
        this.tagid.set(tagid);
    }

    public String getTagname() {
        return tagname.get();
    }

    public StringProperty tagnameProperty() {
        return tagname;
    }

    public void setTagname(String tagname) {
        this.tagname.set(tagname);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tagid=" + tagid +
                ", tagname=" + tagname +
                '}';
    }
}
