package version_1;

import utils.Database;

import java.io.*;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVReader;

public class ViewLoader
{
    private final int BATCH_SIZE = 500;//initial 500
    private Connection con = null;
    private PreparedStatement stmt = null;
    private int cnt = 0;
    private long start;
    private long end;
    private int counter = 1;

    private void loadData(ArrayList<Object> row, String[] type) throws SQLException
    {
        ArrayList<String> new_row = new ArrayList<>();
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
                        continue;
                    }
                    new_row.add(data);
                }
                else if (Objects.equals(type[i], "String"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add("");
                        continue;
                    }
                    data = data.replaceAll("/_reversed", "\\\\");
                    new_row.add(data);
                }
                else if (Objects.equals(type[i], "Date"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add(null);
                        continue;
                    }
                    new_row.add(data);
                }
                else if (Objects.equals(type[i], "Int"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add("-1");
                        continue;
                    }
                    new_row.add(data);
                }
                else if (Objects.equals(type[i], "Time"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add(null);
                        continue;
                    }
                    new_row.add(data);
                }
                else if (Objects.equals(type[i], "Real"))
                {
                    if (row.get(i) == null)
                    {
                        new_row.add(null);
                        continue;
                    }
                    new_row.add(data);
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
                    String regex = "'(\\d+)'|(\\d+)";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(data);
                    List<String> list = new ArrayList<>();
                    while (matcher.find())
                    {
                        String match = matcher.group(1);
                        if (match != null)
                        {
                            list.add(match);
                        }
                        else
                        {
                            list.add(matcher.group(2));
                        }
                    }
                    int len = list.size();
                    for (int k = 0; k < len; k += 2)
                    {
                        int index = 1;
                        String sub_data1 = list.get(k);
                        String sub_data2 = list.get(k + 1);
                        stmt.setString(index++, new_row.get(0));
                        stmt.setLong(index++, Long.parseLong(sub_data1));
                        stmt.setLong(index, Long.parseLong(sub_data2));
                        stmt.addBatch();
                        cnt++;
                        if (cnt % BATCH_SIZE == 0)
                        {
                            stmt.executeBatch();
                            stmt.clearBatch();
                            end = System.currentTimeMillis();
                            if (cnt % (BATCH_SIZE * 100) == 0)
                            {
                                end = System.currentTimeMillis();
                                Duration duration = Duration.ofSeconds((end - start) / 1000);
                                System.out.printf("已处理数：" + cnt / 10000 + " 万条，TIME：" +
                                        duration.toHours() + "h " + duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s，");
                                System.out.printf("导入进度：%.4f%%\n", counter / 7865.0 * 100);
                            }
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
                counter++;
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
                System.out.println("TIME : " + (end - start) / 1000 + "s");
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
