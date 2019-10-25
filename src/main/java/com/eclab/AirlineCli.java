package com.eclab;

import com.eclab.database.Database;
import java.sql.SQLException;

public class AirlineCli {
    public static void main (String [] args){
        Database db = new Database();
        startConnection(db);
        System.out.println("Welcome to LeoPard Airlines client. Choose one of the following listed flights or create a new one:");

        stopConnection(db);
    }

    public static void startConnection(Database db){
        try {
            db.connect();
        }catch(SQLException e){
            System.out.println("Error connecting to db");
        }
        System.out.println("Success");
    }

    public static void stopConnection(Database db){
        try {
            db.disconnect();
        }catch(SQLException e){
            System.out.println("Error connecting to db");
        }
        System.out.println("Success");
    }

}
