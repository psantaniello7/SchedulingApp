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
public class User {
    
    private int userId;
    private String userName;
    private String password;



    public User(int userId, String userName, String password){
    this.userId = userId;
    this.userName = userName;
    this.password = password;


    }

    public User(){
    
    }

    public int getUserId(){
    return userId;
    }

    public void setUserId(int userId){
    this.userId = userId;
    }

    public String getUserName(){
    return userName;
    }

    public void setUserName(String userName){
    this.userName = userName;
    }

    public String getPassword(){
    return password;
    }

    public void setPassword(String password){
    this.password = password;
    }

}
