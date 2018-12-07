/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package psantaniello.view;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import psantaniello.app.ScheduleApp;
import psantaniello.model.User;

/**
 * FXML Controller class
 *
 * @author Paolo
 */
public class HomeScreenController {
    
    @FXML
    private MenuItem logoutUserLabel;
    
    private ScheduleApp scheduleApp;
    private User currentUser;
    
    
    @FXML
    void handleMenuAppointments(ActionEvent event) {
        scheduleApp.showAppointmentScreen(currentUser);
    }

    @FXML
    void handleMenuCustomers(ActionEvent event) {
        scheduleApp.showCustomerScreen(currentUser);
    }

    @FXML
    void handleMenuExit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.NONE);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText("Are you sure you want to exit out of Appointment Scheduler?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.exit(0);
        }
        else {
            System.out.println("You canceled your request.");
        }
    }

    @FXML
    void handleMenuLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Do you want to log out as " + currentUser.getUserName()+"?");
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> scheduleApp.showLoginScreen());

    }

    @FXML
    void handleMenuReports(ActionEvent event) {
        scheduleApp.showReportScreen(currentUser);
    }


 

    public void setHomeScreen(ScheduleApp scheduleApp, User currentUser) {
        this.scheduleApp = scheduleApp;
        this.currentUser = currentUser;
        
        logoutUserLabel.setText("Logout " + currentUser.getUserName());
    }
    
}
