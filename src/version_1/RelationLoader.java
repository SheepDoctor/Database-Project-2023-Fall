package version_1;

import utils.Database;

import java.io.*;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

import com.opencsv.CSVReader;

public class RelationLoader
{
    private final int BATCH_SIZE = 500;//initial 500
    private Connection con = null;
    private PreparedStatement stmt = null;
    private int cnt = 0;
    private long start;
    private long end;

    private void loadData(ArrayList<Object> row, String[] type) throws SQLException
    {
        ArrayList<String> new_row = new ArrayList<>();
        ArrayList<String> new_type = new ArrayList<>();
        for (int i = 0; i < row.size(); i++)
        {
            if (!(Objects.equals(type[i], "Skip") && Objects.equals(type[i], "List")))
            {
                String data = row.get(i) != null ? row.get(i).toString() : null;
                if (Objects.equals(type[i], "Long"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add(null);
                        new_type.add("Long");
                        continue;
                    }
                    new_row.add(data);
                    new_type.add("Long");
                }
                else if (Objects.equals(type[i], "String"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add("");
                        new_type.add("String");
                        continue;
                    }
                    data = data.replaceAll("/_reversed", "\\\\");
                    new_row.add(data);
                    new_type.add("String");
                }
                else if (Objects.equals(type[i], "Date"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add(null);
                        new_type.add("Date");
                        continue;
                    }
                    new_row.add(data);
                    new_type.add("Date");
                }
                else if (Objects.equals(type[i], "Int"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add("-1");
                        new_type.add("Int");
                        continue;
                    }
                    new_row.add(data);
                    new_type.add("Int");
                }
                else if (Objects.equals(type[i], "Time"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add(null);
                        new_type.add("Time");
                        continue;
                    }
                    new_row.add(data);
                    new_type.add("Time");
                }
                else if (Objects.equals(type[i], "Real"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add(null);
                        new_type.add("Time");
                        continue;
                    }
                    new_row.add(data);
                    new_type.add("Time");
                }
            }
        }
        for (int i = 0; i < row.size(); i++)
        {
            try
            {
                if (Objects.equals(type[i], "List"))
                {
                    String data = row.get(i) != null ? row.get(i).toString() : null;
                    data = data.replace("['", "");
                    data = data.replace("']", "");
                    String[] list = data.split("\', \'");
                    for (int k = 0; k < list.length; k++)
                    {
                        int index = 1;
                        String sub_data = list[k];
                        for (int j = 0; j < new_row.size(); j++)
                        {
                            if (Objects.equals(new_type.get(j), "Long"))
                            {
                                if (new_row.get(j) == null)
                                {
                                    stmt.setLong(index++, -1);
                                    continue;
                                }
                                stmt.setLong(index++, Long.parseLong(new_row.get(j)));
                            }
                            else if (Objects.equals(new_type.get(j), "String"))
                            {
                                if (new_row.get(j) == null)
                                {
                                    stmt.setString(index++, "");
                                    continue;
                                }
                                data = data.replaceAll("/_reversed", "\\\\");
                                stmt.setString(index++, new_row.get(j));
                            }
                            else if (Objects.equals(new_type.get(j), "Date"))
                            {
                                if (new_row.get(j) == null)
                                {
                                    stmt.setDate(index++, null);
                                    continue;
                                }
                                stmt.setDate(index++, new Date(2023 - 1900, Integer.parseInt(sub_data.split("月")[0]) - 1, Integer.parseInt(new_row.get(j).split("月")[1].split("日")[0])));
                            }
                            else if (Objects.equals(new_type.get(j), "Int"))
                            {
                                if (new_row.get(j) == null)
                                {
                                    stmt.setInt(index++, -1);
                                    continue;
                                }
                                stmt.setInt(index++, Integer.parseInt(new_row.get(j)));
                            }
                            else if (Objects.equals(new_type.get(j), "Time"))
                            {
                                if (new_row.get(j) == null)
                                {
                                    stmt.setTimestamp(index++, null);
                                    continue;
                                }
                                stmt.setTimestamp(index++, new Timestamp(
                                        Integer.parseInt(new_row.get(j).split(" ")[0].split("-")[0]),
                                        Integer.parseInt(new_row.get(j).split(" ")[0].split("-")[1]),
                                        Integer.parseInt(new_row.get(j).split(" ")[0].split("-")[2]),
                                        Integer.parseInt(new_row.get(j).split(" ")[1].split(":")[0]),
                                        Integer.parseInt(new_row.get(j).split(" ")[1].split(":")[1]),
                                        Integer.parseInt(new_row.get(j).split(" ")[1].split(":")[2]), 0));
                            }
                            else if (Objects.equals(new_type.get(j), "Real"))
                            {
                                if (new_row.get(j) == null)
                                {
                                    stmt.setDouble(index++, -1);
                                    continue;
                                }
                                stmt.setDouble(index++, Double.parseDouble(new_row.get(j)));
                            }
                        }
                        stmt.setLong(index++, Long.parseLong(sub_data));
                        cnt++;
                        if (cnt % BATCH_SIZE == 0)
                        {
                            if (cnt % (BATCH_SIZE * 75) == 0)
                            {
                                end = System.currentTimeMillis();
                                Duration duration = Duration.ofSeconds((long) (end - start) / 1000);
                                long hours = duration.toHours(); // 获取小时数
                                long minutes = duration.toMinutesPart(); // 获取分钟数
                                long seconds = duration.toSecondsPart(); // 获取秒数
                                System.out.println("当前进度：" + cnt + " 条，TIME：" +
                                        hours + "h " + minutes + "m " + seconds + "s");
                            }
                            stmt.executeBatch();
                            stmt.clearBatch();
                        }
                    }
                    break;
                }
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
        stmt.addBatch();
    }

    public void write_data(String file_path, String[] queue, Database database, String sql, Boolean adder, Boolean pretreat)
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
            start = System.currentTimeMillis();//开始时间
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
                //if (cnt % BATCH_SIZE == 0)
                //{
                //    if (cnt % (BATCH_SIZE * 50) == 0)
                //    {
                //        System.out.println("当前进度：" + cnt + " 条");
                //    }
                //    stmt.executeBatch();
                //    stmt.clearBatch();
                //}
            }
            stmt.executeBatch();
            stmt.clearBatch();
            try
            {
                con.commit();//提交事务，运行后才导入数据库
                stmt.close();
                database.close(stmt);
                end = System.currentTimeMillis();//结束时间
                System.out.println(cnt + " records successfully loaded");
                System.out.println("TIME : " + (long) (end - start) / 1000 + "s");
                System.out.println("Loading speed : " + (long) cnt / (end - start) + " records/s");
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
