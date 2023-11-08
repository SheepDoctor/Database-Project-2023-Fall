package version_1;

import utils.Database;

import java.util.Properties;

public class Danmu
{
    public static void main(String[] args)
    {
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        String file_path = "source_file/danmu_p.csv";
        String[] queue = {"String", "Long", "Real", "String", "Int"};
        String sql = "insert into comment(BV,Mid,Time,content,id) values(?,?,?,?,?)";
        Database database = new Database(prop);

        Loader loader = new Loader();
        loader.write_data(file_path, queue, database, sql, true, true, 12478996.0);
    }
}