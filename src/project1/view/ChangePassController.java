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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import project1.model.DBCreds;

import java.sql.*;

public class ChangePassController {

    private DBCreds dbCreds = DBCreds.INSTANCE;
    private String url = dbCreds.getUrl();
    private String username = dbCreds.getUsername();
    private String password = dbCreds.getPassword();
    private Integer instrId;

    @FXML
    private PasswordField oldPassTextBox;

    @FXML
    private PasswordField newPassTextBox;

    @FXML
    private PasswordField repeatTextBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    void handleCancel(ActionEvent event) {
        Stage currStage = (Stage) cancelButton.getScene().getWindow();
        currStage.close();
    }

    @FXML
    void handleOk(ActionEvent event) {
        String updateQuery = "UPDATE instructor SET password=? WHERE idinstructor=?;";
        String getPassQuery = "SELECT password FROM instructor WHERE idinstructor=?";
        if (checkInputs()){
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                PreparedStatement ps = connection.prepareStatement(getPassQuery);
                ps.setInt(1,instrId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                String curPass = rs.getString(1);
                ps.close();
                rs.close();
                String pass = oldPassTextBox.getText();
                if (BCrypt.checkpw(pass, curPass)) {
                    PreparedStatement ps2 = connection.prepareStatement(updateQuery);
                    String newPass = BCrypt.hashpw(newPassTextBox.getText(),BCrypt.gensalt());
                    ps2.setString(1,newPass);
                    ps2.setInt(2,instrId);
                    ps2.executeUpdate();
                    ps2.close();
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Password Update");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Your password was updated successfully!");
                    successAlert.showAndWait();
                }else{
                    Alert inputAlert = new Alert(Alert.AlertType.WARNING);
                    inputAlert.setTitle("Incorrect Password");
                    inputAlert.setHeaderText("The password you entered was incorrect");
                    inputAlert.setContentText("Please enter the correct password");
                    inputAlert.showAndWait();
                }
                connection.close();
            }catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }
            Stage currStage = (Stage) cancelButton.getScene().getWindow();
            currStage.close();
        }

    }

    private Boolean checkInputs(){
        String errorMessage = "";
        if (oldPassTextBox.getText() == null || oldPassTextBox.getText().length() == 0){
            errorMessage += "- Please enter your old password.\n";
        }
        if (repeatTextBox.getText() == null || repeatTextBox.getText().length() == 0){
            errorMessage += "- Please repeat your old password.\n";
        }
        if (newPassTextBox.getText() == null || newPassTextBox.getText().length() == 0){
            errorMessage += "- Please enter a new password.\n";
        }
        if (!newPassTextBox.getText().equals(repeatTextBox.getText())){
            errorMessage += "- Your new passwords don't match.\n";
        }
        if (newPassTextBox.getText().contains(" ")){
            errorMessage += "- No spaces allowed in the password.\n";
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

    public void initValues(Integer instrId){
        this.instrId = instrId;
    }

}
