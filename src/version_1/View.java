package version_1;

import utils.Database;

import java.util.Properties;

public class View
{
    public static void main(String[] args)
    {
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        String file_path = "source_file/videos.csv";
        String[] queue = {
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Long",
                "Skip",
                "Skip",
                "Skip",
                "Skip"};
        String sql = "insert into reviewer(reviewer_id) values(?)";
        Database database = new Database(prop);

        Loader loader = new Loader();
        loader.write_data(file_path, queue, database, sql, false, false);
    }
}