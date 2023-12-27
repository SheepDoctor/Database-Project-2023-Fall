package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;
import io.sustc.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static io.sustc.service.impl.UserServiceImpl.isAuthValid;

@Service
@Slf4j
public class VideoServiceImp implements VideoService
{
    @Autowired
    private DataSource dataSource;

    /**
     * Posts a video. Its commit time shall be {@link LocalDateTime#now()}.
     *
     * @param auth the current user's authentication information
     * @param req  the video's information
     * @return the video's {@code bv}
     * @apiNote You may consider the following corner cases:
     * <ul>
     *   <li>{@code auth} is invalid, as stated in {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)}</li>
     *   <li>{@code req} is invalid
     *     <ul>
     *       <li>{@code title} is null or empty</li>
     *       <li>there is another video with same {@code title} and same user</li>
     *       <li>{@code duration} is less than 10 (so that no chunk can be divided)</li>
     *       <li>{@code publicTime} is earlier than {@link LocalDateTime#now()}</li>
     *     </ul>
     *   </li>
     * </ul>
     * If any of the corner case happened, {@code null} shall be returned.
     */
    @Override
    public String postVideo(AuthInfo auth, PostVideoReq req)
    {
        // 验证授权信息是否有效
        long authenticatedUserId = isAuthValid(auth, dataSource);
        if (authenticatedUserId == -1) return null; // 如果授权无效，返回 null

        // 检查视频请求的有效性
        if (req == null || req.getTitle() == null || req.getTitle().trim().isEmpty()
                || req.getDuration() < 10 || req.getPublicTime().before(Timestamp.valueOf(LocalDateTime.now())))
        {
            return null; // 请求无效，返回 null
        }

        // 生成视频 BV 号
        String bv = generateBvNumber();

        // SQL 语句插入视频数据
        String insertVideoSql = "INSERT INTO videos(bv, title, owner_mid, commit_time, public_time, duration, description)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement videoInsertStmt = conn.prepareStatement(insertVideoSql))
        {
            videoInsertStmt.setString(1, bv);
            videoInsertStmt.setString(2, req.getTitle());
            videoInsertStmt.setLong(3, authenticatedUserId);
            videoInsertStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // 使用当前时间作为提交时间
            videoInsertStmt.setTimestamp(5, req.getPublicTime());
            videoInsertStmt.setLong(6, (long) req.getDuration());
            videoInsertStmt.setString(7, req.getDescription());

            videoInsertStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Failed to post video", e);
        }
        return bv;
    }

    /**
     * 生成 BV 号
     * 使用随机数和固定算法生成 BV 号。
     *
     * @return 生成的 BV 号
     */
    private String generateBvNumber()
    {
        Random random = new Random();
        long av = random.nextInt(1000000000);
        String table = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF";
        int[] s = {11, 10, 3, 8, 4, 6};
        long xor = 177451812L;
        long add = 8728348608L;
        char[] bvChars = "BV1  4 1 7  ".toCharArray();
        av = (av ^ xor) + add;
        for (int i = 0; i < 6; i++)
        {
            bvChars[s[i]] = table.charAt((int) (av / Math.pow(58, i) % 58));
        }
        return new String(bvChars);
    }


    @Override
    public boolean deleteVideo(AuthInfo auth, String bv)
    {
        long mid = isAuthValid(auth, dataSource);
        if (mid == -1) return false;

        try (Connection conn = dataSource.getConnection())
        {
            // 查询auth相关信息
            String select_user_info = "select identity from users where mid = " + mid;
            PreparedStatement select_user_info_stmt = conn.prepareStatement(select_user_info);
            ResultSet user_info = select_user_info_stmt.executeQuery();

            // 判断是否是superuser
            if (user_info.next() && Objects.equals(user_info.getString("identity"), "superuser"))
            {
                String sql = "delete from videos where bv =" + bv;
                PreparedStatement stmt = conn.prepareStatement(sql);
                return stmt.executeUpdate() != -1;
            }

            // 判断是否是视频的上传者
            String select_video = "select owner_mid from videos where bv = " + bv;
            PreparedStatement select_video_stmt = conn.prepareStatement(select_video);
            ResultSet video_info = select_video_stmt.executeQuery();
            if (video_info.next() && video_info.getLong("owner_id") == mid)
            {
                String sql = "delete from videos where bv =" + bv;
                PreparedStatement stmt = conn.prepareStatement(sql);
                return stmt.executeUpdate() != -1;
            }
            return false;
        }
        catch (SQLException e)
        {
            System.out.println("VideoService deleteVideo: " + e);
            return false;
        }
    }

