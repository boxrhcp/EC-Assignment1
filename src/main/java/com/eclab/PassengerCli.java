package com.eclab;

import com.eclab.database.Database;
import com.eclab.database.models.Flight;
import com.eclab.database.models.Passenger;
import com.eclab.database.models.Seat;

import javax.xml.transform.Result;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class PassengerCli {
    private static int rows = 6; //rows in a flight
    private static int cols = 30; //cols in a flight
    private static int diff = 65; // int difference of row chars (A=65)
    private static Database db = new Database();
    private static Scanner in = new Scanner(System.in);
    private static boolean enable = true;
    private static Passenger user;

    public static void main(String[] args) {
        startConnection();
        System.out.println("Welcome to LeoPard Airlines Passenger client.");
        while (enable) {
            try {
                choosePassenger();
                if (enable) {
                    Flight flight = chooseFlight(); //checkear si devuelve el id bien
                    ArrayList<Seat> chosen = chooseFlightSeat(flight);
                    bookSeats(chosen);
                    System.out.println("End of Passenger execution, do you want to exit or to make another action? (y/n)");
                    char answer = in.nextLine().toLowerCase().charAt(0);
                    if (answer == 'n') enable = false;
                }
            } catch (Exception e) {
                System.err.println("Error in the Passenger process. ");
                //e.printStackTrace();
            }
        }
        stopConnection();
        System.exit(0);
    }

    public static void startConnection() {
        try {
            db.connect();
        } catch (SQLException e) {
            System.out.println("Error connecting to db");
            System.exit(-1);
        }
    }

    public static void stopConnection() {
        try {
            db.disconnect();
        } catch (SQLException e) {
            System.out.println("Error connecting to db");
            System.exit(-1);
        }
    }

    public static void choosePassenger() throws Exception {
        System.out.println("Choose one of the following listed passengers or create a new one: ");
        ResultSet rs = null;
        ArrayList<Passenger> passengers = new ArrayList<>();
        int pos = 0;
        try {
            rs = db.execStatement("SELECT * FROM passengers");
            while (rs.next()) {
                Passenger aux = new Passenger(rs.getInt(1), rs.getString(2),
                        rs.getString(3));
                passengers.add(aux);
                System.out.println(pos + ") " + aux.getName() + " " + aux.getSurname());
                pos++;
            }
        } catch (SQLException e) {
            System.err.println("Error reading the available passengers");
            throw e;
        }
        System.out.println(pos + ") Create a new Passenger");
        int num = in.nextInt();
        in.nextLine(); //workaround to not jump next reading
        if (num > pos || num < 0) {
            System.err.println("Error the option chosen is not in the list given.");
        } else if (num == pos) {
            try {
                user = createPassenger();
                System.out.println("End of Passenger execution, do you want to exit or to make another action? (y/n)");
                char answer = in.nextLine().toLowerCase().charAt(0);
                if (answer == 'n') enable = false;
            } catch (Exception e) {
                System.err.println("Error creating a new passenger.");
                throw e;
            }
        } else {
            user = passengers.get(num);
        }

    }

    private static Passenger createPassenger() throws Exception {
        Passenger created = null;
        int id;
        ResultSet rs = null;
        System.out.println("Creating a new passenger. Introduce the name: ");
        String name = in.nextLine();
        System.out.println("Introduce the surname: ");
        String surname = in.nextLine();

        try {
            db.execUpdate("INSERT INTO passengers VALUES (NULL,'" + name + "','" +
                    surname + "')");
            rs = db.execStatement("SELECT LAST_INSERT_ID()");
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                System.err.println("Error retrieving the key of the last inserted row");
                throw new Exception();
            }
            db.commit();
            created = new Passenger(id, name, surname);
        } catch (Exception e) {
            System.err.println("Error when creating a new passenger.");
            db.rollback();
            throw e;
        }
        return created;
    }

    public static Flight chooseFlight() throws Exception {
        System.out.println("Choose one of the following listed flights:");
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
            throw e;
        }
        int num = in.nextInt();
        in.nextLine(); //workaround to not jump next reading
        if (num >= pos || num < 0) {
            System.err.println("Error the option chosen is not in the list given.");
        } else {
            option = flights.get(num);
        }
        return option;
    }

    public static ArrayList<Seat> chooseFlightSeat(Flight flight) throws Exception {
        ResultSet rs = null;
        ArrayList<Seat> chosen = new ArrayList<>();
        Seat[][] seats = new Seat[rows][cols];
        for (int i = 0; i < seats.length; i++)
            for (int j = 0; j < seats[0].length; j++)
                seats[i][j] = new Seat();

        try {
            rs = db.execStatement("SELECT * FROM seats WHERE idFlight = " + flight.getId() + " ORDER BY row, col");
            while (rs.next()) {
                char row = rs.getString(2).charAt(0);
                int col = rs.getInt(3) - 1;

                seats[row - diff][col].setId(rs.getInt(1));
                seats[row - diff][col].setRow(row);
                seats[row - diff][col].setCol(col);
                seats[row - diff][col].setUserId(rs.getInt(4));
                seats[row - diff][col].setIdFlight(rs.getInt(5));
            }
            flight.setSeats(seats);
        } catch (Exception e) {
            System.err.println("Error reading the available flights.");
            throw e;
        }
        System.out.println("Select seats from the seat map. Please separate the letter and number with : and each seat" +
                " with , . E.g.: '30:A,24:E,2:C' .  The seats with a -- are already booked.");
        printSeats(seats);
        String input = in.nextLine();
        try {
            for (String i : input.split(",")) {
                String[] j = i.split(":");
                int col = Integer.parseInt(j[0]) - 1;
                int row = j[1].toUpperCase().charAt(0) - diff;
                if ((col < 0 || col > (cols-1)) || (row < 0 || row > (rows-1))) {
                    System.err.println("The seat chosen is out of bounds");
                    throw new Exception();
                } else {
                    chosen.add(seats[row][col]);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in the input received. Check if the seats are inside of bounds and check if" +
                    " there was a typo. Format so be like the following example: '30:A,24:E,2:C'.");
            throw e;
        }
        return chosen;
    }

    public static void bookSeats(ArrayList<Seat> chosen) throws Exception {
        String ids = "";
        String positions = "";
        ResultSet rs = null;
        for (Seat s : chosen){
            ids += s.getId() + ",";
            positions += (s.getCol()+1) + "" + s.getRow() + " ";
        }
        ids = ids.substring(0, ids.length() - 1);
        try {
            rs = db.execStatement("SELECT * FROM seats WHERE id in (" + ids + ") FOR UPDATE");
            while (rs.next()) {
                if (rs.getInt(4) != 0) {
                    throw new Exception("The seat " + rs.getInt(3) + rs.getString(2)
                            + " is already booked");
                }
            }
            System.out.print("You are going to book the seats: " + positions + ". Confirm the booking (y/n).");
            char answer = in.nextLine().toLowerCase().charAt(0);
            if (answer == 'y'){
                db.execUpdate("UPDATE seats SET userId = " + user.getId() + " WHERE id in (" + ids + ")");
                db.commit();
            }else if(answer == 'n'){
                System.out.println("Cancelling seats booking.");
                db.rollback();
            }else{
                throw new Exception("Error in confirmation, write only y or n.");
            }
        } catch (Exception e) {
            System.err.println("Error when booking the flight seats, the seats may have been already booked: ");
            System.err.println(e.getMessage() + ". Try again please.");
            db.rollback();
            throw e;
        }
    }


    public static void printUser(Seat seat) throws Exception {
        ResultSet rs = null;
        int pos = 0;
        try {
            rs = db.execStatement("SELECT * FROM passengers WHERE id = " + seat.getUserId());
            if (rs.next()) {
                System.out.println("The seat " + seat.getCol() + seat.getRow() + " is booked by the passenger " +
                        rs.getString(2) + " " + rs.getString(3));
            }
        } catch (SQLException e) {
            System.err.println("Error reading the available flights");
            throw e;
        }
    }

    public static void printSeats(Seat[][] seats) {
        for (int i = 0; i < rows; i++) {
            System.out.print((char) (i + diff) + "  ");
            for (int j = 0; j < cols; j++) {
                if (seats[i][j].getUserId() == 0) {
                    System.out.print("[  ] ");
                } else {
                    System.out.print("[--] ");
                }
            }
            System.out.println();
        }
        System.out.print("   ");
        for (int i = 1; i <= cols; i++) {
            if (i < 10) {
                System.out.print(" " + i + "   ");
            } else {
                System.out.print(" " + i + "  ");
            }
        }
        System.out.println();
    }
}
