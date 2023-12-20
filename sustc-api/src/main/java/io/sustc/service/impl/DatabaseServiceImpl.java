package io.sustc.service.impl;

import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;
import io.sustc.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
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
        importUser(userRecords);
        importVideo(videoRecords);
        importDanmu(danmuRecords);
        //throw new UnsupportedOperationException("TODO: implement your import logic");
    }

    private void importDanmu(List<DanmuRecord> danmuRecords)
    {
        String sqlImportDanmu = "INSERT INTO danmu (bv, mid, time, content, post_time) VALUES (?, ?, ?, ?, ?) returning id";
        final int batchSize = 500; // 每批处理的记录数
        long count = 0;

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
                try (PreparedStatement statement = conn.prepareStatement(sqlImportDanmuLikedBy))
                {
                    if (record.getLikedBy() != null)
                        for (Long id : record.getLikedBy())
                        {
                            statement.setLong(1, current);
                            statement.setLong(2, id);
                            statement.addBatch(); // 将当前设置的参数添加到此 PreparedStatement 对象的批处理中
                            if (++count % batchSize == 0)
                            {
                                statement.executeBatch(); // 执行批量插入
                                statement.clearBatch(); // 清除当前批处理
                            }
                        }
                    statement.executeBatch(); // 插入剩余的记录
                    statement.clearBatch();
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void importUser(List<UserRecord> userRecords)
    {
        String sqlImportUsers = "INSERT INTO users (mid, name, sex, birthday, level, coin, sign, identity, password, qq, wechat) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        final int batchSize = 500; // 每批处理的记录数
        long count = 0;

        //插入所有的用户
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlImportUsers))
        {
            for (UserRecord record : userRecords)
            {
                stmt.setLong(1, record.getMid());
                stmt.setString(2, record.getName());
                stmt.setString(3, record.getSex());
                stmt.setDate(4, record.getBirthday() != null ? java.sql.Date.valueOf(record.getBirthday()) : null);
                stmt.setShort(5, record.getLevel());
                stmt.setInt(6, record.getCoin());
                stmt.setString(7, record.getSign());
                stmt.setString(8, record.getIdentity().name());
                stmt.setString(9, record.getPassword());
                stmt.setString(10, record.getQq());
                stmt.setString(11, record.getWechat());
                log.info("SQL: {}", stmt);

                if (++count % batchSize == 0)
                {
                    stmt.executeBatch(); // 执行批量插入
                    stmt.clearBatch(); // 清除当前批处理
                }
            }
            stmt.executeBatch(); // 执行批量插入
            stmt.clearBatch(); // 清除当前批处理
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        count = 0;
        String sqlImportUserFollowing = "insert into user_follow (follow_mid, follow_by_mid) values (?, ?);";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlImportUserFollowing))
        {
            for (UserRecord record : userRecords)
            {
                for (long followingMid : record.getFollowing())
                {
                    statement.setLong(1, record.getMid());
                    statement.setLong(2, followingMid);
                    statement.addBatch(); // 将当前设置的参数添加到此 PreparedStatement 对象的批处理中
                    log.info("SQL: {}", statement);
                    if (++count % batchSize == 0)
                    {
                        statement.executeBatch(); // 执行批量插入
                        statement.clearBatch(); // 清除当前批处理
                    }
                }
            }
            statement.executeBatch(); // 执行批量插入
            statement.clearBatch(); // 清除当前批处理
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void importVideo(List<VideoRecord> videoRecords)
    {
        final int batchSize = 500; // 每批处理的记录数
        long count = 0;

        // 插入所有的视频
        String sqlImportVideo = "INSERT INTO videos (bv, title, owner_mid, commit_time, public_time, duration, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
        String sqlImportReview = "Insert into review (bv, reviewer_mid, review_time) values (?, ?, ?)";
        String sqlImportView = "Insert into view (bv, mid, time) values (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statementVideo = conn.prepareStatement(sqlImportVideo);
             PreparedStatement statementReview = conn.prepareStatement(sqlImportReview);
             PreparedStatement statementView = conn.prepareStatement(sqlImportView)
        )
        {
            //导入视频
            for (VideoRecord record : videoRecords)
            {
                statementVideo.setString(1, record.getBv());
                statementVideo.setString(2, record.getTitle());
                statementVideo.setLong(3, record.getOwnerMid());
                statementVideo.setTimestamp(4, record.getCommitTime());
                statementVideo.setTimestamp(5, record.getPublicTime());
                statementVideo.setFloat(6, record.getDuration());
                statementVideo.setString(7, record.getDescription());

                statementVideo.addBatch();
                log.info("SQL: {}", statementVideo);
                // 执行批量插入
                if (++count % batchSize == 0)
                {
                    statementVideo.executeBatch();
                    statementVideo.clearBatch();
                }
            }
            statementVideo.executeBatch();
            statementVideo.clearBatch();

            // 导入审核记录
            count = 0;
            for (VideoRecord record : videoRecords)
            {
                if (record.getReviewer() != null && record.getReviewTime() != null)
                {
                    statementReview.setString(1, record.getBv());
                    statementReview.setLong(2, record.getOwnerMid());
                    statementReview.setTimestamp(3, record.getReviewTime());
                    statementReview.addBatch();
                    log.info("SQL: {}", statementReview);
                    if (++count % batchSize == 0)
                    {
                        statementReview.executeBatch();
                        statementReview.clearBatch();
                    }
                }
            }
            statementReview.executeBatch();
            statementReview.clearBatch();

            //导入三连记录
            importTriple(videoRecords, conn.prepareStatement("Insert into likes (bv, mid) values (?, ?)"), "Like");
            importTriple(videoRecords, conn.prepareStatement("Insert into coin (bv, mid) values (?, ?)"), "Coin");
            importTriple(videoRecords, conn.prepareStatement("Insert into favorite (bv, mid) values (?, ?)"), "Favorite");

            //导入观看记录
            for (VideoRecord record : videoRecords)
            {
                if (record.getViewerMids() != null)
                    for (int i = 0; i < record.getViewerMids().length; i++)
                    {
                        statementView.setString(1, record.getBv());
                        statementView.setLong(2, record.getViewerMids()[i]);
                        statementView.setFloat(3, record.getViewTime()[i]);
                        statementView.addBatch();
                        log.info("SQL: {}", statementView);
                        if (++count % batchSize == 0)
                        {
                            statementView.executeBatch();
                            statementView.clearBatch();
                        }
                    }
            }
            statementView.executeBatch();
            statementView.clearBatch();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void importTriple(List<VideoRecord> videoRecords, PreparedStatement statement, String type) throws SQLException
    {
        int count = 0, batchSize = 500;
        for (VideoRecord record : videoRecords)
        {
            long[] data;
            switch (type)
            {
                case "Like" -> data = record.getLike();
                case "Coin" -> data = record.getCoin();
                default -> data = record.getFavorite();
            }

            for (long id : data)
            {
                statement.setString(1, record.getBv());
                statement.setLong(2, id);
            }
            statement.addBatch();
            log.info("SQL: {}", statement);
            if (++count % batchSize == 0)
            {
                statement.executeBatch();
                statement.clearBatch();
            }
        }
        statement.executeBatch();
        statement.clearBatch();
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

        String sql = """
                DO $$
                DECLARE
                    tables CURSOR FOR
                        SELECT tablename
                        FROM pg_tables
                        WHERE schemaname = 'public';
                BEGIN
                    FOR t IN tables
                    LOOP
                        EXECUTE 'TRUNCATE TABLE ' || QUOTE_IDENT(t.tablename) || ' CASCADE;';
                    END LOOP;
                END $$;
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
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