    @Override
    public boolean updateVideoInfo(AuthInfo auth, String bv, PostVideoReq req)
    {
        long mid = UserServiceImpl.isAuthValid(auth, dataSource);
        if (mid == -1) return false;

        try (Connection conn = dataSource.getConnection())
        {
            // 获取此视频信息
            String select_video = "select * from videos where bv = ?";

            try (PreparedStatement select_video_stmt = conn.prepareStatement(select_video))
            {
                select_video_stmt.setString(1, bv);
                try (ResultSet video_info = select_video_stmt.executeQuery())
                {
                    if (video_info.next())
                    {
                        // 验证更改者信息
                        if (video_info.getLong("owner_mid") != mid)
                            return false;
                        // 验证视频时长有无修改
                        if (video_info.getLong("duration") != req.getDuration())
                            return false;
                        //todo
                        return false;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public List<String> searchVideo(AuthInfo auth, String keywords, int pageSize, int pageNum)
    {
        String[] split = keywords.split("\\s+");

        // 创建一个用于存放搜索结果的列表
        List<String> bv = new LinkedList<>();

        // 验证用户身份。如果用户无效，返回 null
        long authMid = UserServiceImpl.isAuthValid(auth, dataSource);
        if (authMid == -1) return null;

        // 检查参数是否非法
        if (pageSize <= 0 || pageNum <= 0) return null;
        if (split.length == 0) return null;

        // 遍历每个关键词，对特殊字符进行转义
        for (int i = 0; i < split.length; i++)
        {
            split[i] = split[i].replaceAll("([%_+?*.])", "\\\\$1");
            split[i] = split[i].toLowerCase();
        }

        // 准备 SQL 查询语句。这将调用数据库中的 search_videos 函数
        String sqlSearchVideo = "select bv from search_videos (?, ?, ?, ?);";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmtSearchVideo = connection.prepareStatement(sqlSearchVideo))
        {
            // 设置 search_videos 函数的参数：关键词数组、用户ID、页面大小和页码
            Array sqlKeywordsArray = connection.createArrayOf("TEXT", split);
            stmtSearchVideo.setArray(1, sqlKeywordsArray);
            stmtSearchVideo.setLong(2, authMid);
            stmtSearchVideo.setInt(3, pageSize);
            stmtSearchVideo.setInt(4, pageNum);
            //log.info("search video sql {}", stmtSearchVideo);

            // 执行查询并处理结果集
            try (ResultSet resultSet = stmtSearchVideo.executeQuery())
            {
                // 遍历结果集，并将每个视频的 bv 添加到列表中
                while (resultSet.next())
                {
                    bv.add(resultSet.getString("bv"));
                }
            }
        }
        catch (Exception e)
        {
            // 打印异常信息并抛出运行时异常
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // 返回包含视频 bv 的列表
        return bv;
    }


    @Override
    public double getAverageViewRate(String bv)
    {
        try (Connection conn = dataSource.getConnection())
        {
            String query = """
                    select total / (num *videos.duration)
                      from (select sum(time) as total, count(*) as num from view where bv = ?) tmp
                               join videos on videos.bv=?
                      where tmp is not null;""";
            //String query = """
            //        select duration from videos""";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, bv);
            preparedStatement.setString(2, bv);
            ResultSet resultSet = preparedStatement.executeQuery();
            float ans = -1;
            if (resultSet.next())
            {
                ans = resultSet.getFloat(1);
            }
            conn.close();
            return ans;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Integer> getHotspot(String bv)
    {
        Set<Integer> res = new HashSet<>();
        try (Connection conn = dataSource.getConnection())
        {
            String query2 = """
                    select time2 from (
                    select distinct time2,rank() over (order by cou desc ) as rank from (
                    select floor(time/10) as time2,count(*) over (partition by floor(time/10)) as cou from danmu where bv=?)tmp)tmp2
                    where rank=1::bigint
                    order by time2;""";
            PreparedStatement preparedStatement2 = conn.prepareStatement(query2);
            preparedStatement2.setString(1, bv);
            ResultSet resultSet2 = preparedStatement2.executeQuery();
            while (resultSet2.next())
            {
                res.add(resultSet2.getInt(1));
            }

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public boolean reviewVideo(AuthInfo auth, String bv)
    {
        long mid = isAuthValid(auth, dataSource);
        if (mid == -1) return false;

        try (Connection conn = dataSource.getConnection())
        {
            String query4 = "SELECT * FROM videos WHERE BV=?;";
            PreparedStatement preparedStatement4 = conn.prepareStatement(query4);
            preparedStatement4.setString(1, bv);
            ResultSet resultSet4 = preparedStatement4.executeQuery();
            if (!resultSet4.next() || resultSet4.getLong(1) == mid)
            {
                return false;
            }
            String query6 = "SELECT identity FROM USERS where mid= ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query6);
            preparedStatement.setLong(1, mid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
            {
                return false;
            }
            String query3 = "SELECT * FROM review WHERE BV=?;";
            PreparedStatement preparedStatement3 = conn.prepareStatement(query3);
            preparedStatement3.setLong(1, mid);
            ResultSet resultSet3 = preparedStatement3.executeQuery();
            if (!resultSet3.next())
            {
                String query5 = "INSERT INTO review(bv,reviewer_mid,review_time) values (?,?,?);";
                PreparedStatement preparedStatement5 = conn.prepareStatement(query5);
                preparedStatement5.setLong(2, mid);
                preparedStatement5.setString(1, bv);
                preparedStatement5.setTime(3, new Time(System.currentTimeMillis()));
                preparedStatement5.executeUpdate();
                ;
                return true;
            }
            else
            {
                return false;
            }

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean coinVideo(AuthInfo auth, String bv)
    {
        return like_collect(auth, bv, "coin");
    }

    @Override
    public boolean likeVideo(AuthInfo auth, String bv)
    {
        return like_collect(auth, bv, "like");
    }

    @Override
    public boolean collectVideo(AuthInfo auth, String bv)
    {
        return like_collect(auth, bv, "favorite");
    }

    public boolean like_collect(AuthInfo auth, String bv, String op)
    {
        long mid = UserServiceImpl.isAuthValid(auth, dataSource);
        if (isAuthValid(auth, dataSource) == -1)
            return false;

        try (Connection conn = dataSource.getConnection())
        {

            String query4 = "SELECT * FROM videos WHERE BV= ? ; ";
            String check_done_before = "SELECT * FROM " + op + " WHERE MID = ? ;";
            String update_op = "INSERT INTO " + op + " (bv, mid) values (?, ?) ;";

            try (PreparedStatement stmtVideo = conn.prepareStatement(query4))
            {
                stmtVideo.setString(1, bv);
                try (ResultSet resultSet4 = stmtVideo.executeQuery())
                {
                    if (!resultSet4.next() || resultSet4.getLong(1) == mid)
                        return false;
                }
            }
            List<String> res = searchVideo(auth, bv, 1, 1);
            if (res == null)
                return false;


            PreparedStatement check_statement = conn.prepareStatement(check_done_before);
            check_statement.setLong(1, mid);
            ResultSet resultSet3 = check_statement.executeQuery();
            if (!resultSet3.next())

            {
                PreparedStatement insert_statement = conn.prepareStatement(update_op);
                insert_statement.setLong(2, mid);
                insert_statement.setString(1, bv);
                insert_statement.executeUpdate();
                return true;
            }
            else
            {
                if (!op.equals("coin"))
                {
                    PreparedStatement insert_statement = conn.prepareStatement(update_op);
                    insert_statement.setLong(1, mid);
                    insert_statement.setString(2, bv);
                    insert_statement.executeUpdate();
                }
                return false;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

}