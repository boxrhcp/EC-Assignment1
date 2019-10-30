package com.eclab;

import com.eclab.database.Database;
import com.eclab.database.models.Flight;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class PassengerCli {
    public static void main(String[] args) {
        Database db = new Database();
        startConnection(db);
        System.out.println("Welcome to LeoPard Airlines client. Choose one of the following " +
                "listed flights or create a new one:");
        chooseFlight(db);

        stopConnection(db);
        System.exit(0);
    }

    public static void startConnection(Database db) {
        try {
            db.connect();
        } catch (SQLException e) {
            System.out.println("Error connecting to db");
        }
    }

    public static void stopConnection(Database db) {
        try {
            db.disconnect();
        } catch (SQLException e) {
            System.out.println("Error connecting to db");
        }
    }

    public static Flight chooseFlight(Database db) {
        ResultSet rs = null;
        Flight option = null;
        ArrayList<Flight> flights = new ArrayList<>();
        int pos = 0;
        try {
            rs = db.execStatement("SELECT * FROM flights");
            while (rs.next()) {
                Flight aux = new Flight(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getString(4));
                flights.add(aux);
                System.out.println(pos + ") Flight number " + aux.getFlightId() + " from " + aux.getFrom() + " to " +
                        aux.getTo() + ".");
                pos++;
            }
        } catch (SQLException e) {
            System.err.println("Error reading the available flights");
            System.exit(-1);
        }
        Scanner in = new Scanner(System.in);
        int num = in.nextInt();

        if (num >= pos || num < 0) {
            System.err.println("Error the option chosen is not in the list given.");
        } else {
            option = flights.get(num);
        }

        return option;
    }
}
