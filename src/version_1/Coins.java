package version_1;

import utils.Database;

import java.util.Properties;

public class Coins
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
                "String",
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
                "List",
                "Skip",
                "Skip"};
        //String sql = "insert into coin(bv,mid) values(?,?)";
        String sql = "insert into coins(bv,mid) values(?,?)";
        Database database = new Database(prop);

        RelationLoader loader = new RelationLoader();
        loader.write_data(file_path, queue, database, sql, false, false);
    }
}
