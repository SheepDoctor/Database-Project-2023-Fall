import utils.Database;
import version_1.Loader;

import java.util.Properties;

public class Main
{
    public static void main(String[] args)
    {
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        String file_path = "source_file/videos.csv";
        String[] queue = {"String", "String", "Long", "Skip", "Time", "Skip", "Time", "Int", "String", "Skip", "Skip", "Skip", "Skip", "Skip"};
        String sql = "insert into videos(bv, title, owner_id, commit_time, public_time, duration, description) values(?,?,?,?,?,?,?)";
        Database database = new Database(prop);

        Loader loader = new Loader();
        loader.write_data(file_path, queue, database, sql);
    }
}