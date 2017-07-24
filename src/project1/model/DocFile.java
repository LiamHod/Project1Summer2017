package project1.model;


import javafx.beans.property.*;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DocFile {

    private final IntegerProperty docid;
    private final StringProperty docname;
    private final StringProperty docdesc;
    private final StringProperty docdateadded;
    private final StringProperty filetype;


    public DocFile(Integer docid, String docname, String docdesc, Date docdateadded, String filetype){
        this.docid = new SimpleIntegerProperty(docid);
        this.docname = new SimpleStringProperty(docname);
        this.docdesc = new SimpleStringProperty(docdesc);
        Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        String docdateaddedstring = formatter.format(docdateadded);
        this.docdateadded = new SimpleStringProperty(docdateaddedstring);
        this.filetype = new SimpleStringProperty(filetype);
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

    public String getDocdateadded() {
        return docdateadded.get();
    }

    public StringProperty docdateaddedProperty() {
        return docdateadded;
    }

    public void setDocdateadded(String docdateadded) {
        this.docdateadded.set(docdateadded);
    }

    public String getFiletype() {
        return filetype.get();
    }

    public StringProperty filetypeProperty() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype.set(filetype);
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
