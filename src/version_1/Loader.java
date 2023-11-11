package version_1;

import utils.Database;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

import com.opencsv.CSVReader;

public class Loader
{
    private final int BATCH_SIZE = 5000;//initial 500
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

                if (Objects.equals(type[i], "Skip"))
                {
                    continue;
                }
                else if (Objects.equals(type[i], "Long"))
                {
                    if (row.get(i) == null)
                    {
                        stmt.setLong(index++, -1);
                        continue;
                    }
                    stmt.setLong(index++, Long.parseLong(data));
                }
                else if (Objects.equals(type[i], "String"))
                {
                    if (row.get(i) == null)
                    {
                        stmt.setString(index++, "");
                        continue;
                    }
                    data = data.replaceAll("/_reversed", "\\\\");
                    stmt.setString(index++, data);
                }
                else if (Objects.equals(type[i], "Date"))
                {
                    if (row.get(i) == null)
                    {
                        stmt.setDate(index++, null);
                        continue;
                    }
                    stmt.setDate(index++, new Date(2023 - 1900, Integer.parseInt(data.split("月")[0]) - 1, Integer.parseInt(data.split("月")[1].split("日")[0])));
                }
                else if (Objects.equals(type[i], "Int"))
                {
                    if (row.get(i) == null)
                    {
                        stmt.setInt(index++, -1);
                        continue;
                    }
                    stmt.setInt(index++, Integer.parseInt(data));
                }
                else if (Objects.equals(type[i], "Time"))
                {
                    if (row.get(i) == null)
                    {
                        stmt.setTimestamp(index++, null);
                        continue;
                    }
                    stmt.setTimestamp(index++, new Timestamp(
                            Integer.parseInt(data.split(" ")[0].split("-")[0]) - 1900,
                            Integer.parseInt(data.split(" ")[0].split("-")[1]) - 1,
                            Integer.parseInt(data.split(" ")[0].split("-")[2]),
                            Integer.parseInt(data.split(" ")[1].split(":")[0]),
                            Integer.parseInt(data.split(" ")[1].split(":")[1]),
                            Integer.parseInt(data.split(" ")[1].split(":")[2]), 0));
                }
                else if (Objects.equals(type[i], "Real"))
                {
                    if (row.get(i) == null)
                    {
                        stmt.setDouble(index++, -1);
                        continue;
                    }
                    stmt.setDouble(index++, Double.parseDouble(data));
                }
            }
            catch (Exception e)
            {
            }
        }
        stmt.addBatch();
    }

    public void write_data(String file_path, String[] queue, Database database, String sql, Boolean adder, Boolean pretreat, double num)
    {
        con = database.open();
        int cnt = 0;
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
            CSVReader reader;
            if (pretreat)
            {
                reader = new CSVReader(fr, '\7');//用于数据处理过的数据
            }
            else
            {
                reader = new CSVReader(fr);
            }
            String[] lineData = reader.readNext();
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
                if (adder)
                {
                    row.add(cnt);
                }
                loadData(row, queue);
                if (cnt % BATCH_SIZE == 0)
                {
                    if(num == 0)
                    {
                        System.out.println("当前进度：" + cnt + " 条");
                    }
                    else
                    {
                        System.out.printf("导入进度：%.3f%%\n", cnt / num * 100);
                    }
                    stmt.executeBatch();
                    stmt.clearBatch();
                }
                cnt++;
            }
            stmt.executeBatch();
            stmt.clearBatch();
            try
            {
                con.commit();//提交事务，运行后才导入数据库
                stmt.close();
                database.close(stmt);
                long end = System.currentTimeMillis();//结束时间
                System.out.println(cnt + " records successfully loaded");
                System.out.println("TIME : " + (end - start) / 1000 + "s");
                System.out.println("Loading speed : " + (long) cnt / ((end - start) / 1000) + " records/s");
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
