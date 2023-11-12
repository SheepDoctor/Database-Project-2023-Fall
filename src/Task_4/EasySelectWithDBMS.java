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

public class EasySelectWithDBMS {
    public static void main(String[] args) {
        // 数据库连接参数
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        Database database = new Database(prop);
        QWriter qWriter = new QWriter();
        int runTime = 100;
        double[] time = new double[runTime];
        for (int i = 0; i < runTime; i++) {
            try {
                long start = System.nanoTime();
                Connection connection = database.open();
                connection.setAutoCommit(false); // 关闭自动提交

                //selectOrderedLevel2(connection, qWriter);
                //selectLevel1Count(connection, qWriter);
                select100MinMidComment(connection, qWriter);
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

    // 查询所有等级为2的行，并且按mid的升序输出
    private static void selectOrderedLevel2(Connection connection, QWriter qWriter) throws SQLException {
        String querySQL = "select mid from users where level = 2 order by mid;";
        try (PreparedStatement selectStatement = connection.prepareStatement(querySQL)) {
            ResultSet resultSet = selectStatement.executeQuery();
            while (resultSet.next()) {
                //打印结果
                qWriter.println(resultSet.getLong(1));
            }
        }
    }

    // 查询所有level为1的列的数量
    private static void selectLevel1Count(Connection connection, QWriter qWriter) throws SQLException {
        String querySQL = "select count(*) from users where level = 1;";
        try (PreparedStatement selectStatement = connection.prepareStatement(querySQL)) {
            ResultSet resultSet = selectStatement.executeQuery();
            while (resultSet.next()) {
                //打印结果
                qWriter.println(resultSet.getInt(1));
            }
        }
    }

    private static void select100MinMidComment(Connection connection, QWriter qWriter) throws SQLException {
        String querySQL = "SELECT u.mid, c.content FROM (SELECT mid FROM users ORDER BY mid LIMIT 100) AS u LEFT JOIN comment c ON u.mid = c.mid;";
        try (PreparedStatement selectStatement = connection.prepareStatement(querySQL)) {
            ResultSet resultSet = selectStatement.executeQuery();
            while (resultSet.next()) {
                //打印结果
                qWriter.println(resultSet.getInt(1) + " " + resultSet.getString(2));
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