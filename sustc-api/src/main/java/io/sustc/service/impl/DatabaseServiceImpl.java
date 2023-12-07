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
        throw new UnsupportedOperationException("TODO: implement your import logic");
    }

    /*
     * The following code is just a quick example of using jdbc datasource.
     * Practically, the code interacts with database is usually written in a DAO layer.
     *
     * Reference: [Data Access Object pattern](https://www.baeldung.com/java-dao-pattern)
     */
    /*
     * 以下代码只是使用 jdbc 数据源的快速示例。
     * 实际上，与数据库交互的代码通常写在DAO层中。
     *
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
