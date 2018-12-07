/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package psantaniello.model;

/**
 *
 * @author Paolo
 */
public class Appointment {
    
    private String appointmentId;
    private Customer customer;
    private String user;
    private String title;
    private String description;
    private String location;
    private String contact;
    private String type;
    private String url;
    private String start;
    private String end;
    
    public Appointment(){
        
    }
    
    public Appointment(String appointmentId){
        this.appointmentId = appointmentId;
    }
    
    public Appointment(String appointmentId, Customer customer, String title, String type, String description, String user, String url, String start, String end){
        this.appointmentId = appointmentId;
        this.customer = customer;
        this.title = title;
        this.type = type;
        this.description = description;
        this.user = user;
        this.url = url;
        this.start = start;
        this.end = end;
    }
    
    public Appointment(String start, String end, String user){
        this.user = user;
        this.start = start;
        this.end = end;
    }


    
    public String getAppointmentId(){
        return appointmentId;
    }
    
    public void setAppointmentId(String appointmentId){
        this.appointmentId = appointmentId;
    }
    
    public Customer getCustomer(){
        return customer;
    }
    
    public void setCustomerName(Customer customer){
        this.customer = customer;
    }
    

    public String getUser(){
        return user;
    }
    
    public void setUser(String user){
        this.user = user;
    }
    
    public String getTitle(){
        return title;
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    
    public String getDescription(){
        return description;
    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public String getLocation(){
        return location;
    }
    
    public void setLocation(String location){
        this.location = location;
    }
    
    public String getContact(){
        return contact;
    }
    
    public void setContact(String contact){
        this.contact = contact;
    }
    
    public String getType(){
        return type;
    }
    
    public void setType(String type){
        this.type = type;
    }
    
    public String getUrl(){
        return url;
    }
    
    public void setUrl(String url){
        this.url = url;
    }
    
    public String getStart(){
        return start;
    }
    
    public void setStart(String start){
        this.start = start;
    }
    
    public String getEnd(){
        return end;
    }
    
    public void setEnd(String end){
        this.end = end;
    }
    
    @Override
    public String toString() {
        return "ID: " + this.appointmentId +
                " Start: " + this.start +
                " End: " + this.end +
                " Title: " + this.title +
                " Description: " + this.description +
                " Type: " + this.type +
                " URL: " + this.url +
                " Customer: " + this.customer.getCustomerName() +
                " Consultant: " + this.user + "\n" ;
    }
}
