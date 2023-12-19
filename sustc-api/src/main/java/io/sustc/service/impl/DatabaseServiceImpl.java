package io.sustc.service.impl;

import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;
import io.sustc.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * It's important to mark your implementation class with {@link Service} annotation.
 * As long as the class is annotated and implements the corresponding interface, you can place it under any package.
 * 使用 {@link Service} 注解标记实现类非常重要。
 * 只要类已被注解并实现了相应的接口，就可以将其放在任何软件包中。
 */
@Service
@Slf4j
public class DatabaseServiceImpl implements DatabaseService
{

    /**
     * Getting a {@link DataSource} instance from the framework, whose connections are managed by HikariCP.
     * <p>
     * Marking a field with {@link Autowired} annotation enables our framework to automatically
     * provide you a well-configured instance of {@link DataSource}.
     * Learn more: <a href="https://www.baeldung.com/spring-dependency-injection">Dependency Injection</a>
     * 从框架获取一个{@link DataSource}实例，其连接由HikariCP管理。
     * <p>
     * 使用 {@link Autowired} 注解标记字段使我们的框架能够自动
     * 为您提供一个配置良好的 {@link DataSource} 实例。
     * 了解更多：<a href="https://www.baeldung.com/spring-dependency-injection">依赖注入</a>
     */

    @Autowired
    private DataSource dataSource;

    @Override
    public List<Integer> getGroupMembers()
    {
        return Arrays.asList(12212309, 12211818, 12211111);
    }

    @Override
    public void importData(
            List<DanmuRecord> danmuRecords,
            List<UserRecord> userRecords,
            List<VideoRecord> videoRecords
    )
    {
        importDanmu(danmuRecords);
        //throw new UnsupportedOperationException("TODO: implement your import logic");
    }

    private void importDanmu(List<DanmuRecord> danmuRecords)
    {
        String sqlImportDanmu = "INSERT INTO danmu (bv, mid, time, content, post_time) VALUES (?, ?, ?, ?, ?) returning id";
        final int batchSize = 500; // 每批处理的记录数
        int count = 0;

        //插入所有的弹幕
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlImportDanmu))
        {
            for (DanmuRecord record : danmuRecords)
            {
                stmt.setString(1, record.getBv());
                stmt.setLong(2, record.getMid());
                stmt.setFloat(3, record.getTime());
                stmt.setString(4, record.getContent());
                stmt.setTimestamp(5, record.getPostTime());

                ResultSet resultSet = stmt.executeQuery(); // 返回一个自增主键
                resultSet.next();
                long current = resultSet.getLong(1);


                String sqlImportDanmuLikedBy = "insert into danmu_likes (id, mid) values (?, ?)";
                for (Long id : record.getLikedBy())
                {
                    try(PreparedStatement statement=conn.prepareStatement(sqlImportDanmuLikedBy)){

                    }
                }

                stmt.addBatch(); // 将当前设置的参数添加到此 PreparedStatement 对象的批处理中
                if (++count % batchSize == 0)
                {
                    stmt.executeBatch(); // 执行批量插入
                    stmt.clearBatch(); // 清除当前批处理
                }
            }

            stmt.executeBatch(); // 插入剩余的记录
            conn.commit(); // 提交事务
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        //查询对应弹幕的id
        HashMap<DanmuRecord, Long> hashMap = new HashMap<>();
        count = 0;
        sqlImportDanmu = "select id from danmu where mid = ? and post_time = ? ";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlImportDanmu))
        {
            for (DanmuRecord record : danmuRecords)
            {
                stmt.setString(1, record.getBv());
                stmt.setTimestamp(2, record.getPostTime());
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next())
                {
                    hashMap.put(record, resultSet.getLong("id"));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        //导入用户喜欢弹幕的数据
        sqlImportDanmu = "INSERT INTO danmu_likes (id, mid) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlImportDanmu))
        {
            for (DanmuRecord danmuRecord : danmuRecords)
            {
                long id = hashMap.get(danmuRecord);
                for (long mid :
                        danmuRecord.getLikedBy())
                {
                    stmt.setLong(1, id);
                    stmt.setLong(2, mid);
                    stmt.addBatch(); // 将当前设置的参数添加到此 PreparedStatement 对象的批处理中
                    if (++count % batchSize == 0)
                    {
                        stmt.executeBatch(); // 执行批量插入
                        stmt.clearBatch(); // 清除当前批处理
                    }
                }
                stmt.executeBatch(); // 执行批量插入
                stmt.clearBatch(); // 清除当前批处理
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * The following code is just a quick example of using jdbc datasource.
     * Practically, the code interacts with database is usually written in a DAO layer.
     * <p>
     * Reference: [Data Access Object pattern](https://www.baeldung.com/java-dao-pattern)
     * <p>
     * <p>
     * 以下代码只是使用 jdbc 数据源的快速示例。
     * 实际上，与数据库交互的代码通常写在DAO层中。
     * <p>
     * 参考：[数据访问对象模式](https://www.baeldung.com/java-dao-pattern)
     */

    @Override
    public void truncate()
    {
        // You can use the default truncate script provided by us in most cases,
        // but if it doesn't work properly, you may need to modify it.
        // 在大多数情况下，您可以使用我们提供的默认截断脚本、
        // 但如果它不能正常工作，您可能需要修改它。

        String sql = "DO $$\n" +
                "DECLARE\n" +
                "    tables CURSOR FOR\n" +
                "        SELECT tablename\n" +
                "        FROM pg_tables\n" +
                "        WHERE schemaname = 'public';\n" +
                "BEGIN\n" +
                "    FOR t IN tables\n" +
                "    LOOP\n" +
                "        EXECUTE 'TRUNCATE TABLE ' || QUOTE_IDENT(t.tablename) || ' CASCADE;';\n" +
                "    END LOOP;\n" +
                "END $$;\n";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer sum(int a, int b)
    {
        String sql = "SELECT ?+?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, a);
            stmt.setInt(2, b);
            log.info("SQL: {}", stmt);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
