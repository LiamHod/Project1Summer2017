package project1.model;


public class DBConn {

    private String url;
    private String username;
    private String password;

    public DBConn(){
        this.url = "jdbc:mysql://localhost:3306/project1?useSSL=false";
        this.username = "root";
        this.password = "admin";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
