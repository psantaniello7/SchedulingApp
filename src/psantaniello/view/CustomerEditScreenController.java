/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package psantaniello.view;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import psantaniello.app.ScheduleApp;
import psantaniello.model.City;
import psantaniello.model.Customer;
import psantaniello.model.User;
import psantaniello.util.DBConnection;

public class CustomerEditScreenController {

    @FXML
    private TableView<Customer> customerTableview;

    @FXML
    private TableColumn<Customer, String> customerColumn;

    @FXML
    private TableColumn<Customer, String> phoneColumn;

    @FXML
    private TextField customerIdField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField address2Field;

    @FXML
    private ComboBox<City> cityComboBox;
    
    @FXML
    private TextField countryField;

    @FXML
    private TextField postalField;

    @FXML
    private ButtonBar saveCancelButtonBar;
        
    @FXML
    private ButtonBar newEditDeleteButtonBar;
   
    private ScheduleApp scheduleApp;
    private boolean editClicked = false;
    private Stage dialogStage;
    private User currentUser;
    
    
   
    public void setCustomerScreen(ScheduleApp scheduleApp, User currentUser) {
	this.scheduleApp = scheduleApp;
        this.currentUser = currentUser;
        
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        disableCustomerFields();
        
        populateCityList();
        
        cityComboBox.setConverter(new StringConverter<City>() {

        @Override
        public String toString(City object) {
        return object.getCity();
        }     

        @Override
        public City fromString(String string) {
        return cityComboBox.getItems().stream().filter(ap -> 
            ap.getCity().equals(string)).findFirst().orElse(null);
        }
        });
        
        cityComboBox.valueProperty().addListener((obs, oldval, newval) -> {
            if(newval != null)
                showCountry(newval.toString());
        });
        
        customerTableview.getItems().setAll(populateCustomerList());          
        customerTableview.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue)->showCustomerDetails(newValue));
        
    }
    
    @FXML
    void handleNewCustomer(ActionEvent event) {
        editClicked = false;
        enableCustomerFields();
        saveCancelButtonBar.setDisable(false);
        customerTableview.setDisable(true);
        clearCustomerDetails();
        customerIdField.setText("Auto-Generated");
        newEditDeleteButtonBar.setDisable(true);
    }
    
    @FXML
    void handleEditCustomer(ActionEvent event) {
        Customer selectedCustomer = customerTableview.getSelectionModel().getSelectedItem();
        
        if (selectedCustomer != null) {
            editClicked = true;
            enableCustomerFields();
            saveCancelButtonBar.setDisable(false);
            customerTableview.setDisable(true);
            newEditDeleteButtonBar.setDisable(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Customer selected");
            alert.setContentText("Please select a Customer in the Table");
            alert.showAndWait();
        }
        
        
    }
    
    @FXML
    void handleDeleteCustomer(ActionEvent event) {
        Customer selectedCustomer = customerTableview.getSelectionModel().getSelectedItem();
        
        if (selectedCustomer != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Are you sure you want to delete " + selectedCustomer.getCustomerName() + "?");
            alert.showAndWait()
            .filter(response -> response == ButtonType.OK)
            .ifPresent(response -> {
                deleteCustomer(selectedCustomer);
                scheduleApp.showCustomerScreen(currentUser);
                }
            );
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Customer selected for Deletion");
            alert.setContentText("Please select a Customer in the Table to delete");
            alert.showAndWait();
        }
        
    }
    
    @FXML
    void handleSaveCustomer(ActionEvent event) {
        if (validateCustomer()){
            saveCancelButtonBar.setDisable(true);
            customerTableview.setDisable(false);
            if (editClicked == true) {
                updateCustomer();
            } else if (editClicked == false){
                saveCustomer();
        }
        scheduleApp.showCustomerScreen(currentUser);
        } 
    }
    
    @FXML
    void handleCancelCustomer(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancel");
        alert.setHeaderText("Are you sure you want to Cancel?");
        alert.showAndWait()
        .filter(response -> response == ButtonType.OK)
        .ifPresent(response -> {
            saveCancelButtonBar.setDisable(true);
            customerTableview.setDisable(false);
            clearCustomerDetails();
            newEditDeleteButtonBar.setDisable(false);
            editClicked = false;
            }
        );
    }
    
    private void disableCustomerFields() {
        
        nameField.setEditable(false);
        addressField.setEditable(false);
        address2Field.setEditable(false);
        postalField.setEditable(false);
        phoneField.setEditable(false);
    }

    @FXML
    private void showCustomerDetails(Customer selectedCustomer) {
        
     
        customerIdField.setText(selectedCustomer.getCustomerId());
        nameField.setText(selectedCustomer.getCustomerName());
        addressField.setText(selectedCustomer.getAddress());
        address2Field.setText(selectedCustomer.getAddress2());
        cityComboBox.setValue(selectedCustomer.getCity());
        countryField.setText(selectedCustomer.getCountry());
        postalField.setText(selectedCustomer.getPostalCode());
        phoneField.setText(selectedCustomer.getPhone());

    }

    private void enableCustomerFields() {
        
        nameField.setEditable(true);
        addressField.setEditable(true);
        address2Field.setEditable(true);
        postalField.setEditable(true);
        phoneField.setEditable(true);
    }

    @FXML
    private void clearCustomerDetails() {
     
        customerIdField.clear();
        nameField.clear();
        addressField.clear();
        address2Field.clear();
        countryField.clear();
        postalField.clear();
        phoneField.clear();

    }
    
    protected List<Customer> populateCustomerList() {
      
        String tCustomerId;
        String tCustomerName;
        String tAddress;
        String tAddress2;
        City tCity;
        String tCountry;
        String tPostalCode;
        String tPhone;
        
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        try(
            
            
        PreparedStatement statement = DBConnection.getConn().prepareStatement(
        "SELECT customer.customerId, customer.customerName, address.address, address.address2, address.postalCode, city.cityId, city.city, country.country, address.phone " +
        "FROM customer, address, city, country " +
        "WHERE customer.addressId = address.addressId AND address.cityId = city.cityId AND city.countryId = country.countryId " +
        "ORDER BY customer.customerName");
            ResultSet rs = statement.executeQuery();){
           
            
            while (rs.next()) {
                tCustomerId = rs.getString("customer.customerId");

                tCustomerName = rs.getString("customer.customerName");

                tAddress = rs.getString("address.address");
                
                tAddress2 = rs.getString("address.address2");
                
                tCity = new City(rs.getInt("city.cityId"), rs.getString("city.city"));
                
                tCountry = rs.getString("country.country");
                
                tPostalCode = rs.getString("address.postalCode");
                
                tPhone = rs.getString("address.phone");

                customerList.add(new Customer(tCustomerId, tCustomerName, tAddress, tAddress2, tCity, tCountry, tPostalCode, tPhone));

            }
          
        } catch (SQLException sqe) {
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
        }

         
        return customerList;

    }

    protected void populateCityList() {
    
    
    ObservableList<City> cities = FXCollections.observableArrayList();
    
    try(

    PreparedStatement statement = DBConnection.getConn().prepareStatement("SELECT cityId, city FROM city LIMIT 100;");
    ResultSet rs = statement.executeQuery();){
    
    while (rs.next()) {
        cities.add(new City(rs.getInt("city.cityId"),rs.getString("city.city")));
    }
    
    
    } catch (SQLException sqe) {
    System.out.println("Check your SQL");
    sqe.printStackTrace();
    } catch (Exception e) {
    System.out.println("Something besides the SQL went wrong.");
    }
    
    
    cityComboBox.setItems(cities);
    
    }
    
    @FXML
    private void showCountry(String citySelection) {
        
        
     try{
            
            PreparedStatement pst = DBConnection.getConn().prepareStatement(
            "SELECT city.cityId, city.city, country.country "
                + "FROM city, country "
                + " WHERE city.countryId = country.countryId AND city.city = ?");
            pst.setString(1, citySelection);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()){
                String city = rs.getString("city.city");
                if (citySelection.equals(city)){
                    //System.out.println(city);
                    countryField.setText(rs.getString("country.country"));
                }
            }
            
        } catch (SQLException sqe){
            System.out.println("Check your SQL");
            sqe.printStackTrace();
        } catch (Exception e){
            System.out.println("Something besides the SQL went wrong!");
        }

    }
    

    private void saveCustomer() {

            try {

                PreparedStatement ps = DBConnection.getConn().prepareStatement("INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) "
                        + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)",Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, addressField.getText());
                ps.setString(2, address2Field.getText());
                ps.setInt(3, cityComboBox.getValue().getCityId());
                ps.setString(4, postalField.getText());
                ps.setString(5, phoneField.getText());
                ps.setString(6, currentUser.getUserName());
                ps.setString(7, currentUser.getUserName());
                boolean res = ps.execute();
                int newAddressId = -1;
                ResultSet rs = ps.getGeneratedKeys();
                
                if(rs.next()){
                    newAddressId = rs.getInt(1);
                    
                }
            
            
                PreparedStatement psc = DBConnection.getConn().prepareStatement("INSERT INTO customer "
                + "(customerName, addressId, active, createDate, createdBy, lastUpdate, lastUpdateBy)"
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)");
            
                psc.setString(1, nameField.getText());
                psc.setInt(2, newAddressId);
                psc.setInt(3, 1);
                
                psc.setString(4, currentUser.getUserName());
                
                psc.setString(5, currentUser.getUserName());
                int result = psc.executeUpdate();
               
            } catch (SQLException ex) {
            ex.printStackTrace();
            }
    }

    private void updateCustomer() {
        try {

                PreparedStatement ps = DBConnection.getConn().prepareStatement("UPDATE address, customer, city, country "
                        + "SET address = ?, address2 = ?, address.cityId = ?, postalCode = ?, phone = ?, address.lastUpdate = CURRENT_TIMESTAMP, address.lastUpdateBy = ? "
                        + "WHERE customer.customerId = ? AND customer.addressId = address.addressId AND address.cityId = city.cityId AND city.countryId = country.countryId");

                ps.setString(1, addressField.getText());
                ps.setString(2, address2Field.getText());
                ps.setInt(3, cityComboBox.getValue().getCityId());
                ps.setString(4, postalField.getText());
                ps.setString(5, phoneField.getText());
                ps.setString(6, currentUser.getUserName());
                ps.setString(7, customerIdField.getText());
                
                int result = ps.executeUpdate();
                             
            
            
                PreparedStatement psc = DBConnection.getConn().prepareStatement("UPDATE customer, address, city "
                + "SET customerName = ?, customer.lastUpdate = CURRENT_TIMESTAMP, customer.lastUpdateBy = ? "
                + "WHERE customer.customerId = ? AND customer.addressId = address.addressId AND address.cityId = city.cityId");
            
                psc.setString(1, nameField.getText());
                psc.setString(2, currentUser.getUserName());
                psc.setString(3, customerIdField.getText());
                int results = psc.executeUpdate();
                
            } catch (SQLException ex) {
            ex.printStackTrace();
            }
    }

    private boolean validateCustomer() {
        String name = nameField.getText();
        String address = addressField.getText();
        City city = cityComboBox.getValue();
        String country = countryField.getText();
        String zip = postalField.getText();
        String phone = phoneField.getText();
        
        String errorMessage = "";
        
        if (name == null || name.length() == 0) {
            errorMessage += "Please enter the Customer's name.\n"; 
        }
        if (address == null || address.length() == 0) {
            errorMessage += "Please enter an address.\n";  
        } 
        if (city == null) {
            errorMessage += "Please Select a City.\n"; 
        } 
        if (country == null || country.length() == 0) {
            errorMessage += "No valid Country. Country set by City.\n"; 
        }         
        if (zip == null || zip.length() == 0) {
            errorMessage += "Please enter the Postal Code.\n"; 
        } else if (zip.length() > 10 || zip.length() < 5){
            errorMessage += "Please enter a valid Postal Code.\n";
        }
        if (phone == null || phone.length() == 0) {
            errorMessage += "Please enter a Phone Number (including Area Code)."; 
        } else if (phone.length() < 10 || phone.length() > 15 ){
            errorMessage += "Please enter a valid phone number (including Area Code).\n";
        }        
        if (errorMessage.length() == 0) {
            return true;
        } else {
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please input invalid Customer fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    private void deleteCustomer(Customer customer) {
        
        try{           
            PreparedStatement pst = DBConnection.getConn().prepareStatement("DELETE customer.*, address.* from customer, address WHERE customer.customerId = ? AND customer.addressId = address.addressId");
            pst.setString(1, customer.getCustomerId()); 
            pst.executeUpdate();   
                
        } catch(SQLException e){
            e.printStackTrace();
        }       
    }

    
}

