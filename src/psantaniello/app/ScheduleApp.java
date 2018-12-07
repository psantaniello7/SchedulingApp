/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package psantaniello.app;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import psantaniello.model.Appointment;
import psantaniello.model.User;
import psantaniello.util.DBConnection;
import psantaniello.util.LoggerUtil;
import psantaniello.view.AppointmentEditScreenController;
import psantaniello.view.AppointmentScreenController;
import psantaniello.view.CustomerEditScreenController;
import psantaniello.view.LoginScreenController;
import psantaniello.view.HomeScreenController;
import psantaniello.view.ReportsScreenController;

/**
 *
 * @author Paolo
 */
public class ScheduleApp extends Application {
    
    private Stage primaryStage;
    private AnchorPane loginScreen;
    private BorderPane menu;
    private AnchorPane appointmentScreen;
    private TabPane tabPane;
    Locale locale = Locale.getDefault();
    private static Connection connection;
    
    public static void main(String[] args) {
    
    //Locale.setDefault(new Locale("it", "IT"));
    DBConnection.init();
    connection = DBConnection.getConn();
    LoggerUtil.init();
    launch(args);
    DBConnection.closeConn();
    
    }    
    
    @Override
    public void start(Stage primaryStage){
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Appointment Scheduler");
        showLoginScreen();
    }
    
    public void showLoginScreen(){
        try{
            
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ScheduleApp.class.getResource("/psantaniello/view/LoginScreen.fxml"));
            loginScreen = (AnchorPane) loader.load();  
            
            
            LoginScreenController controller = loader.getController();
            controller.setLoginScreen(this);
            
            
            Scene scene = new Scene(loginScreen);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        }   catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showHomeScreen(User currentUser) {
        try{
            
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ScheduleApp.class.getResource("/psantaniello/view/HomeScreen.fxml"));
            menu = (BorderPane) loader.load();  
            
            
            HomeScreenController controller = loader.getController();
            controller.setHomeScreen(this, currentUser);
            
            
            Scene scene = new Scene(menu);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        }   catch (IOException e){
            e.printStackTrace();
        }
    }

    public void showAppointmentScreen(User currentUser) {
        try{
            
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ScheduleApp.class.getResource("/psantaniello/view/AppointmentScreen.fxml"));
            AnchorPane appointmentScreen = (AnchorPane) loader.load();  
            
            
            menu.setCenter(appointmentScreen);
            AppointmentScreenController controller = loader.getController();
            controller.setAppointmentScreen(this, currentUser);
                 
        }   catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public boolean showNewApptScreen(User currentUser){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ScheduleApp.class.getResource("/psantaniello/view/AppointmentEditScreen.fxml"));
            AnchorPane newApptScreen = (AnchorPane) loader.load();
        
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Appointment");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(newApptScreen);
            dialogStage.setScene(scene);
            
            AppointmentEditScreenController controller = loader.getController();
            controller.setDialogStage(dialogStage, currentUser);
            
            dialogStage.showAndWait();
        
        
        return controller.isOkClicked();
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean showEditApptScreen(Appointment appointment, User currentUser) {
        
        try {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ScheduleApp.class.getResource("/psantaniello/view/AppointmentEditScreen.fxml"));
        AnchorPane editApptScreen = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Appointment");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(editApptScreen);
        dialogStage.setScene(scene);

        AppointmentEditScreenController controller = loader.getController();
        controller.setDialogStage(dialogStage, currentUser);
        controller.setAppointment(appointment);
        dialogStage.showAndWait();

        return controller.isOkClicked();
        } catch (IOException e) {
        e.printStackTrace();
        return false;
        }
    }

    public void showCustomerScreen(User currentUser) {
        try {
            
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ScheduleApp.class.getResource("/psantaniello/view/CustomerEditScreen.fxml"));
            AnchorPane customerScreen = (AnchorPane) loader.load();

            
            menu.setCenter(customerScreen);

            
            CustomerEditScreenController controller = loader.getController();
            controller.setCustomerScreen(this, currentUser);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showReportScreen(User currentUser) {
        
        try {
            
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ScheduleApp.class.getResource("/psantaniello/view/ReportsScreen.fxml"));
            TabPane tabPane = (TabPane) loader.load();

            
            menu.setCenter(tabPane);

            
            ReportsScreenController controller = loader.getController();
            controller.setReportScreen(this, currentUser);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    
}
