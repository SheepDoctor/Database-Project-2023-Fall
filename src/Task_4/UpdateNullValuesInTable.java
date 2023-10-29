package Task_4;

import utils.Database;

import java.sql.*;
import java.util.Properties;

public class UpdateNullValuesInTable {
    public static void main(String[] args) {
        // 数据库连接参数
        Properties prop = new Properties();
        prop.put("host", "localhost");
        prop.put("user", "postgres");
        prop.put("password", "123456");
        prop.put("database", "Project");

        // 任意值用于替换空值
        String replacementValue = "该用户太懒了，没有留下任何介绍哦。";

        Database database = new Database(prop);


        Connection connection = database.open();

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        //
        String tableName = "users";
        String columnName = "birthday";//sign

        // 获取包含空值的记录的数量
        int nullValueCount = getNullValueCount(connection, tableName, columnName);

        // 遍历记录，将空值更新为替代值
        updateNullValues(connection, tableName, columnName, replacementValue);

        // 记录结束时间
        long endTime = System.currentTimeMillis();

        long timeCost = endTime - startTime;
        System.out.println("Time cost for updating " + nullValueCount + " null values: " + timeCost + " ms");
        System.out.println("Speed: " + nullValueCount * 1000L / timeCost  + " null values /s");

        database.close(null); // 关闭数据库连接

    }

    private static int getNullValueCount(Connection connection, String tableName, String columnName) {
        int nullValueCount = 0;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // 使用 SQL 查询语句来统计包含空值的记录数量
            String sqlQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " IS NULL";
            preparedStatement = connection.prepareStatement(sqlQuery);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                nullValueCount = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭 ResultSet 和 PreparedStatement
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return nullValueCount;
    }

    private static void updateNullValues(Connection connection, String tableName, String columnName, String replacementValue) {
        PreparedStatement preparedStatement = null;

        try {
            // 使用 SQL 更新语句来将空值更新为替代值
            String sqlUpdate = "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + columnName + " IS NULL ";
            preparedStatement = connection.prepareStatement(sqlUpdate);
            //preparedStatement.setString(1, replacementValue);
            preparedStatement.setDate(1, new Date(0));
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭 PreparedStatement
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
