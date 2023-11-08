package version_1;

import utils.Database;
import version_1.Loader;

import java.util.Properties;

import static version_1.Reviewer.reviewer;

public class Users
{
    public static void main(String[] args)
    {
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        String file_path = "source_file/users.csv";
        String[] queue = {"Long", "String", "String", "Date", "Int", "String", "Skip", "String"};
        String sql = "insert into users(Mid,Name,Sex,Birthday,Level,Sign,identity) values(?,?,?,?,?,?,?)";
        Database database = new Database(prop);

        Loader loader = new Loader();
        System.out.println("USER导入......");
        loader.write_data(file_path, queue, database, sql, false, false, 37881.0);

        reviewer();
    }
}
