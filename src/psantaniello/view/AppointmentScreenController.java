/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package psantaniello.view;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import psantaniello.app.ScheduleApp;
import psantaniello.model.Appointment;
import psantaniello.model.Customer;
import psantaniello.model.User;
import psantaniello.util.DBConnection;

/**
 * FXML Controller class
 *
 * @author Paolo
 */
public class AppointmentScreenController{

    @FXML
    private TableView<Appointment> apptTableView;

    @FXML
    private TableColumn<Appointment, Customer> customerApptColumn;

    @FXML
    private TableColumn<Appointment, String> titleApptColumn;

    @FXML
    private TableColumn<Appointment, String> typeApptColumn;

    @FXML
    private TableColumn<Appointment, String> contactApptColumn;

    @FXML
    private TableColumn<Appointment, ZonedDateTime> startApptColumn;

    @FXML
    private TableColumn<Appointment, LocalDateTime> endApptColumn;

    @FXML
    private RadioButton weekRadioButton;

    @FXML
    private ToggleGroup ApptView;

    @FXML
    private RadioButton monthRadioButton;
    
    private ScheduleApp scheduleApp;
    private User currentUser;
    private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    private  ZoneId newzid = ZoneId.ofOffset("", ZoneOffset.UTC);
    ObservableList<Appointment> apptList;
    

    

    @FXML
    void handleDeleteAppt(ActionEvent event) {
        Appointment  selectedAppointment = apptTableView.getSelectionModel().getSelectedItem();
        
        if (selectedAppointment != null){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Appointment");
                alert.setHeaderText("Delete " + selectedAppointment.getTitle() + " scheduled for " + selectedAppointment.getStart()+" ?");
                alert.showAndWait()
                .filter(response -> response ==  ButtonType.OK)
                .ifPresent(response -> {
                deleteAppointment(selectedAppointment);
                scheduleApp.showAppointmentScreen(currentUser);
                }
            );
        }  else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Appointment Selected");
            alert.setHeaderText("No Appointment selected.");
            alert.setContentText("Please select an appointment to delete.");
            alert.showAndWait();
        }          
    }

    private void deleteAppointment(Appointment appointment){
        try{
            PreparedStatement pst = DBConnection.getConn().prepareStatement("DELETE appointment.* FROM appointment WHERE appointment.appointmentId = ?");
            pst.setString(1, appointment.getAppointmentId());
            pst.executeUpdate();
            
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    @FXML
    void handleEditAppt(ActionEvent event) {
        Appointment selectedAppointment = apptTableView.getSelectionModel().getSelectedItem();
        
        if (selectedAppointment != null) {
            boolean okClicked = scheduleApp.showEditApptScreen(selectedAppointment, currentUser);
            scheduleApp.showAppointmentScreen(currentUser);
            
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Appointment selected");
            alert.setContentText("Please select an Appointment in the Table");
            alert.showAndWait();
        }

    }

    @FXML
    void handleNewAppt(ActionEvent event) {
        boolean okClicked = scheduleApp.showNewApptScreen(currentUser);
        scheduleApp.showAppointmentScreen(currentUser);
        
    }

    public void setAppointmentScreen(ScheduleApp scheduleApp, User currentUser) {
        this.scheduleApp = scheduleApp;
        this.currentUser = currentUser;
        
        customerApptColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        titleApptColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeApptColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        startApptColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endApptColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        contactApptColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        
        apptList = FXCollections.observableArrayList();
        fillAppointmentList();
        apptTableView.getItems().setAll(apptList);
        
        
        
        
       
    }
    
    @FXML
    void handleApptMonth(ActionEvent event) {
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlusMonth = now.plusMonths(1);
        //Filters to display appointments from current date to 1 month out
        FilteredList<Appointment> filteredData = new FilteredList<>(apptList);
        filteredData.setPredicate(row -> {
            LocalDateTime rowDate = LocalDateTime.parse(row.getStart(), timeDTF);
            return rowDate.isAfter(now) && rowDate.isBefore(nowPlusMonth);
        });
        
        apptTableView.setItems(filteredData);
        
    }

    @FXML
    void handleApptWeek(ActionEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlusWeek = now.plusDays(7);
        //Filters to display appointments from current date to 7 days out
        FilteredList<Appointment> filteredData = new FilteredList<>(apptList);
        filteredData.setPredicate(row -> {
            LocalDateTime rowDate = LocalDateTime.parse(row.getStart(), timeDTF);
            return rowDate.isAfter(now) && rowDate.isBefore(nowPlusWeek);
        });
        
        apptTableView.setItems(filteredData);
    }

    private void fillAppointmentList() {

        
        try{
            
            PreparedStatement statement = DBConnection.getConn().prepareStatement(
        "SELECT appointment.appointmentId, appointment.customerId, appointment.title, appointment.description, appointment.type ,"
                + "appointment.`start`, appointment.url , appointment.`end`, customer.customerId, customer.customerName, appointment.createdBy "
                + "FROM appointment, customer "
                + "WHERE appointment.customerId = customer.customerId "
                + "ORDER BY `start` DESC");
            ResultSet rs = statement.executeQuery();
            
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
                      
                apptList.add(new Appointment(tAppointmentId, tCustomer, tTitle, tType, tDescription, tUser, tUrl, newLocalStart.format(timeDTF), newLocalEnd.format(timeDTF))); 
            }
            
        } catch (SQLException sqe){
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } catch (Exception e){
            System.out.println("Something besides the SQL went wrong!");
        }
    }


}
