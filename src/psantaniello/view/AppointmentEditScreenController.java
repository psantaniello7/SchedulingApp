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
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import psantaniello.app.ScheduleApp;
import psantaniello.model.Appointment;
import psantaniello.model.Customer;
import psantaniello.model.User;
import psantaniello.util.DBConnection;

public class AppointmentEditScreenController {

    @FXML
    private Label appointmentTitleLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Customer> customerTableView;

    @FXML
    private TableColumn<Customer, String> customerNameColumn;

    @FXML
    private TextField titleTextField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> startComboBox;

    @FXML
    private ComboBox<String> endComboBox;

    @FXML
    private ComboBox<String> descrComboBox;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField urlTextField;

    @FXML
    private Button apptSaveButton;

    @FXML
    private Button apptCancelButton;
    

    private Stage dialogStage;
    private ScheduleApp sheduleApp;
    private boolean okClicked = false;
    private  ZoneId zid = ZoneId.ofOffset("", ZoneOffset.UTC);
    private Appointment selectedAppt;
    private User currentUser;
    
    private ObservableList<Customer> masterData = FXCollections.observableArrayList();
    private final ObservableList<String> startTimes = FXCollections.observableArrayList();
    private final ObservableList<String> endTimes = FXCollections.observableArrayList();
    private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
    private final DateTimeFormatter dateDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    ObservableList<Appointment> apptTimeList;
    
    public boolean isOkClicked(){
        return okClicked;
    }
    
