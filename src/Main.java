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

        String file_path = "source_file/users.csv";
        String[] queue = {"Long", "String", "String", "Date", "Int", "String", "Skip","String"};
        String sql = "insert into users(Mid,Name,Sex,Birthday,Level,Sign,identity) values(?,?,?,?,?,?,?)";
        Database database = new Database(prop);

        Loader loader = new Loader();
        loader.write_data(file_path, queue, database, sql);
    }
}