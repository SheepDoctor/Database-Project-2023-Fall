package io.sustc.service.impl;

import com.alibaba.druid.pool.DruidDataSource;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


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


    private static DruidDataSource bigSource;

    static
    {
        bigSource = new DruidDataSource();
        bigSource.setDriverClassName("org.postgresql.Driver");
        bigSource.setUsername("sustc");
        bigSource.setPassword("123456");
        bigSource.setUrl("jdbc:postgresql://localhost:5432/sustc?useUnicode=true&characterEncoding=UTF-8");
        bigSource.setInitialSize(1);
        bigSource.setMaxActive(200);
    }

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
        importUsersInParallel(userRecords);
        //System.out.println("************* import video... begin**************");
        importVideo(videoRecords, danmuRecords, userRecords);
        bigSource.close();
    }

    private void importDanmu(List<DanmuRecord> danmuRecords)
    {
        String sqlImportDanmu = "INSERT INTO danmu (bv, mid, time, content, post_time) VALUES (?, ?, ?, ?, ?) returning id";
        String sqlImportDanmuLikedBy = "insert into danmu_likes (id, mid) values (?, ?)";
        final int batchSize = 1000; // 每批处理的记录数
        long count = 0, count2 = 0;

        //插入所有的弹幕
        try (Connection conn = bigSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlImportDanmu);
             PreparedStatement statement = conn.prepareStatement(sqlImportDanmuLikedBy))
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
                count2++;
                long current = resultSet.getLong(1);

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
            }
            statement.executeBatch(); // 插入剩余的记录
            statement.clearBatch();
        }
        catch (SQLException e)
        {
            log.error("Error during importing danmu {}", e.toString());
            throw new RuntimeException(e);
        }
        //log.info("{} danmu are imported.", count2);
        //log.info("{} danmu_like are imported.", count);
    }

    private void importUsersInParallel(List<UserRecord> userRecords)
    {
        final int threadCount = 16; // 使用多个线程
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount); // 创建固定大小的线程池

        // 分割userRecords列表，以便每个线程处理列表的一部分
        int totalRecords = userRecords.size();
        int recordsPerThread = totalRecords / threadCount;

        for (int i = 0; i < threadCount; i++)
        {
            int start = i * recordsPerThread;
            int end = (i == threadCount - 1) ? totalRecords : (start + recordsPerThread);
            List<UserRecord> subList = userRecords.subList(start, end);

            // 将子列表分配给线程
            executorService.submit(() -> importUser(subList));
        }

        // 关闭线程池，不再接受新任务，等待已提交任务完成
        executorService.shutdown();
        try
        {
            // 等待所有任务完成
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        //log.info("{} users are imported.", totalRecords);
    }

    private void importUser(List<UserRecord> userRecords)
    {
        String sqlImportUsers = "INSERT INTO users (mid, name, sex, birthday, level, coin, sign, identity, password, qq, wechat)" +
                " VALUES (?, ?, CAST( ? AS gender_type), ?, ?, ?, ?, CAST( ? AS identity_type), ?, ?, ?)";
        final int batchSize = 500; // 每批处理的记录数
        long count = 0;

        //插入所有的用户
        try (Connection conn = bigSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlImportUsers))
        {
            for (UserRecord record : userRecords)
            {
                stmt.setLong(1, record.getMid());
                stmt.setString(2, record.getName());
                stmt.setString(3, record.getSex());
                stmt.setString(4, record.getBirthday());
                stmt.setShort(5, record.getLevel());
                stmt.setInt(6, record.getCoin());
                stmt.setString(7, record.getSign());
                stmt.setString(8, record.getIdentity().name());
                stmt.setString(9, UserServiceImpl.generateSha256Hash(record.getPassword()));
                stmt.setString(10, record.getQq().equals("") ? null : record.getQq());
                stmt.setString(11, record.getWechat().equals("") ? null : record.getWechat());

                stmt.addBatch();// 把预编译语句置入当前批次中
                //log.info("SQL: {}", stmt);

                if (++count % batchSize == 0)
                {
                    //log.info("****user: {}", count);
                    stmt.executeBatch(); // 执行批量插入
                    stmt.clearBatch(); // 清除当前批处理
                }
            }
            stmt.executeBatch(); // 执行批量插入
            stmt.clearBatch(); // 清除当前批处理
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        //log.info("{} users are imported.", count);
    }

    private void importVideo(List<VideoRecord> videoRecords, List<DanmuRecord> danmuRecords, List<UserRecord> userRecords)
    {
        final int batchSize = 2000; // 每批处理的记录数
        long count = 0;

        // 插入所有的视频
        String sqlImportVideo = "INSERT INTO videos (bv, title, owner_mid, commit_time, public_time, duration, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
        String sqlImportReview = "Insert into review (bv, reviewer_mid, review_time) values (?, ?, ?)";
        String sqlImportView = "Insert into view (bv, mid, time) values (?, ?, ?)";

        try (Connection conn = bigSource.getConnection();
             PreparedStatement statementVideo = conn.prepareStatement(sqlImportVideo);
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
                //log.info("SQL: {}", statementVideo);
                // 执行批量插入
                if (++count % batchSize == 0)
                {
                    statementVideo.executeBatch();
                    statementVideo.clearBatch();
                }
            }
            statementVideo.executeBatch();
            statementVideo.clearBatch();
            //log.info("{} videos are imported.", count);

            int n = 7;
            Thread[] thread = new Thread[n];

            // 创建线程以导入“点赞”记录
            thread[0] = new Thread(() ->
            {
                try
                {
                    importTriple(videoRecords, "Insert into likes (bv, mid) values (?, ?)", "Like");
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            });

            // 创建线程以导入“投币”记录
            thread[1] = new Thread(() ->
            {
                try
                {
                    importTriple(videoRecords, "Insert into coin (bv, mid) values (?, ?)", "Coin");
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            });

            // 创建线程以导入“收藏”记录
            thread[2] = new Thread(() ->
            {
                try
                {
                    importTriple(videoRecords, "Insert into favorite (bv, mid) values (?, ?)", "Favorite");
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            });

            //导入观看记录
            thread[3] = new Thread(() ->
            {
                ExecutorService executorService = Executors.newFixedThreadPool(70); // 使用固定大小的线程池

                // 分配任务到线程池
                for (final VideoRecord record : videoRecords)
                {
                    executorService.submit(() -> importView(record));
                }

                executorService.shutdown(); // 关闭线程池
                try
                {
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // 等待所有任务完成
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }

                // 计算总观看次数
                int totalViews = videoRecords.stream()
                        .mapToInt(vr -> vr.getViewerMids().length)
                        .sum();
                //log.info("{} views are imported.", totalViews);
            });


            // 导入审核记录
            thread[4] = new Thread(() -> importReview(videoRecords));

            // 导入弹幕记录
            thread[5] = new Thread(() -> importDanmu(danmuRecords));

            // 导入用户关注记录
            thread[6] = new Thread(() -> importUserFollows(userRecords));

            // 创建一个固定大小的线程池来执行导入任务
            ExecutorService executorService = Executors.newFixedThreadPool(n);

            // 向线程池提交所有导入任务
            for (int i = 0; i < n; i++)
            {
                executorService.submit(thread[i]);
            }
            // 关闭线程池，不再接受新任务，等待已提交任务完成
            executorService.shutdown();

            // 等待所有任务完成
            try
            {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt(); // 重置中断状态
            }

            // 计算总关注数
            int totalFollows = userRecords.stream()
                    .mapToInt(record -> record.getFollowing().length)
                    .sum();
            //log.info("{} follows are prepared.", totalFollows);
            //catch (InterruptedException e) {
            //    throw new RuntimeException(e);
            //}
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // 辅助方法：导入用户关注记录
    private void importUserFollows(List<UserRecord> userRecords)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(70); // 使用固定大小的线程池
        userRecords.forEach(record -> executorService.submit(() -> importSingleUserFollow(record)));

        executorService.shutdown(); // 关闭线程池
        try
        {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // 等待所有任务完成
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    // 辅助方法：导入单个用户的关注记录
    private void importSingleUserFollow(UserRecord record)
    {
        String sqlImportUserFollowing = "insert into user_follow (follow_mid, follow_by_mid) values (?, ?)";
        try (Connection conn = bigSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlImportUserFollowing))
        {

            for (long followingMid : record.getFollowing())
            {
                statement.setLong(1, record.getMid());
                statement.setLong(2, followingMid);
                statement.addBatch();
            }
            statement.executeBatch(); // 执行批量插入
            statement.clearBatch();   // 清除批处理
        }
        catch (SQLException e)
        {
            log.error("Error during importing follow records for user {}", record.getMid(), e);
        }
    }

    // 辅助方法：导入单个视频的观看记录
    private void importView(VideoRecord record)
    {
        String sqlImportView = "Insert into view (bv, mid, time) values (?, ?, ?)";
        try (Connection conn = bigSource.getConnection();
             PreparedStatement statementView = conn.prepareStatement(sqlImportView))
        {

            for (int i = 0; i < record.getViewerMids().length; i++)
            {
                statementView.setString(1, record.getBv());
                statementView.setLong(2, record.getViewerMids()[i]);
                statementView.setFloat(3, record.getViewTime()[i]);
                statementView.addBatch();
            }
            statementView.executeBatch(); // 执行批量插入
            statementView.clearBatch(); // 清除批处理
        }
        catch (SQLException e)
        {
            log.error("Error during importing view records for video {}", record.getBv(), e);
        }
    }

    // 方法：导入三连（喜欢、投币、收藏）记录
    private void importTriple(List<VideoRecord> videoRecords, String sql, String type) throws SQLException
    {
        int n = 70;
        ExecutorService executorService = Executors.newFixedThreadPool(n); // 创建固定大小的线程池

        // 将任务分配到线程池
        videoRecords.forEach(record -> executorService.submit(() -> importRecord(record, sql, type)));

        executorService.shutdown(); // 关闭线程池
        try
        {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // 等待所有任务完成
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        //log.info("{} are imported", type); // 记录日志
    }

    // 辅助方法：导入单个视频记录（喜欢、投币、收藏）
    private void importRecord(VideoRecord record, String sql, String type)
    {
        try (Connection conn = bigSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql))
        {

            long[] data = switch (type)
                    {
                        case "Like" -> record.getLike();
                        case "Coin" -> record.getCoin();
                        default -> record.getFavorite();
                    };

            for (long id : data)
            {
                statement.setString(1, record.getBv());
                statement.setLong(2, id);
                statement.addBatch();
            }
            statement.executeBatch(); // 执行批量插入
            statement.clearBatch();   // 清除批处理
        }
        catch (SQLException e)
        {
            log.error("Error during importing " + type + " records for video " + record.getBv(), e);
        }
    }

    // 辅助方法：导入视频审核记录
    private void importReview(List<VideoRecord> videoRecords)
    {
        String sqlImportReview = "Insert into review (bv, reviewer_mid, review_time) values (?, ?, ?)";
        long count = 0, batchSize = 1000;

        try (Connection conn = bigSource.getConnection();
             PreparedStatement statementReview = conn.prepareStatement(sqlImportReview))
        {

            for (VideoRecord record : videoRecords)
            {
                if (record.getReviewer() != null && record.getReviewTime() != null)
                {
                    statementReview.setString(1, record.getBv());
                    statementReview.setLong(2, record.getReviewer());
                    statementReview.setTimestamp(3, record.getReviewTime());
                    statementReview.addBatch();

                    if (++count % batchSize == 0)
                    {
                        statementReview.executeBatch(); // 执行批量插入
                        statementReview.clearBatch();   // 清除批处理
                    }
                }
            }
            statementReview.executeBatch(); // 处理剩余记录
            statementReview.clearBatch();
            //log.info("{} reviews are imported.", count);
        }
        catch (SQLException e)
        {
            log.error("Error during importing review records", e);
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
        // 在大多数情况下，您可以使用我们提供的默认截断脚本、、、、
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
            //log.info("SQL: {}", stmt);

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