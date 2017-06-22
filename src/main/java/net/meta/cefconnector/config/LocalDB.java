/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.meta.cefconnector.config;

import net.meta.cefconnector.config.CEFConnectorConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
        
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.text.ParseException;
import org.sqlite.JDBC;
import java.util.TimeZone;
import java.text.DateFormat;
import java.util.Date;
/**
 *
 * @author krassouli
 */
public class LocalDB {
   
    private static final Logger log = LogManager.getLogger(LocalDB.class);

    private static String sTempDb = "cefconnector.db";
    private static String sJdbc = "jdbc:sqlite";
    private static String sDbUrl = sJdbc + ":" + sTempDb;
        // which will produce a legitimate Url for SqlLite JDBC :
        // jdbc:sqlite:hello.db
    private static int iTimeout = 30;
    
    private static String sMakeTable_lastoffset = "CREATE TABLE IF NOT EXISTS lastoffset (id INTEGER PRIMARY KEY, offset VARCHAR(200))";
    private static String sMakeInsert_lastoffset = "INSERT INTO lastoffset VALUES(1,'%s')";
    private static String sMakeUpdate_lastoffset = "UPDATE lastoffset SET offset='%s' where id=1";
    private static String sMakeSelect_lastoffset = "SELECT offset from lastoffset";
    
    private static String sMakeTable_lastpulldate = "CREATE TABLE IF NOT EXISTS lastpulldate (id INTEGER PRIMARY KEY, pulldate VARCHAR(20))";
    private static String sMakeInsert_lastpulldate = "INSERT INTO lastpulldate VALUES(1,'%s')";
    private static String sMakeUpdate_lastpulldate = "UPDATE lastpulldate SET pulldate='%s' where id=1";
    private static String sMakeSelect_lastpulldate = "SELECT pulldate from lastpulldate";
    private static String rTimeSpan = "?startTime=%s&endTime=%s";
    
    
    public static Connection connectDB(){
        // create a database connection
        Connection conn = null;
        try{ 
            conn = DriverManager.getConnection(sDbUrl);
            return conn;
        }
        catch (Exception ignore) { 
            return conn;
        }
    }
    public static String getLastOffset(Connection conn) {   
        
        String offset = "";
        
        try {
            Class.forName("org.sqlite.JDBC");
            Statement stmt = conn.createStatement();
            try {
                stmt.setQueryTimeout(iTimeout);
                stmt.execute(sMakeTable_lastoffset); 
                
                ResultSet rs = stmt.executeQuery( sMakeSelect_lastoffset  );
                
                //check if an entry exists otherwise use default time
                if ((rs.next())) {                            //if rs.next() returns false
                    offset = rs.getString("offset");
                } 
                
                rs.close(); 

            }
            catch (Exception ignore) {}
   
            try { stmt.close(); } catch (Exception ignore) {}
        }
        catch (Exception ignore) {}
        
        return offset;
    }  
    public static void setLastOffset(Connection conn, String offset) {   
         
        try {
            Class.forName("org.sqlite.JDBC");
            Statement stmt = conn.createStatement();
            try {
                stmt.setQueryTimeout(iTimeout);
                stmt.execute(sMakeTable_lastoffset);                
                String s = "";
                ResultSet rs = stmt.executeQuery( sMakeSelect_lastoffset  );
                               
                //check if an entry exists otherwise use default time
                if ((rs.next())) {                            //if rs.next() returns false
                    s = String.format(sMakeUpdate_lastoffset , offset);
                    stmt.executeUpdate(s); 
                } 
                else{
                    s = String.format(sMakeInsert_lastoffset, offset);
                    stmt.executeUpdate(s);
                }
                rs.close(); 

            }
            catch (Exception ignore) {}
   
            try { stmt.close(); } catch (Exception ignore) {}
        }
        catch (Exception ignore) {}
        
    } 
    public static String getLastPullTime(Connection conn) {   
        
        TimeZone tz = TimeZone.getTimeZone(CEFConnectorConfiguration.getTimezone());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        
        String startTime = nowAsISO; // this will either be default to 1,1,1970 or currenttime
        String endTime = nowAsISO;
        
        try {
            Class.forName("org.sqlite.JDBC");
            Statement stmt = conn.createStatement();
            try {
                stmt.setQueryTimeout(iTimeout);
                stmt.execute(sMakeTable_lastpulldate);                
                String s = "";
                ResultSet rs = stmt.executeQuery( sMakeSelect_lastpulldate );
                 
                //check if an entry exists otherwise use default time
                if (rs.next()) {                            //if rs.next() returns false
                    startTime = rs.getString("pulldate");
                    
                    s = String.format(sMakeUpdate_lastpulldate, endTime);
                    stmt.executeUpdate(s); 
                } 
                else{
                    s = String.format(sMakeInsert_lastpulldate, endTime);
                    stmt.executeUpdate(s);
                }
                rs.close(); 
                
                //update lastpulldate with time now
                s = String.format(sMakeUpdate_lastpulldate, endTime);
                stmt.executeUpdate(s);
            }
            catch (Exception ignore) {}
   
            try { stmt.close(); } catch (Exception ignore) {}
        }
        catch (Exception ignore) {}
        
        return String.format(rTimeSpan,startTime,endTime);
    }
}


