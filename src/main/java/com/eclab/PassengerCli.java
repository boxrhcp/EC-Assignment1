package com.eclab;

import com.eclab.database.Database;
import java.sql.SQLException;

public class PassengerCli {
    public static void main (String [] args){
        Database db = new Database();
        try {
            db.connect();
        }catch(SQLException e){
            System.out.println("Error connecting to db");
        }
        System.out.println("Success");
    }
}
