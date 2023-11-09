package version_2;

import utils.Database;

import java.util.Properties;

public class Videos2
{
    public static void main(String[] args)
    {
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123abc");
        prop.put("database", "Project");

        String file_path = "source_file/videos.csv";
        String[] queue = {"String", "String", "Long", "Skip", "Time", "Skip", "Time", "Int", "String", "Skip", "Skip", "Skip", "Skip", "Skip"};
        String sql = "insert into videos(bv, title, owner_id, commit_time, public_time, duration, description) values(?,?,?,?,?,?,?)";
        Database database = new Database(prop);

        Loader2 loader = new Loader2();
        loader.write_data(file_path, queue, database, sql, false, false, 7865.0);
    }
}
