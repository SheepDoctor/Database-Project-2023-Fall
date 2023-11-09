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
        prop.put("password", "123abc");
        prop.put("database", "postgres");

        String file_path = "C:\\Users\\Dell\\Desktop\\学习\\Third Semester\\Database\\data.project1\\danmu.csv";
        String[] queue = {"String", "Long", "Real", "String", "Int"};
        String sql = "insert into comment(BV,Mid,Time,content,id) values(?,?,?,?,?)";
        Database database = new Database(prop);

        Loader loader = new Loader();
        loader.write_data(file_path, queue, database, sql, true, true, 0);
    }
}