package com.eclab.database;

import java.sql.*;

public class Database {
    public Database() {

    }

    private String database = "";
    private String username = "";
    private String password = "";
    private Connection con;

    public void connect() throws SQLException {
        con = DriverManager.getConnection(database, username, password);
        con.setAutoCommit(false);
    }

    public void disconnect() throws SQLException {
        con.close();
    }

    public ResultSet execStatement(String query) throws SQLException {
        Statement selectStmt = con.createStatement();
        return selectStmt
                .executeQuery(query);
    }

    public void execUpdate(String query) throws SQLException {
        Statement selectStmt = con.createStatement();
        selectStmt.executeUpdate(query);
    }

    public void commit() throws SQLException {
        con.commit();
    }

    public void rollback() throws SQLException {
        con.rollback();
    }

}
