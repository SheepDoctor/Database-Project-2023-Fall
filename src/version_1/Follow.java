package version_1;

import utils.Database;

import java.util.Properties;

public class Follow
{
    public static void main(String[] args)
    {
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        String file_path = "source_file/users.csv";
        String[] queue = {
                "Long",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "Skip",
                "List",
                "Skip"
                };
        String sql = "insert into follow(follow_mid,follow_by_mid) values(?,?)";
        Database database = new Database(prop);
        RelationLoader loader = new RelationLoader();
        loader.write_data(file_path, queue, database, sql, false, false, 37881.0);
    }
}
