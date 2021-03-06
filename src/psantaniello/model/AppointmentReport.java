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

public class AppointmentReport {
    
    private String month;
    private String total;
    private String type;    


    public AppointmentReport(String month, String type, String total) {
        this.month = month;
        this.type = type;
        this.total = total;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
    
    
    @Override
    public String toString() {
    return "Month: " + this.month +
            " Type: " + this.type +
            " Total: " + this.total + ".\n" ;
    }
    
}