    public void setDialogStage(Stage dialogStage, User currentUser){
        this.dialogStage = dialogStage;
        this.currentUser = currentUser;
        
        populateDescriptionList();
        populateTypeList();
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        masterData = populateCustomerList();
        
        FilteredList<Customer> filteredData = new FilteredList<>(masterData, p -> true);
        
        //Sets filter Predicate when the filter changes
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(customer -> {
            if( newValue == null || newValue.isEmpty()) {
                return true;
            }
            
            String caseFilter = newValue.toLowerCase();
            
            if (customer.getCustomerName().toLowerCase().contains(caseFilter)) {
                return true;
            }
            return false;
            });
        });
        
        SortedList<Customer> sortedData = new SortedList<>(filteredData);
        
        sortedData.comparatorProperty().bind(customerTableView.comparatorProperty());
        
        customerTableView.setItems(sortedData);
        
        
        LocalTime time = LocalTime.of(8, 0);
        
        do{
            startTimes.add(time.format(timeDTF));
            endTimes.add(time.format(timeDTF));
            time = time.plusMinutes(15);
        } while (!time.equals(LocalTime.of(17, 15)));
            startTimes.remove(startTimes.size()-1);
            endTimes.remove(0);
            
        datePicker.setValue(LocalDate.now());
        
        startComboBox.setItems(startTimes);
        endComboBox.setItems(endTimes);
        startComboBox.getSelectionModel().select(LocalTime.of(8, 0).format(timeDTF));
        endComboBox.getSelectionModel().select(LocalTime.of(8, 15).format(timeDTF));
        
    }
    
    @FXML
    void handleApptCancelButton(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancel");
        alert.setHeaderText("Are you sure you want to Cancel?");
        alert.showAndWait()
        .filter(response -> response == ButtonType.OK)
        .ifPresent(response -> dialogStage.close());
    }

    @FXML
    void handleApptSaveButton(ActionEvent event) {
        if (validateAppointment()){
            if (isOkClicked()) {
                updateExistingAppt();            
            } else {
                saveNewAppt();
            }
            dialogStage.close();
        }
    }
    
    private void saveNewAppt(){
        
        LocalDate localDate = datePicker.getValue();
        LocalTime startTime = LocalTime.parse(startComboBox.getSelectionModel().getSelectedItem(), timeDTF);
        LocalTime endTime = LocalTime.parse(endComboBox.getSelectionModel().getSelectedItem(), timeDTF);
        
        LocalDateTime startDateTime = LocalDateTime.of(localDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(localDate, endTime);
        
        ZonedDateTime startUTC = startDateTime.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endUTC = endDateTime.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));
        
        Timestamp startSQLts = Timestamp.valueOf(startUTC.toLocalDateTime());
        Timestamp endSQLts = Timestamp.valueOf(endUTC.toLocalDateTime());
        
        try {
            
            PreparedStatement pst = DBConnection.getConn().prepareStatement("INSERT INTO appointment " +
                    "(customerId, title, description, location, contact, type, url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy)"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)");
            
            pst.setString(1, customerTableView.getSelectionModel().getSelectedItem().getCustomerId());
            pst.setString(2, titleTextField.getText());
            pst.setString(3, descrComboBox.getValue());
            pst.setString(4, "");
            pst.setString(5, currentUser.getUserName());
            pst.setString(6,typeComboBox.getValue());
            pst.setString(7,urlTextField.getText());
            pst.setTimestamp(8, startSQLts);
            pst.setTimestamp(9, endSQLts);
            pst.setString(10, currentUser.getUserName());
            pst.setString(11, currentUser.getUserName());
            int result = pst.executeUpdate();
            if (result == 1){
                    System.out.println("YAY! New Appointment Save");
                } else {
                    System.out.println("BOO! New Appointment Save");
            }           
        } catch (SQLException ex){
            ex.printStackTrace();
        }  
    }
    
    private void updateExistingAppt(){
        LocalDate localDate = datePicker.getValue();
	LocalTime startTime = LocalTime.parse(startComboBox.getSelectionModel().getSelectedItem(), timeDTF);
	LocalTime endTime = LocalTime.parse(endComboBox.getSelectionModel().getSelectedItem(), timeDTF);
        
        LocalDateTime startDT = LocalDateTime.of(localDate, startTime);
        LocalDateTime endDT = LocalDateTime.of(localDate, endTime);

        ZonedDateTime startUTC = startDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endUTC = endDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));            
	
	Timestamp startsqlts = Timestamp.valueOf(startUTC.toLocalDateTime()); 
        Timestamp endsqlts = Timestamp.valueOf(endUTC.toLocalDateTime());        
        
        try {

                PreparedStatement pst = DBConnection.getConn().prepareStatement("UPDATE appointment "
                        + "SET customerId = ?, title = ?, description = ?, type = ?, url = ?, start = ?, end = ?, lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy = ? "
                        + "WHERE appointmentId = ?");
            
                pst.setString(1, customerTableView.getSelectionModel().getSelectedItem().getCustomerId());
                pst.setString(2, titleTextField.getText());
                pst.setString(3, descrComboBox.getValue());
                pst.setString(4, typeComboBox.getValue());
                pst.setString(5, urlTextField.getText());
                pst.setTimestamp(6, startsqlts);
                pst.setTimestamp(7, endsqlts);
                pst.setString(8, currentUser.getUserName());
                pst.setString(9, selectedAppt.getAppointmentId());
                int result = pst.executeUpdate();
                if (result == 1) {
                    System.out.println("YAY! Update Appointment Save");
                } else {
                    System.out.println("BOO! Update Appointment Save");
                }
            } catch (SQLException ex) {
            ex.printStackTrace();
            }
        
    }
    
    public void setAppointment(Appointment appointment){
        okClicked = true;
        selectedAppt = appointment;
        
        String start = appointment.getStart();
        
        LocalDateTime startLDT = LocalDateTime.parse(start, dateDTF);
        String end = appointment.getEnd();
        LocalDateTime endLDT = LocalDateTime.parse(end, dateDTF);        
        
        appointmentTitleLabel.setText("Edit Appointment");
        titleTextField.setText(appointment.getTitle());
        descrComboBox.setValue(appointment.getDescription());
        typeComboBox.setValue(appointment.getType());
        urlTextField.setText(appointment.getUrl());
        customerTableView.getSelectionModel().select(appointment.getCustomer());
        datePicker.setValue(LocalDate.parse(appointment.getStart(), dateDTF));
        startComboBox.getSelectionModel().select(startLDT.toLocalTime().format(timeDTF));
        endComboBox.getSelectionModel().select(endLDT.toLocalTime().format(timeDTF));
        
    }
    
    private boolean validateAppointment() {
        String title = titleTextField.getText();
        String description = descrComboBox.getValue();
        String type = typeComboBox.getValue();
        String url = urlTextField.getText();
        Customer customer = customerTableView.getSelectionModel().getSelectedItem();
        LocalDate localDate = datePicker.getValue();
	LocalTime startTime = LocalTime.parse(startComboBox.getSelectionModel().getSelectedItem(), timeDTF);
	LocalTime endTime = LocalTime.parse(endComboBox.getSelectionModel().getSelectedItem(), timeDTF);
        
        LocalDateTime startDT = LocalDateTime.of(localDate, startTime);
        LocalDateTime endDT = LocalDateTime.of(localDate, endTime);

        ZonedDateTime startUTC = startDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endUTC = endDT.atZone(zid).withZoneSameInstant(ZoneId.of("UTC"));            
	        
        String errorMessage = "";
        
        if (title == null || title.length() == 0) {
            errorMessage += "Please enter an Appointment title.\n"; 
        }
        if (description == null || description.length() ==0){
            errorMessage += "Please enter an Appointment description.\n";
        }
        if (type == null || type.length() == 0) {
            errorMessage += "Please select an Appointment type.\n";  
        } 
        if (url == null || url.length() ==0){
            urlTextField.setText("");
        }
        if (customer == null) {
            errorMessage += "Please Select a Customer.\n"; 
        } 
        if (startUTC == null) {
            errorMessage += "Please select a Start time"; 
        }         
        if (endUTC == null) {
            errorMessage += "Please select an End time.\n"; 
            
            } else if (endUTC.equals(startUTC) || endUTC.isBefore(startUTC)){
                errorMessage += "End time must be after Start time.\n";
            } else try {
                
                if (hasApptConflict(startUTC, endUTC)){
                    errorMessage += "Appointment times conflict with Consultant's existing appointments. Please select a new time.\n";
                }
        } catch (SQLException ex) {
            Logger.getLogger(AppointmentEditScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (errorMessage.length() == 0) {
            return true;
        } else {
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid Appointment fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    private boolean hasApptConflict(ZonedDateTime newStart, ZonedDateTime newEnd) throws SQLException {
        String apptID;
        String consultant;
        if (isOkClicked()) {
            
            apptID = selectedAppt.getAppointmentId();
            consultant = selectedAppt.getUser();
        } else {
            
            apptID = "0";
            consultant = currentUser.getUserName();
        }
        System.out.println("ApptID: " + apptID);
        
        try{
            
            
        PreparedStatement pst = DBConnection.getConn().prepareStatement(
        "SELECT * FROM appointment "
	+ "WHERE (? BETWEEN start AND end OR ? BETWEEN start AND end OR ? < start AND ? > end) "
	+ "AND (createdBy = ? AND appointmentID != ?)");
        pst.setTimestamp(1, Timestamp.valueOf(newStart.toLocalDateTime()));
	pst.setTimestamp(2, Timestamp.valueOf(newEnd.toLocalDateTime()));
        pst.setTimestamp(3, Timestamp.valueOf(newStart.toLocalDateTime()));
	pst.setTimestamp(4, Timestamp.valueOf(newEnd.toLocalDateTime()));
        pst.setString(5, consultant);
        pst.setString(6, apptID);
        ResultSet rs = pst.executeQuery();
           
        if(rs.next()) {
            return true;
	}
            
        } catch (SQLException sqe) {
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
            e.printStackTrace();
        }
        return false;
    }
    
    private void populateTypeList(){
        ObservableList<String> typeList = FXCollections.observableArrayList();
        typeList.addAll("In Person", "Phone");
        typeComboBox.setItems(typeList);
    
    }

    private void populateDescriptionList(){
        ObservableList<String> descriptionList = FXCollections.observableArrayList();
        descriptionList.addAll("First Meeting", "Follow-up", "First Consultation");
        descrComboBox.setItems(descriptionList);   
    }


    
    protected ObservableList<Customer> populateCustomerList(){
        
        String tCustomerId;
        String tCustomerName;
        
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        
        try (
                
                PreparedStatement statement = DBConnection.getConn().prepareStatement(
                "SELECT customer.customerId, customer.customerName " + 
                "FROM customer, address, city, country "+
                "WHERE customer.addressId = address.addressId AND address.cityId = city.cityId AND city.countryId = country.countryId");
                    ResultSet rs = statement.executeQuery();){
            
            while (rs.next()) {
                tCustomerId = rs.getString("customer.customerId");
                tCustomerName = rs.getString("customer.customerName");
                customerList.add(new Customer(tCustomerId, tCustomerName));
            }
        } catch (SQLException sqe){
                System.out.println("Check your SQL");
                sqe.printStackTrace();
        } catch (Exception e){
            System.out.println("Something besides the SQL went wrong.");

    }        
        
    return customerList;
    }

}
