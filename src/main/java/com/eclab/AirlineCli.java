package com.eclab;

import com.eclab.database.Database;
import com.eclab.database.models.Flight;
import com.eclab.database.models.Seat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * To run this client write in terminal "mvn exec:java@airline"
 */
public class AirlineCli {
    private static int rows = 6; //rows in a flight
    private static int cols = 30; //cols in a flight
    private static int diff = 65; // int difference of row chars (A=65)
    private static Database db = new Database();
    private static Scanner in = new Scanner(System.in);
    private static boolean enable = true;

    public static void main(String[] args) {
        startConnection();
        System.out.println("Welcome to LeoPard Airlines client.");
        while (enable) {
            try {
                Flight flight = chooseFlight();
                if (enable) {
                    Seat seat = chooseFlightSeat(flight);
                    if (seat.getUserId() != 0) {
                        printUser(seat);
                    } else {
                        System.out.println("The seat is empty.");
                    }
                    System.out.println("End of Airline execution, do you want to make another action (y) or to exit (n)? (y/n)");
                    char answer = in.nextLine().toLowerCase().charAt(0);
                    if (answer == 'n') enable = false;
                }
            } catch (Exception e) {
                System.err.println("Error in the Airline process. ");
                //e.printStackTrace();
            }
        }
        stopConnection();
        System.exit(0);
    }

    private static void startConnection() {
        try {
            db.connect();
        } catch (SQLException e) {
            System.out.println("Error connecting to db");
            System.exit(-1);
        }
    }

    private static void stopConnection() {
        try {
            db.disconnect();
        } catch (SQLException e) {
            System.out.println("Error connecting to db");
            System.exit(-1);
        }
    }

    /**
     * It will print the available flights and let the user choose a flight. If they choose the option to create
     * a new flight it will call the method createFlight
     * @return the chosen or created flight
     * @throws Exception when there is an SQL or when the input is not the expected one
     */
    private static Flight chooseFlight() throws Exception {
        System.out.println("Choose one of the following listed flights or create a new one:");
        ResultSet rs;
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
        System.out.println(pos + ") Create a new flight");
        int num = in.nextInt();
        in.nextLine(); //workaround to not jump next reading
        if (num > pos || num < 0) {
            System.err.println("Error the option chosen is not in the given list.");
        } else if (num == pos) {
            try {
                option = createFlight();
                System.out.println("End of Airline execution, do you want to make another action (y) or to exit (n)? (y/n)");
                char answer = in.nextLine().toLowerCase().charAt(0);
                if (answer == 'n') enable = false;
            } catch (Exception e) {
                System.err.println("Error creating a new flight.");
                throw e;
            }
        } else {
            option = flights.get(num);
        }

        return option;
    }

    /**
     * creates a new flight after receiving user input
     * @return the created flight
     * @throws Exception when there is an SQL error
     */
    private static Flight createFlight() throws Exception {
        Flight created;
        ResultSet rs;
        int id;
        System.out.println("Creating a new flight. Introduce the flight id: ");
        String flightId = in.nextLine();
        System.out.println("Introduce the departure location: ");
        String from = in.nextLine();
        System.out.println(("Introduce the destination location: "));
        String to = in.nextLine();
        try {
            db.execUpdate("INSERT INTO flights VALUES (NULL,'" + flightId + "','" +
                    from + "','" + to + "')");

            rs = db.execStatement("SELECT LAST_INSERT_ID()");
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                System.err.println("Error retrieving the key of the last inserted row");
                throw new Exception();
            }
            for (char i = (char) diff; i < diff + rows; i++) {
                for (int j = 1; j <= cols; j++) {
                    db.execUpdate("INSERT INTO seats VALUES (NULL,'" + i + "'," + j + ",0," + id + ")");
                }
            }
            db.commit();
            created = new Flight(id, flightId, from, to);
        } catch (SQLException e) {
            System.err.println("Error when creating a new flight.");
            db.rollback();
            throw e;
        }
        return created;
    }

    /**
     * It will print the seats available on the flight and let the user choose any seat.
     * @param flight chosen in chooseFlight
     * @return the seat chosen
     * @throws Exception when there is an SQL error or when the input is not the expected one
     */
    private static Seat chooseFlightSeat(Flight flight) throws Exception {
        ResultSet rs;
        Scanner in = new Scanner(System.in);
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
        System.out.println("Select a seat from the seat map. The seats with a -- are already booked.");
        printSeats(seats);
        System.out.println("First choose the seat row from 1 to 30: ");
        int col = in.nextInt() - 1;
        in.nextLine();
        System.out.println("Now choose the seat from A to F: ");
        int row = in.nextLine().toUpperCase().charAt(0) - diff;
        if ((col < 0 || col > (cols - 1)) || (row < 0 || row > (rows - 1))) {
            System.err.println("The seat chosen is out of bounds");
            throw new Exception();
        }
        return seats[row][col];
    }

    /**
     * It prints the user that booked the seat chosen.
     * @param seat the seat chosen in chooseFlightSeat
     * @throws Exception when there is an SQL error
     */
    private static void printUser(Seat seat) throws Exception {
        ResultSet rs;
        try {
            rs = db.execStatement("SELECT * FROM passengers WHERE id = " + seat.getUserId());
            if (rs.next()) {
                System.out.println("The seat " + (seat.getCol() + 1) + seat.getRow() + " is booked by the passenger " +
                        rs.getString(2) + " " + rs.getString(3));
            }
        } catch (SQLException e) {
            System.err.println("Error reading the available flights");
            throw e;
        }
    }

    /**
     * method created to print the flight seats retrieved from the db
     * @param seats the seats of the flight to be printed
     */
    private static void printSeats(Seat[][] seats) {
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
