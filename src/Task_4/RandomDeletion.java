package Task_4;

import utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class RandomDeletion {
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

        try {
            long start = System.currentTimeMillis();
            Connection connection = database.open();

            // 获取表中所有主键值
            List<Long> allPrimaryKeys = getAllPrimaryKeys(connection, "users", "mid");

            // 随机选择要删除的主键值
            List<Long> deletedPrimaryKeys = getRandomPrimaryKeys(allPrimaryKeys, rowsToDelete);

            // 批量删除
            batchDeleteRowsWithPrimaryKeys(connection, "users", "mid", deletedPrimaryKeys);

            connection.commit();
            database.close(null); // 关闭数据库连接
            long end = System.currentTimeMillis();
            System.out.println(rowsToDelete + " rows deleted.");
            System.out.println("Speed : " + (rowsToDelete * 1000L) / (end - start) + " records/s");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    private static List<Long> getAllPrimaryKeys(Connection connection, String tableName, String primaryKeyColumn) throws SQLException {
        List<Long> primaryKeys = new ArrayList<>();
        String querySQL = "SELECT " + primaryKeyColumn + " FROM " + tableName;
        PreparedStatement queryStatement = connection.prepareStatement(querySQL);
        ResultSet resultSet = queryStatement.executeQuery();
        while (resultSet.next()) {
            primaryKeys.add(resultSet.getLong(primaryKeyColumn));
        }
        return primaryKeys;
    }

    private static List<Long> getRandomPrimaryKeys(List<Long> allPrimaryKeys, int count) {
        List<Long> randomPrimaryKeys = new ArrayList<>();
        Random random = new Random();

        while (randomPrimaryKeys.size() < count) {
            int randomIndex = random.nextInt(allPrimaryKeys.size());
            randomPrimaryKeys.add(allPrimaryKeys.get(randomIndex));
            allPrimaryKeys.remove(randomIndex);
        }

        return randomPrimaryKeys;
    }

    private static void batchDeleteRowsWithPrimaryKeys(Connection connection, String tableName, String primaryKeyColumn, List<Long> primaryKeys) throws SQLException {
        String deleteSQL = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
        PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);

        for (Long primaryKeyValue : primaryKeys) {
            deleteStatement.setLong(1, primaryKeyValue);
            deleteStatement.addBatch();
        }

        deleteStatement.executeBatch();
    }
}
