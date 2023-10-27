package utils;

import java.sql.*;
import java.util.Properties;

public class Database
{
    private Connection con = null;
    private boolean verbose = false;
    private String host;
    private String dbname;
    private String user;
    private String pwd;

    public Database(Properties prop)
    {
        host = prop.getProperty("host");
        dbname = prop.getProperty("database");
        user = prop.getProperty("user");
        pwd = prop.getProperty("password");
    }

    public Connection open()
    {
        try
        {
            Class.forName("org.postgresql.Driver");
        }
        catch (Exception e)
        {
            System.err.println("Cannot find the Postgres driver. Check CLASSPATH.");
            System.exit(1);
        }
        String url = "jdbc:postgresql://" + host + "/" + dbname;
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        try
        {
            con = DriverManager.getConnection(url, props);
            if (verbose)
            {
                System.out.println("Successfully connected to the database " + dbname + " as " + user);
            }
            con.setAutoCommit(false);
        }
        catch (SQLException e)
        {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return con;
    }

    public void close(Statement stmt)
    {
        if (con != null)
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                con.close();
                con = null;
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
    }

}
