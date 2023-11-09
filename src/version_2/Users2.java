package version_2;

import utils.Database;

import java.util.Properties;

import static version_2.Reviewer2.reviewer;

public class Users2
{
    public static void main(String[] args)
    {
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123abc");
        prop.put("database", "Project");
        String file_path = "source_file/users.csv";
        String[] queue = {"Long", "String", "String", "Date", "Int", "String", "Skip", "String"};
        String sql = "insert into users(Mid,Name,Sex,Birthday,Level,Sign,identity) values(?,?,?,?,?,?,?)";
        Database database = new Database(prop);

        Loader2 loader = new Loader2();
        System.out.println("USER导入......");
        loader.write_data(file_path, queue, database, sql, false, false, 37881.0);

        reviewer();
    }
}
