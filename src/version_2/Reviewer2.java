package version_2;

import com.opencsv.CSVReader;
import utils.Database;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reviewer2
{
    public static void reviewer()
    {
        ExecutorService executorService= Executors.newCachedThreadPool();

        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123abc");
        prop.put("database", "Project");
        Database database = new Database(prop);
        Loader2 loader = new Loader2();
        check_data(database);
        String file_path = "source_file/videos.csv";
        String[] queue = {"Skip", "Skip", "Skip", "Skip", "Skip", "Skip", "Skip", "Skip",
                "Skip", "Long", "Skip", "Skip", "Skip", "Skip"};
        String sql = "insert into users(Mid,identity) values(?,'Reviewer') on conflict (Mid) do nothing";
        System.out.println("REVIEWER导入......");
        loader.write_data(file_path, queue, database, sql, false, false, 7865.0,executorService);
    }
    public static void check_data(Database database)
    {
        System.out.println("更新USERS......");
        int BATCH_SIZE = 500;
        int cnt = 1;
        String sql = "update users set is_reviewer = true where mid = ?";
        Connection con = database.open();
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement(sql);
        }
        catch (SQLException e)
        {
            System.err.println("Update statement failed");
        }
        try
        {
            FileReader fr = new FileReader("source_file/videos.csv");
            CSVReader reader;
            reader = new CSVReader(fr);
            String[] lineData = reader.readNext();
            while ((lineData = reader.readNext()) != null)
            {
                Long mid = Long.parseLong(lineData[9]);
                stmt.setLong(1, mid);
                stmt.addBatch();
                if (cnt % BATCH_SIZE == 0)
                {
                    System.out.printf("用户表导更新进度：%.3f%%\n", cnt / 7865.0 * 100);
                    stmt.executeBatch();
                    stmt.clearBatch();
                }
                cnt++;
            }
            try
            {
                con.commit();//提交事务，运行后才导入数据库
                stmt.close();
                database.close(stmt);
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
                    System.err.println("1:" + e2);
                }
                database.close(stmt);
                System.exit(1);
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("FileNotFound");
        }
        catch (IOException e)
        {
            System.err.println("2:" + e);
        }
        catch (SQLException e)
        {
            System.err.println("3:" + e);
        }
        database.close(stmt);
    }
}
