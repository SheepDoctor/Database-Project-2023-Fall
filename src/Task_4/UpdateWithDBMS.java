package Task_4;

import utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class UpdateWithDBMS {
    private static void updateNullBirthdays(Connection connection, String tableName, String dateColumn, int rowsUpdated) throws SQLException {

        String primaryKeyColumn = "mid";

        String subQuerySQL = String.format(
                "SELECT %s FROM %s WHERE %s.%s IS NULL ORDER BY RANDOM() LIMIT %d",
                primaryKeyColumn, tableName, tableName, dateColumn, rowsUpdated
        );

        // 构建更新语句，它使用子查询的结果
        String updateSQL = String.format(
                "UPDATE %s SET %s = '1970-01-01' WHERE %s IN (%s)",
                tableName, dateColumn, primaryKeyColumn, subQuerySQL
        );

        // 执行更新语句
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSQL)) {
            updateStatement.executeUpdate();
        }
    }

    private static void updateNullSigns(Connection connection, String tableName, String signColumn, int rowsUpdated) throws SQLException {

        String primaryKeyColumn = "mid";
        String subQuerySQL = String.format(
                "SELECT %s FROM %s WHERE %s.%s IS NULL ORDER BY RANDOM() LIMIT %d",
                primaryKeyColumn, tableName, tableName, signColumn, rowsUpdated
        );

        // 构建删除语句，它使用子查询的结果
        String updateSQL = String.format(
                "UPDATE %s SET %s = '该用户太懒了，没有留下任何介绍哦。' WHERE %s IN (%s)",
                tableName, signColumn, primaryKeyColumn, subQuerySQL
        );


        // 执行更新语句
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSQL)) {
            updateStatement.executeUpdate();
        }
    }


    public static void main(String[] args) {
        for (int j = 0; j < 7; j++) {


            long start = System.nanoTime();
            // 数据库连接参数
            Properties prop = new Properties();
            prop.put("host", "localhost");
            prop.put("user", "postgres");
            prop.put("password", "123456");
            prop.put("database", "Project");

            Database database = new Database(prop);
            int rowsUpdated = 1000;
            try {
                Connection connection = database.open();
                connection.setAutoCommit(false); // 关闭自动提交

                for (int i = 0; i < 5; i++) {
                    // 更新空行
                    //updateNullBirthdays(connection, "users", "birthday", rowsUpdated / 5);
                    updateNullSigns(connection, "users", "sign", rowsUpdated / 5);
                    connection.commit(); // 提交事务
                    //System.out.println(rowsUpdated / 5 * (i + 1) + " rows updated.");
                }

                database.close(null);
                connection.close(); // 关闭数据库连接
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println(e);
            }
            long end = System.nanoTime();
            //System.out.print(rowsUpdated + " rows updated. ");
            //System.out.println("Speed: " + (rowsUpdated * 1000L) / (end - start) + " records/s");
            System.out.println((rowsUpdated * 1.0e9) / (end - start));
        }

    }

}
