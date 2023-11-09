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
        prop.put("password", "123abc");
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

        ViewLoader loader = new ViewLoader();
        loader.write_data(file_path, queue, database, sql, false, false);
    }
}