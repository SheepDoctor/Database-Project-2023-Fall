package Task_4;

import utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DeleteWithDBMS {
    public static void main(String[] args) {
        // 数据库连接参数
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        // 随机删除的行数
        int rowsToDelete = 10000;

        Database database = new Database(prop);
        for (int i = 0; i < 40; i++) {


            try {
                long start = System.currentTimeMillis();
                Connection connection = database.open();
                connection.setAutoCommit(false); // 关闭自动提交


                // 分页查询并批量删除
                int pageSize = rowsToDelete / 5;
                for (int page = 0; page < rowsToDelete / pageSize; page++) {
                    deleteRandomRows(connection, "users", "mid", pageSize);
                    connection.commit();
                    //System.out.println("已删除" + page * pageSize + "条。");
                }

                database.close(null); // 关闭数据库连接
                connection.close();
                long end = System.currentTimeMillis();
                //System.out.print(rowsToDelete + " rows deleted. ");
                //System.out.println("Speed: " + (rowsToDelete * 1000L) / (end - start) + " records/s");
                System.out.println((rowsToDelete * 1000L) / (end - start));
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println(e);
            }
        }
    }

    private static void deleteRandomRows(Connection connection, String tableName, String primaryKeyColumn, int rowsToDelete) throws SQLException {
        // 构建一个子查询，它会随机选择指定数量的行，并返回这些行的主键
        String subquerySQL = String.format(
                "SELECT %s FROM %s ORDER BY RANDOM() LIMIT %d",
                primaryKeyColumn, tableName, rowsToDelete
        );

        // 构建删除语句，它使用子查询的结果
        String deleteSQL = String.format(
                "DELETE FROM %s WHERE %s IN (%s)",
                tableName, primaryKeyColumn, subquerySQL
        );

        // 执行删除语句
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL)) {
            deleteStatement.executeUpdate();
        }
    }

}
