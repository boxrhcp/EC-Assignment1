package com.eclab.database.models;

public class Seat {
    private int id;
    private char row;
    private int col;
    private int userId;
    private int idFlight;

    public Seat(){

    }

    public Seat(int id, char row, int col, int userId, int idFlight) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.userId = userId;
        this.idFlight = idFlight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public char getRow() {
        return row;
    }

    public void setRow(char row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getIdFlight() {
        return idFlight;
    }

    public void setIdFlight(int idFlight) {
        this.idFlight = idFlight;
    }

}
