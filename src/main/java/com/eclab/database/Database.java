package com.eclab.database;
import java.sql.*;

public class Database {
    public Database (){

    }
    String database = "";
    String username = "";
    String password = "";
    Connection con;

    public void connect() throws SQLException{
        con = DriverManager.getConnection(database,username,password);
    }

    public void disconnect() throws SQLException{
        con.close();
    }
}
