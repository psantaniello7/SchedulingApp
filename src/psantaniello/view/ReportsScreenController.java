/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package psantaniello.view;

import psantaniello.model.AppointmentReport;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import psantaniello.model.Appointment;
import psantaniello.model.Appointment;
import psantaniello.model.Customer;
import psantaniello.util.DBConnection;
import psantaniello.app.ScheduleApp;
import psantaniello.model.User;

public class ReportsScreenController {

    @FXML
    private TabPane tabPane;
    
    @FXML
    private Tab schedTab;

    @FXML
    private TableView<Appointment> schedTableview;

    @FXML
    private TableColumn<Appointment, ZonedDateTime> startSchedColumn;

    @FXML
    private TableColumn<Appointment, LocalDateTime> endSchedColumn;

    @FXML
    private TableColumn<Appointment, String> titleSchedColumn;

    @FXML
    private TableColumn<Appointment, String> descrSchedColumn;

    @FXML
    private TableColumn<Appointment, Customer> custSchedColumn;

    @FXML
    private Tab apptTab;

    @FXML
    private TableView<AppointmentReport> apptTableview;

    @FXML
    private TableColumn<AppointmentReport, String> monthApptColumn;

    @FXML
    private TableColumn<AppointmentReport, String> typeApptColumn;

    @FXML
    private TableColumn<AppointmentReport, String> totalApptColumn;

    @FXML
    private Tab custTab;

    @FXML
    private PieChart pieChart;
    
    private ScheduleApp scheduleApp;
    private ObservableList<AppointmentReport> apptList;
    private ObservableList<Appointment> schedule;
    private ObservableList<PieChart.Data> data;
    private final DateTimeFormatter timeDTF = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    private  ZoneId newzid = ZoneId.ofOffset("", ZoneOffset.UTC);
    private User currentUser;
    
    
    
    public void setReportScreen(ScheduleApp scheduleApp, User currentUser) {
        this.scheduleApp = scheduleApp;
        this.currentUser = currentUser;
        //System.out.println("TEST");
        
               
        populateApptTypeList();
        populateCustPieChart();
        populateSchedule();     
        
        startSchedColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endSchedColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        titleSchedColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descrSchedColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        custSchedColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        
        monthApptColumn.setCellValueFactory(new PropertyValueFactory<>("Month"));
        typeApptColumn.setCellValueFactory(new PropertyValueFactory<>("Type"));
        totalApptColumn.setCellValueFactory(new PropertyValueFactory<>("Total"));
        
        
    }
    
    
    
    private void populateApptTypeList() {
        apptList = FXCollections.observableArrayList();
        
        try{
            
            
        PreparedStatement statement = DBConnection.getConn().prepareStatement(
            "SELECT MONTHNAME(`start`) AS \"Month\", description AS \"Type\", COUNT(*) AS \"Total\" "
            + "FROM appointment "
            + "GROUP BY MONTHNAME(`start`), description");
            ResultSet rs = statement.executeQuery();
           
            
            while (rs.next()) {
                
                String month = rs.getString("Month");
                
                String type = rs.getString("Type");

                String total = rs.getString("Total");
                      
                apptList.add(new AppointmentReport(month, type, total));
                
                

            }
            
        } catch (SQLException sqe) {
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
        }
        
        apptTableview.getItems().setAll(apptList);
    }
    
    private void populateCustPieChart() {
        
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        

            try { PreparedStatement pst = DBConnection.getConn().prepareStatement(
                  "SELECT city.city, COUNT(city) "
                + "FROM customer, address, city "
                + "WHERE customer.addressId = address.addressId "
                + "AND address.cityId = city.cityId "
                + "GROUP BY city"); 
                ResultSet rs = pst.executeQuery();


                while (rs.next()) {
                        String city = rs.getString("city");
                        Integer count = rs.getInt("COUNT(city)");
                        data.add(new PieChart.Data(city , count));
                }

            } catch (SQLException sqe) {
                System.out.println("Check your SQL");
                sqe.printStackTrace();
            } catch (Exception e) {
                System.out.println("Something besides the SQL went wrong.");
                e.printStackTrace();
            }             
        
        pieChart.getData().addAll(data);
    }
    
    private void populateSchedule() {
      
        schedule = FXCollections.observableArrayList();
        
        try{
            
        PreparedStatement statement = DBConnection.getConn().prepareStatement(
        "SELECT appointment.appointmentId, appointment.customerId, appointment.title, appointment.description, appointment.type, "
                + "appointment.`start`, appointment.url, appointment.`end`, customer.customerId, customer.customerName, appointment.createdBy "
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
                      
                schedule.add(new Appointment(tAppointmentId, tCustomer, tTitle, tType, tDescription, tUser, tUrl, newLocalStart.format(timeDTF), newLocalEnd.format(timeDTF))); 
            }
            
        } catch (SQLException sqe) {
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
        }
        schedTableview.getItems().setAll(schedule);
    }

}
