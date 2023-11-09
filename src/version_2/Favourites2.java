package version_2;

import utils.Database;

import java.util.Properties;

public class Favourites2
{
    public static void main(String[] args)
    {
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123abc");;
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
                "Skip",
                "List",
                "Skip"};
        String sql = "insert into favourites(bv,mid) values(?,?)";
        Database database = new Database(prop);

        RelationLoader2 loader = new RelationLoader2();
        loader.write_data(file_path, queue, database, sql, false, false, 7865.0);
    }
}
