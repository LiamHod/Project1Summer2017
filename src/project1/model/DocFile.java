package project1.model;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DocFile {

    private final IntegerProperty docid;
    private final StringProperty docname;
    private final StringProperty docdesc;


    public DocFile(Integer docid, String docname, String docdesc){
        this.docid = new SimpleIntegerProperty(docid);
        this.docname = new SimpleStringProperty(docname);
        this.docdesc = new SimpleStringProperty(docdesc);
    }

    public int getDocid() {
        return docid.get();
    }

    public IntegerProperty docidProperty() {
        return docid;
    }

    public void setDocid(int docid) {
        this.docid.set(docid);
    }

    public String getDocname() {
        return docname.get();
    }

    public StringProperty docnameProperty() {
        return docname;
    }

    public void setDocname(String docname) {
        this.docname.set(docname);
    }

    public String getDocdesc() {
        return docdesc.get();
    }

    public StringProperty docdescProperty() {
        return docdesc;
    }

    public void setDocdesc(String docdesc) {
        this.docdesc.set(docdesc);
    }

    @Override
    public String toString() {
        return "DocFile{" +
                "docid=" + docid +
                ", docname=" + docname +
                ", docdesc=" + docdesc +
                '}';
    }
}
