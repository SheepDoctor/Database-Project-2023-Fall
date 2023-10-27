package version_1;

import utils.Database;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.sql.Date;
import java.sql.Timestamp;

import com.opencsv.CSVReader;

public class Loader
{
    private final int BATCH_SIZE = 500;//initial 500
    private Connection con = null;
    private PreparedStatement stmt = null;

    private void loadData(ArrayList<Object> row, String[] type) throws SQLException
    {
        int index = 1;
        for (int i = 0; i < row.size(); i++)
        {
            try
            {
                String data;
                if (row.get(i) != null)
                {
                    data = row.get(i).toString();
                }
                else
                {
                    data = null;
                }
                if (type[i] == "Skip")
                {
                    continue;
                }
                else if (type[i] == "Long")
                {
                    if (row.get(i) == null)
                    {
                        stmt.setLong(index++, -1);
                        continue;
                    }
                    stmt.setLong(index++, Long.parseLong(data));
                }
                else if (type[i] == "String")
                {
                    if (row.get(i) == null)
                    {
                        stmt.setString(index++, null);
                        continue;
                    }
                    stmt.setString(index++, data);
                }
                else if (type[i] == "Date")
                {
                    if (row.get(i) == null)
                    {
                        stmt.setDate(index++, null);
                        continue;
                    }
                    stmt.setDate(index++, new Date(2023 - 1900, Integer.parseInt(data.split("月")[0]) - 1, Integer.parseInt(data.split("月")[1].split("日")[0])));
                }
                else if (type[i] == "Int")
                {
                    if (row.get(i) == null)
                    {
                        stmt.setInt(index++, -1);
                        continue;
                    }
                    stmt.setInt(index++, Integer.parseInt(data));
                }
                else if (type[i] == "Time")
                {
                    if (row.get(i) == null)
                    {
                        stmt.setTimestamp(index++, null);
                        continue;
                    }
                    stmt.setTimestamp(index++, new Timestamp(
                            Integer.parseInt(data.split(" ")[0].split("-")[0]),
                            Integer.parseInt(data.split(" ")[0].split("-")[1]),
                            Integer.parseInt(data.split(" ")[0].split("-")[2]),
                            Integer.parseInt(data.split(" ")[1].split(":")[0]),
                            Integer.parseInt(data.split(" ")[1].split(":")[1]),
                            Integer.parseInt(data.split(" ")[1].split(":")[2]),0));
                }
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
        stmt.addBatch();
    }


    public void write_data(String file_path, String[] queue, Database database, String sql)
    {
        con = database.open();
        try
        {
            stmt = con.prepareStatement(sql);
        }
        catch (SQLException e)
        {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            database.close(stmt);
            System.exit(1);
        }
        try
        {
            long start = System.currentTimeMillis();//开始时间
            FileReader fr = new FileReader(file_path);
            CSVReader reader = new CSVReader(fr);
            String[] lineData = reader.readNext();
            int cnt = 1;
            while ((lineData = reader.readNext()) != null)
            {
                ArrayList<Object> row = new ArrayList<>();
                for (Object data : lineData)
                {
                    if (data == "" || data == null)
                    {
                        data = null;
                    }
                    row.add(data);
                }
                loadData(row, queue);
                if (cnt % BATCH_SIZE == 0)
                {
                    stmt.executeBatch();
                    stmt.clearBatch();
                    System.out.println("进度：" + cnt / BATCH_SIZE + "%");
                }
                cnt++;
            }

            try
            {
                con.commit();//提交事务，运行后才导入数据库
                stmt.close();
                database.close(stmt);
                long end = System.currentTimeMillis();//结束时间
                System.out.println(cnt + " records successfully loaded");
                System.out.println("Loading speed : " + (cnt * 1000) / (end - start) + " records/s");
            }
            catch (Exception e)
            {
                System.err.println("Fatal error: " + e.getMessage());
                try
                {
                    con.rollback();
                    stmt.close();
                }
                catch (Exception e2)
                {
                    System.out.println(e2);
                }
                database.close(stmt);
                System.exit(1);
            }
        }
        catch (SQLException e)
        {
            System.err.println("SQL error: " + e.getMessage());
            try
            {
                con.rollback();
                stmt.close();
            }
            catch (Exception e2)
            {
            }
            database.close(stmt);
            System.exit(1);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("FileNotFound");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        database.close(stmt);
    }
}
