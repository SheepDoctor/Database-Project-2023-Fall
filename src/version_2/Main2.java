import utils.Database;
import version_1.Loader;
import version_1.Loader2;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main2
{
    public static void main(String[] args)
    {
        ExecutorService executorService= Executors.newCachedThreadPool();
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123abc");
        prop.put("database", "postgres");

        String file_path = "source_file/users.csv";
        String[] queue = {"Long", "String", "String", "Date", "Int", "String", "Skip","String"};
        String sql = "insert into users(Mid,Name,Sex,Birthday,Level,Sign,identity) values(?,?,?,?,?,?,?)";
        Database database = new Database(prop);

        Loader2 loader2 = new Loader2();
        loader2.write_data2(file_path, queue, database, sql, false, executorService);
    }
}