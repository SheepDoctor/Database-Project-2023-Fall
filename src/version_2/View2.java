package version_2;

import utils.Database;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class View2
{
    public static void main(String[] args)
    {
        ExecutorService executorService= Executors.newCachedThreadPool();
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        String file_path = "source_file/videos.csv";
        //String file_path = "source_file/draft.csv";
        String[] queue = {
                "Long",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "List"};
        String sql = "insert into view(bv,mid,time) values(?,?,?)";
        Database database = new Database(prop);

        ViewLoader2 loader = new ViewLoader2();
        loader.write_data(file_path, queue, database, sql, false, false,executorService);
    }
}