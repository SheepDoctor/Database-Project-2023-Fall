package io.sustc.service.impl;

import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;
import io.sustc.service.DatabaseService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.druid.pool.DruidDataSource;

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
public class DatabaseServiceImpl implements DatabaseService {

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
    private DruidDataSource bigSource;

    @Override
    public List<Integer> getGroupMembers() {
        return Arrays.asList(12212309, 12211818, 12211111);
    }

    @Override
    public void importData(
            List<DanmuRecord> danmuRecords,
            List<UserRecord> userRecords,
            List<VideoRecord> videoRecords
    ) {
        importUser(userRecords);
        System.out.println("************* import video... begin**************");
        importVideo(videoRecords, danmuRecords, userRecords);
    }

    private void importDanmu(List<DanmuRecord> danmuRecords) {
        String sqlImportDanmu = "INSERT INTO danmu (bv, mid, time, content, post_time) VALUES (?, ?, ?, ?, ?) returning id";
        String sqlImportDanmuLikedBy = "insert into danmu_likes (id, mid) values (?, ?)";
        final int batchSize = 1000; // 每批处理的记录数
        long count = 0;

        //插入所有的弹幕
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlImportDanmu);
             PreparedStatement statement = conn.prepareStatement(sqlImportDanmuLikedBy)) {
            for (DanmuRecord record : danmuRecords) {
                stmt.setString(1, record.getBv());
                stmt.setLong(2, record.getMid());
                stmt.setFloat(3, record.getTime());
                stmt.setString(4, record.getContent());
                stmt.setTimestamp(5, record.getPostTime());

                ResultSet resultSet = stmt.executeQuery(); // 返回一个自增主键
                resultSet.next();
                long current = resultSet.getLong(1);

                if (record.getLikedBy() != null)
                    for (Long id : record.getLikedBy()) {
                        statement.setLong(1, current);
                        statement.setLong(2, id);
                        statement.addBatch(); // 将当前设置的参数添加到此 PreparedStatement 对象的批处理中
                        if (++count % batchSize == 0) {
                            statement.executeBatch(); // 执行批量插入
                            statement.clearBatch(); // 清除当前批处理
                        }
                    }
                statement.executeBatch(); // 插入剩余的记录
                statement.clearBatch();
            }
        }
        catch (SQLException e) {
            log.error("Error during importing danmu {}", e.toString());
            throw new RuntimeException(e);
        }
        log.info("{} danmu_like are imported.", count);
    }

    private void importUser(List<UserRecord> userRecords) {
        String sqlImportUsers = "INSERT INTO users (mid, name, sex, birthday, level, coin, sign, identity, password, qq, wechat)" +
                " VALUES (?, ?, CAST( ? AS gender_type), ?, ?, ?, ?, CAST( ? AS identity_type), ?, ?, ?)";
        final int batchSize = 1000; // 每批处理的记录数
        long count = 0;

        //插入所有的用户
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlImportUsers)) {
            for (UserRecord record : userRecords) {
                stmt.setLong(1, record.getMid());
                stmt.setString(2, record.getName());
                stmt.setString(3, record.getSex());
                //String[] date=record.getBirthday().split("月");
                //stmt.setDate(4, record.getBirthday() != null|record.getBirthday()==" " ? Date.valueOf("2022-1-1") : null);
                stmt.setString(4, record.getBirthday());
                stmt.setShort(5, record.getLevel());
                stmt.setInt(6, record.getCoin());
                stmt.setString(7, record.getSign());
                stmt.setString(8, record.getIdentity().name());
                stmt.setString(9, record.getPassword());
                stmt.setString(10, record.getQq().equals("") ? null : record.getQq());
                stmt.setString(11, record.getWechat().equals("") ? null : record.getWechat());
                stmt.addBatch();// 把预编译语句置入当前批次中
                //log.info("SQL: {}", stmt);

                if (++count % batchSize == 0) {
                    //System.out.println("****user: " + count);
                    stmt.executeBatch(); // 执行批量插入
                    stmt.clearBatch(); // 清除当前批处理
                }
            }
            stmt.executeBatch(); // 执行批量插入
            stmt.clearBatch(); // 清除当前批处理
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        log.info("{} users are imported.", count);
    }

    private void importVideo(List<VideoRecord> videoRecords, List<DanmuRecord> danmuRecords, List<UserRecord> userRecords) {
        //int countt = 0;
        //for(int finalI = 0; finalI < 70; finalI++) {
        //    int m = videoRecords.size() / 70;
        //    for (int j = finalI * m; j < ((finalI == 69) ? videoRecords.size() : (finalI + 1) * m); j++) {
        //        VideoRecord record = videoRecords.get(j);
        //        for (int i = 0; i < record.getViewerMids().length; i++) {
        //            countt++;
        //        }
        //    }
        //}
        //System.out.println(countt);
        //System.out.println("******************************************************");

        bigSource = new DruidDataSource();
        bigSource.setDriverClassName("org.postgresql.Driver");
        bigSource.setUsername("sustc");
        bigSource.setPassword("123456");
        bigSource.setUrl("jdbc:postgresql://localhost:5432/sustc?useUnicode=true&characterEncoding=UTF-8");
        bigSource.setInitialSize(1);
        bigSource.setMaxActive(200);
        final int batchSize = 2000; // 每批处理的记录数
        long count = 0;

        // 插入所有的视频
        String sqlImportVideo = "INSERT INTO videos (bv, title, owner_mid, commit_time, public_time, duration, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
        String sqlImportReview = "Insert into review (bv, reviewer_mid, review_time) values (?, ?, ?)";
        String sqlImportView = "Insert into view (bv, mid, time) values (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statementVideo = conn.prepareStatement(sqlImportVideo);
             PreparedStatement statementReview = conn.prepareStatement(sqlImportReview);
        ) {
            //导入视频
            for (VideoRecord record : videoRecords) {
                statementVideo.setString(1, record.getBv());
                statementVideo.setString(2, record.getTitle());
                statementVideo.setLong(3, record.getOwnerMid());
                //System.err.println(record.getCommitTime());
                statementVideo.setTimestamp(4, record.getCommitTime());
                //System.err.println(record.getPublicTime());
                statementVideo.setTimestamp(5, record.getPublicTime());
                statementVideo.setFloat(6, record.getDuration());
                statementVideo.setString(7, record.getDescription());

                statementVideo.addBatch();
                //log.info("SQL: {}", statementVideo);
                // 执行批量插入
                if (++count % batchSize == 0) {
                    statementVideo.executeBatch();
                    statementVideo.clearBatch();
                }
            }
            statementVideo.executeBatch();
            statementVideo.clearBatch();
            log.info("{} videos are imported.", count);

            int n = 7;
            Thread[] thread = new Thread[n];

            //导入三连记录
            thread[0] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        importTriple(videoRecords, "Insert into likes (bv, mid) values (?, ?)", "Like");
                    }
                    catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread[1] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        importTriple(videoRecords, "Insert into coin (bv, mid) values (?, ?)", "Coin");
                    }
                    catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            thread[2] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        importTriple(videoRecords, "Insert into favorite (bv, mid) values (?, ?)", "Favorite");
                    }
                    catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            //导入观看记录
            thread[3] = new Thread(new Runnable() {
                @Override
                public void run() {
                    int n = 70;
                    Thread[] follow = new Thread[n];
                    for (int i = 0; i < n; i++) {
                        int finalI = i;
                        follow[i] = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try (Connection conn = bigSource.getConnection();
                                     PreparedStatement statementView = conn.prepareStatement(sqlImportView)) {
                                    int count2 = 0;
                                    int m = videoRecords.size() / n;
                                    for (int j = finalI * m; j < ((finalI == n - 1) ? videoRecords.size() : (finalI + 1) * m); j++) {
                                        VideoRecord record = videoRecords.get(j);
                                        for (int i = 0; i < record.getViewerMids().length; i++) {
                                            statementView.setString(1, record.getBv());
                                            statementView.setLong(2, record.getViewerMids()[i]);
                                            statementView.setFloat(3, record.getViewTime()[i]);
                                            statementView.addBatch();

                                            //log.info("SQL: {}", statement);
                                            if (++count2 % batchSize == 0) {
                                                //System.out.println("****view" + count);
                                                statementView.executeBatch();
                                                statementView.clearBatch();
                                            }
                                        }
                                        statementView.executeBatch();
                                        statementView.clearBatch();
                                    }
                                    //log.info("SubThread: {} follows are prepared.", count2);
                                }
                                catch (SQLException e) {
                                    log.error("Error during importing users_follow {}", e.toString());
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        follow[i].start();
                    }
                    boolean flag = true;
                    while (flag) {
                        flag = false;
                        for (int i = 0; i < n; i++) {
                            if (follow[i].getState() != Thread.State.TERMINATED) {
                                //System.out.println((i + 1) + "'th sub-thread in view state: " + follow[i].getState());
                                flag = true;
                            }
                        }
                        //System.out.println();
                    }

                    int t_view = 0;
                    for (VideoRecord record : videoRecords) {
                        if (record.getViewerMids() != null)
                            for (int i = 0; i < record.getViewerMids().length; i++) {
                                t_view++;
                            }
                    }
                    log.info("{} views are imported.", t_view);
                }
            });
            // 导入审核记录
            thread[4] = new Thread(new Runnable() {
                @Override
                public void run() {
                    long count = 0;
                    try {
                        for (VideoRecord record : videoRecords) {
                            if (record.getReviewer() != null && record.getReviewTime() != null) {
                                statementReview.setString(1, record.getBv());
                                statementReview.setLong(2, record.getOwnerMid());
                                statementReview.setTimestamp(3, record.getReviewTime());
                                statementReview.addBatch();
                                //log.info("SQL: {}", statementReview);
                                if (++count % batchSize == 0) {
                                    //System.out.println("****review" + count);
                                    statementReview.executeBatch();
                                    statementReview.clearBatch();
                                }
                            }
                        }
                        statementReview.executeBatch();
                        statementReview.clearBatch();
                        log.info("{} reviews are imported.", count);
                    }
                    catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread[5] = new Thread(new Runnable() {
                @Override
                public void run() {
                    importDanmu(danmuRecords);
                }
            });
            thread[6] = new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    int n = 70;
                    String sqlImportUserFollowing = "insert into user_follow (follow_mid, follow_by_mid) values (?, ?);";
                    Thread[] follow = new Thread[n];
                    for (int i = 0; i < n; i++) {
                        int finalI = i;
                        follow[i] = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try (Connection conn = bigSource.getConnection();
                                     PreparedStatement statement = conn.prepareStatement(sqlImportUserFollowing)) {
                                    int count2 = 0;
                                    int m = userRecords.size() / n;
                                    for (int j = finalI * m; j < ((finalI == n - 1) ? userRecords.size() : (finalI + 1) * m); j++) {
                                        UserRecord record = userRecords.get(j);
                                        for (long followingMid : record.getFollowing()) {
                                            statement.setLong(1, record.getMid());
                                            statement.setLong(2, followingMid);
                                            statement.addBatch(); // 将当前设置的参数添加到此 PreparedStatement 对象的批处理中

                                            //log.info("SQL: {}", statement);
                                            if (++count2 % batchSize == 0) {
                                                //System.out.println("**********sub-follow " + finalI + ": " + (double)(j - finalI * m + 1) / m * 100);
                                                statement.executeBatch(); // 执行批量插入
                                                statement.clearBatch(); // 清除当前批处理
                                            }
                                        }
                                        statement.executeBatch(); // 执行批量插入
                                        statement.clearBatch(); // 清除当前批处理
                                    }
                                    //log.info("SubThread: {} follows are prepared.", count2);
                                }
                                catch (SQLException e) {
                                    log.error("Error during importing users_follow {}", e.toString());
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        follow[i].start();
                    }
                    boolean flag = true;
                    while (flag) {
                        flag = false;
                        for (int i = 0; i < n; i++) {
                            if (follow[i].getState() != Thread.State.TERMINATED) {
                                //System.out.println((i + 1) + "'th sub-thread in user_follow state: " + follow[i].getState());
                                flag = true;
                            }
                        }
                        //System.out.println();
                    }
                }
            });

            for (int i = 0; i < n; i++) {
                thread[i].start();
            }
            boolean flag = true;
            while (flag) {
                //Thread.sleep(1000);
                flag = false;
                for (int i = 0; i < n; i++) {
                    if (thread[i].getState() != Thread.State.TERMINATED) {
                        //System.out.println((i + 1) + "'th thread state: " + thread[i].getState());
                        flag = true;
                    }
                }
                //System.out.println();
            }

            int temp = 0;
            for (UserRecord record : userRecords) {
                for (long followingMid : record.getFollowing()) {
                    temp++;
                }
            }
            log.info("{} follows are prepared.", temp);
        }
        catch (SQLException e) {
            log.error("Error during importing video detail {}", e.toString());
            throw new RuntimeException(e);
        }
        //catch (InterruptedException e) {
        //    throw new RuntimeException(e);
        //}
    }

    private void importTriple(List<VideoRecord> videoRecords, String sql, String type) throws SQLException {
        int n = 70, batchSize = 1000;
        Thread[] follow = new Thread[n];
        for (int i = 0; i < n; i++) {
            int finalI = i;
            follow[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (Connection conn = bigSource.getConnection();
                         PreparedStatement statement = conn.prepareStatement(sql)) {
                        int count2 = 0;
                        int m = videoRecords.size() / n;
                        for (int j = finalI * m; j < ((finalI == n - 1) ? videoRecords.size() : (finalI + 1) * m); j++) {
                            long[] data;
                            VideoRecord record = videoRecords.get(j);
                            switch (type) {
                                case "Like" -> data = record.getLike();
                                case "Coin" -> data = record.getCoin();
                                default -> data = record.getFavorite();
                            }
                            for (long id : data) {
                                statement.setString(1, record.getBv());
                                statement.setLong(2, id);
                                statement.addBatch();
                                if (++count2 % batchSize == 0) {
                                    //System.out.println("****" + type + ": " + count);
                                    statement.executeBatch();
                                    statement.clearBatch();
                                }
                            }
                            statement.executeBatch();
                            statement.clearBatch();
                        }
                        //log.info("SubThread: {} follows are prepared.", count2);
                    }
                    catch (SQLException e) {
                        log.error("Error during importing users_follow {}", e.toString());
                        throw new RuntimeException(e);
                    }
                }
            });
            follow[i].start();
        }
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < n; i++) {
                if (follow[i].getState() != Thread.State.TERMINATED) {
                    //System.out.println((i + 1) + "'th sub-thread in view state: " + follow[i].getState());
                    flag = true;
                }
            }
            //System.out.println();
        }
        log.info("{} are imported", type);
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
    public void truncate() {

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
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer sum(int a, int b) {
        String sql = "SELECT ?+?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a);
            stmt.setInt(2, b);
            //log.info("SQL: {}", stmt);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}