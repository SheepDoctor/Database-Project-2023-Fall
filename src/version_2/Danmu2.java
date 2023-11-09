package version_2;

import utils.Database;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Danmu2
{
    public static void main(String[] args)
    {
        ExecutorService executorService= Executors.newCachedThreadPool();
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123abc");
        prop.put("database", "Project");

        String file_path = "source_file/danmu_p.csv";
        String[] queue = {"String", "Long", "Real", "String", "Int"};
        String sql = "insert into comment(BV,Mid,Time,content,id) values(?,?,?,?,?)";
        Database database = new Database(prop);

        Loader2 loader = new Loader2();
        loader.write_data(file_path, queue, database, sql, true, true, 12478996.0,executorService);
    }
}