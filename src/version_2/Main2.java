import utils.Database;
import version_1.Loader;
import version_1.Loader2;
import version_1.RelationLoader;
import version_1.RelationLoader2;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main2
{
    public static void main(String[] args)
    {
        ExecutorService executorService= Executors.newCachedThreadPool();
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123abc");
        prop.put("database", "postgres");

        String file_path = "C:\\Users\\Dell\\Desktop\\学习\\Third Semester\\Database\\data.project1\\danmu.csv";
     //   String[] queue = {"Long", "String", "String", "Date", "Int", "String", "Skip","String"};
     //   String sql = "insert into users(Mid,Name,Sex,Birthday,Level,Sign,identity) values(?,?,?,?,?,?,?)";
        String[] queue = {"String", "Long", "Real", "String", "Int"};
        String sql = "insert into comment(BV,Mid,Time,content,id) values(?,?,?,?,?)";
        Database database = new Database(prop);

        RelationLoader2 loader2 = new RelationLoader2();
        loader2.write_data_2(file_path, queue, database, sql, false,false, executorService );
    }
}