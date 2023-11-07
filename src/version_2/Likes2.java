package version_1;

import utils.Database;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Likes2
{
    public static void main(String[] args)
    {
        ExecutorService executorService= Executors.newCachedThreadPool();

        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123abc");
        prop.put("database", "postgres");

        String file_path = "C:\\Users\\Dell\\Desktop\\学习\\Third Semester\\Database\\data.project1\\videos.csv";
        String[] queue = {"String", "Skip", "Skip", "Skip", "Skip", "Skip", "Skip", "Skip", "Skip", "Skip", "List", "Skip", "Skip", "Skip"};
        String sql = "insert into likes(bv,mid) values(?,?)";
        Database database = new Database(prop);

        RelationLoader loader = new RelationLoader();
        loader.write_data(file_path, queue, database, sql, false, false);
    }
}
