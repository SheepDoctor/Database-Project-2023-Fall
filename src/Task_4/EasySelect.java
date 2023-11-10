package Task_4;

import utils.Database;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class EasySelect {
    public static void main(String[] args) {
        // 数据库连接参数
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        Database database = new Database(prop);
        QWriter qWriter = new QWriter();
        double[] time = new double[100];
        for (int i = 0; i < 100; i++) {
            try {
                long start = System.nanoTime();
                Connection connection = database.open();
                connection.setAutoCommit(false); // 关闭自动提交

                selectLevel1Rows(connection, qWriter);
                connection.commit();

                database.close(null); // 关闭数据库连接
                connection.close();

                long end = System.nanoTime();
                time[i] = (end - start) / 1.0e9;


            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println(e);
            }
        }
        for (double num : time
        ) {
            qWriter.println(num);
        }
        qWriter.close();
    }


    private static void selectLevel1Rows(Connection connection, QWriter qWriter) throws SQLException {
        // 构建一个查询，它会选择满足条件的行，并返回这些行的主键
        String querySQL = "select level,count(*) from users group by  level order by level ;";
        // "select mid from users where level = 1 order by mid asc;"
        // "select count(*) from users where level = 1 ;"
        // 执行语句
        try (PreparedStatement selectStatement = connection.prepareStatement(querySQL)) {
            ResultSet resultSet = selectStatement.executeQuery();
            while (resultSet.next()) {
                qWriter.println(resultSet.getLong(1) + " " + resultSet.getLong(2));
            }
        }
    }
}

class QWriter implements Closeable {
    private final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

    public void print(Object object) {
        try {
            writer.write(object.toString());
        } catch (IOException e) {
        }
    }

    public void println(Object object) {
        try {
            writer.write(object.toString());
            writer.write("\n");
        } catch (IOException e) {
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
        }
    }
}