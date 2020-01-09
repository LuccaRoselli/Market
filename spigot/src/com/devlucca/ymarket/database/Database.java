package com.devlucca.ymarket.database;

import java.sql.*;

public interface Database
{
    boolean open();
    
    boolean close();
    
    boolean connection();
    
    ResultSet query(final String p0);
    
    boolean execute(final String p0);
    
    Connection getConnection();
    
    Statement getStatement();
    
    String getHostname();
    
    String getDatabase();
    
    String getUsername();
    
    String getPassword();
    
    String getType();
    
    int getPort();
}
