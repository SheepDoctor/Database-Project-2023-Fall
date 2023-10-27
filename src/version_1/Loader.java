package version_1;

import utils.Database;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.sql.Date;

public class Loader
{
    private static final int BATCH_SIZE = 500;//initial 500
    private static URL propertyURL = Loader.class.getResource("/loader.cnf");
    private static Connection con = null;
    private static PreparedStatement stmt = null;
    private static boolean verbose = false;

    private static void loadData(long Mid, String name, String sex, java.sql.Date birthday, int level, String sign, String identity) throws SQLException
    {
        if (con != null)
        {
            stmt.setLong(1, Mid);
            stmt.setString(2, name);
            stmt.setString(3, sex);
            stmt.setDate(4, birthday);
            stmt.setInt(5, level);
            stmt.setString(6, sign);
            stmt.setString(7, identity);
            stmt.addBatch();
        }
    }


    public static void main(String[] args)
    {

        Properties def_prop = new Properties();
        def_prop.put("host", "localhost");
        def_prop.put("user", "postgres");
        def_prop.put("password", "123456");
        def_prop.put("database", "Project");
        Properties prop = new Properties(def_prop);

        Database database = new Database(prop);
        // Ignore
        //System.err.println("No configuration file (loader.cnf) found");
        con = database.open();
        try
        {
            stmt = con.prepareStatement("insert into users(Mid,Name,Sex,Birthday,Level,Sign,identity)" + " values(?,?,?,?,?,?,?)");
        }
        catch (SQLException e)
        {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            database.close(stmt);
            System.exit(1);
        }

        File users = new File("..\\..\\source_file\\users.csv");
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(users));
            long start = System.currentTimeMillis();//开始时间
            String line;
            String parts_1;
            String[] parts_;
            String[] parts_left;
            String[] parts_sign;

            long mid;
            String name;
            Date birthday;
            String sex;
            int level;
            String identity;

            ArrayList<Object> row = new ArrayList<>();

            int cnt = 0;
            //限制导入条数
            String buffersign;//给签名的缓冲
            try
            {
                int test_cnt = 0;
                while ((line = bufferedReader.readLine()) != null && test_cnt <= 2)//限制
                {
                    String sign = "";
                    test_cnt++;
                    if (cnt == 0)
                    {//跳过表头
                        cnt++;
                        continue;
                    }
                    parts_ = line.split("\\,", 2);
                    //奇妙的昵称


                    int cou_to_start = 1;
                    if (parts_[1].charAt(0) == '"')
                    {
                        while (parts_[1].charAt(cou_to_start) != '"' | parts_[1].charAt(cou_to_start + 1) != ',')
                            cou_to_start++;
                        parts_1 = parts_[1].substring(1, cou_to_start);
                        parts_left = parts_[1].substring(cou_to_start + 2, parts_[1].length() - 1).split(",", 4);
                    }
                    else
                    {
                        parts_left = parts_[1].split(",", 5);
                        parts_1 = parts_left[0];
                        for (int i = 0; i < 4; i++)
                            parts_left[i] = parts_left[i + 1];
                    }


                    //换行的奇妙签名
                    if (parts_left[3] != "" && parts_left[3].charAt(0) == '"')
                    {
                        do
                        {
                            buffersign = bufferedReader.readLine();
                            parts_left[3] += "\n" + buffersign;
                        }
                        while (!buffersign.contains("user"));
                    }


                    if (parts_.length > 1)
                    {
                        mid = Long.parseLong(parts_[0]);
                        name = parts_1;
                        sex = parts_left[0];

                        //处理日期
                        String[] tmp = parts_left[1].split("月");
                        if (tmp.length == 1)
                        {
                            birthday = null;
                        }
                        else
                        {
                            birthday = new Date(0, Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1].substring(0, tmp[1].length() - 1)));
                        }

                        //处理用户类型
                        boolean f;
                        level = Integer.parseInt(parts_left[2]);
                        if (parts_left[3].charAt(parts_left[3].length() - 5) == 'r')
                        {
                            identity = "superuser";
                            f = true;
                        }
                        else
                        {
                            identity = "user";
                            f = false;
                        }

                        //处理签名和订阅
                        parts_sign = parts_left[3].split("\\[");
                        for (int i = 0; i < parts_sign.length - 1; i++)
                            sign += parts_sign[i];
                        //其中parts_sign[parts_sign.length-1]为订阅
                        //System.out.println(parts_sign[parts_sign.length-1]);

                        if (sign.charAt(sign.length() - 1) == '"')
                            sign = sign.substring(0, sign.length() - 2);
                        else
                            sign = sign.substring(0, sign.length() - 1);
                        loadData(mid, name, sex, birthday, level, sign == "" ? null : sign, identity);
                        cnt++;
                        if (cnt % BATCH_SIZE == 0)
                        {
                            stmt.executeBatch();
                            stmt.clearBatch();
                        }

                    }
                }
                if (cnt % BATCH_SIZE != 0)
                {
                    stmt.executeBatch();
                }
                con.commit();//提交事务，运行后才导入数据库
                stmt.close();
                database.close(stmt);
                long end = System.currentTimeMillis();//结束时间
                System.out.println(cnt + " records successfully loaded");
                System.out.println("Loading speed : "
                        + (cnt * 1000) / (end - start)
                        + " records/s");
            }
            catch (IOException e)
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
        database.close(stmt);
    }
}
