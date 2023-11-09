package version_2;

import utils.Database;

import java.util.Properties;

public class Review2
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
                "String",
                "Skip",
                "Skip",
                "Skip",
                "Time",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Long",
                "Skip",
                "Skip",
                "Skip",
                "Skip"};
        String sql = "insert into review(bv,review_time,reviewer_mid) values(?,?,?)";
        Database database = new Database(prop);

        Loader2 loader = new Loader2();
        loader.write_data(file_path, queue, database, sql, false, false, 7865.0);
    }
}
