/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package psantaniello.view;

import java.sql.*;
import java.util.Optional;
import javafx.stage.Modality;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import psantaniello.app.ScheduleApp;
import psantaniello.model.Appointment;
import psantaniello.model.Customer;
import psantaniello.model.User;
import psantaniello.util.DBConnection;
import psantaniello.util.LoggerUtil;

/**
 *
 * @author Paolo
 */





public class LoginScreenController {

    @FXML
    private Label titleText;

    @FXML
    private Label signInText;

    @FXML
    private Label userText;
    
    @FXML
    private TextField usernameField;

    @FXML
     private PasswordField passwordField;
    
    @FXML
    private Label passwordText;

    @FXML
    private Label loginValidation;

    @FXML
    private Button loginButton;
    
    @FXML
    private Button cancelButton;
    
    private ScheduleApp scheduleApp;
    User user = new User();
    ResourceBundle rb = ResourceBundle.getBundle("login", Locale.getDefault());
    private  ZoneId newzid = ZoneId.ofOffset("", ZoneOffset.UTC);
    private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    ObservableList<Appointment> reminderLists;
    private final static Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
    

    public LoginScreenController() {
    }
    
    @FXML
    void loginButtonHandler(ActionEvent event) {
        
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if(username.length()==0 || password.length()==0)
                loginValidation.setText(rb.getString("empty"));
        else{
            User validUser = loginValidation(username, password);
            if (validUser == null){
                loginValidation.setText(rb.getString("incorrect"));
                return;
            }
            populateReminderList();
            reminder();
            scheduleApp.showHomeScreen(validUser);
            scheduleApp.showAppointmentScreen(validUser);
            LOGGER.log(Level.INFO, "{0} logged in", validUser.getUserName());
            
        }
         
        
    }
    
    User loginValidation(String username, String password){
        try{
            PreparedStatement ps = DBConnection.getConn().prepareStatement("SELECT * FROM user WHERE userName=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs= ps.executeQuery();
            if(rs.next()){
                user.setUserName(rs.getString("userName"));
                user.setPassword(rs.getString("password"));
                user.setUserId(rs.getInt("userId"));
            }else{
                return null;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return user;
    }
    
    
    
    @FXML
    void cancelButtonHandler(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.NONE);
        
        alert.setTitle(rb.getString("exit"));
        
        alert.setHeaderText(rb.getString("confirm"));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.exit(0);
        }
        else {
            System.out.println("You canceled our request.");
        }
    }

    public void setLoginScreen(ScheduleApp scheduleApp) {
        
        this.scheduleApp = scheduleApp;
        reminderLists = FXCollections.observableArrayList();
        
        titleText.setText(rb.getString("title"));
        signInText.setText(rb.getString("signin"));
        userText.setText(rb.getString("username"));
        passwordText.setText(rb.getString("password"));
        loginButton.setText(rb.getString("login"));
        cancelButton.setText(rb.getString("cancel"));
        
    }
    
    private void reminder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlus15Min = now.plusMinutes(15);
        
        FilteredList<Appointment> filteredData = new FilteredList<>(reminderLists);
        //Filter list using Predicate filteredData and launches an alert if list is populated
        filteredData.setPredicate(row -> {
            LocalDateTime rowDateTime = LocalDateTime.parse(row.getStart(), timeDTF);
            return rowDateTime.isAfter(now) && rowDateTime.isBefore(nowPlus15Min);
            
            
            }
        );
        if (filteredData.isEmpty()) {
            System.out.println("No reminders");
        } else {
            String description = filteredData.get(0).getDescription();
            String customer = filteredData.get(0).getCustomer().getCustomerName();
            String start = filteredData.get(0).getStart();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Appointment Reminder");
            alert.setHeaderText("Reminder: You have the an appointment scheduled within the next 15 minutes.");
            alert.setContentText("Your next " + description + " appointment with " + customer +
                " is currently scheduled for " + start + ".");
            alert.showAndWait();
            
        }
    }

        private void populateReminderList() {
        System.out.println(user.getUserName());
        
        try{
            
            PreparedStatement pst = DBConnection.getConn().prepareStatement(
            "SELECT appointment.appointmentId, appointment.customerId, appointment.title, appointment.description, appointment.type, "
                + "appointment.`start`, appointment.url, appointment.`end`, customer.customerId, customer.customerName, appointment.createdBy "
                + "FROM appointment, customer "
                + " WHERE appointment.customerId = customer.customerId AND appointment.createdBy = ? "
                + "ORDER BY `start`");
            pst.setString(1, user.getUserName());
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()){
               String tAppointmentId = rs.getString("appointment.appointmentId");
                Timestamp tsStart = rs.getTimestamp("appointment.start");
                ZonedDateTime newzdtStart = tsStart.toLocalDateTime().atZone(ZoneId.of("UTC"));
        	ZonedDateTime newLocalStart = newzdtStart.withZoneSameInstant(newzid);

                Timestamp tsEnd = rs.getTimestamp("appointment.end");
                ZonedDateTime newzdtEnd = tsEnd.toLocalDateTime().atZone(ZoneId.of("UTC"));
        	ZonedDateTime newLocalEnd = newzdtEnd.withZoneSameInstant(newzid);

                String tTitle = rs.getString("appointment.title");
                
                String tDescription = rs.getString("appointment.description");
                
                String tType = rs.getString("appointment.type");
                
                String tUrl = rs.getString("appointment.url");
                
                Customer tCustomer = new Customer(rs.getString("appointment.customerId"), rs.getString("customer.customerName"));
                
                String tUser = rs.getString("appointment.createdBy");
                      
                reminderLists.add(new Appointment(tAppointmentId, tCustomer, tTitle, tType, tDescription, tUser, tUrl, newLocalStart.format(timeDTF), newLocalEnd.format(timeDTF))); 
            }
            
        } catch (SQLException sqe){
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } catch (Exception e){
            System.out.println("Something besides the SQL went wrong!");
        }
        
        
    }
}

