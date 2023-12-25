package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;
import io.sustc.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static io.sustc.service.impl.UserServiceImpl.isAuthValid;

@Service
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
    public String postVideo(AuthInfo auth, PostVideoReq req) {
        // 验证授权信息是否有效
        long authenticatedUserId = isAuthValid(auth, dataSource);
        if (authenticatedUserId == -1) return null; // 如果授权无效，返回 null

        // 检查视频请求的有效性
        if (req == null || req.getTitle() == null || req.getTitle().trim().isEmpty()
                || req.getDuration() < 10 || req.getPublicTime().before(Timestamp.valueOf(LocalDateTime.now()))) {
            return null; // 请求无效，返回 null
        }

        // 生成视频 BV 号
        String bv = generateBvNumber();

        // SQL 语句插入视频数据
        String insertVideoSql = "INSERT INTO videos(bv, title, owner_mid, commit_time, public_time, duration, description)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement videoInsertStmt = conn.prepareStatement(insertVideoSql)) {
            videoInsertStmt.setString(1, bv);
            videoInsertStmt.setString(2, req.getTitle());
            videoInsertStmt.setLong(3, auth.getMid());
            videoInsertStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // 使用当前时间作为提交时间
            videoInsertStmt.setTimestamp(5, req.getPublicTime());
            videoInsertStmt.setLong(6, (long) req.getDuration());
            videoInsertStmt.setString(7, req.getDescription());

            videoInsertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to post video", e);
        }
        return bv;
    }

    /**
     * 生成 BV 号
     * 使用随机数和固定算法生成 BV 号。
     * @return 生成的 BV 号
     */
    private String generateBvNumber() {
        Random random = new Random();
        long av = random.nextInt(1000000000);
        String table = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF";
        int[] s = {11, 10, 3, 8, 4, 6};
        long xor = 177451812L;
        long add = 8728348608L;
        char[] bvChars = "BV1  4 1 7  ".toCharArray();
        av = (av ^ xor) + add;
        for (int i = 0; i < 6; i++) {
            bvChars[s[i]] = table.charAt((int) (av / Math.pow(58, i) % 58));
        }
        return new String(bvChars);
    }


    @Override
    public boolean deleteVideo(AuthInfo auth, String bv)
    {
        try
        {
            Connection conn = dataSource.getConnection();
            // 查询auth相关信息
            String select_user_info = "select identity from users where mid = " + auth.getMid();
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
            if (video_info.next() && video_info.getLong("owner_id") == auth.getMid())
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
        try
        {
            Connection conn = dataSource.getConnection();
            // 获取此视频信息
            String select_video = "select * from videos where bv = " + bv;
            PreparedStatement select_video_stmt = conn.prepareStatement(select_video);
            ResultSet video_info = select_video_stmt.executeQuery();
            // 验证更改者信息
            if (video_info.getLong("owner_id") == auth.getMid())
                return false;
            // 验证视频时长有无修改
            if (video_info.getLong("duration") != req.getDuration())
                return false;
            //todo
            return false;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> searchVideo(AuthInfo auth, String keywords, int pageSize, int pageNum)
    {
        List<String> res = new LinkedList<>();
        if (keywords.equals(" ") | pageNum <= 0 | pageSize <= 0)
            return null;
        try
        {
            Connection conn = dataSource.getConnection();
            String query1 = """
                    select bv from (
                    select  bv,(
                        select count(*)
                            from
                        (
                                (select *from key_word where a.description like key_word.column1)
                                union
                                (select *from key_word where a.title like key_word.column1)
                                union
                                (select *from key_word where b.name like key_word.column1)
                                                                                                )tmp
                        )  as rate,
                        (
                            select count(*)
                            from(select *from view
                                         where view.bv=a.bv)tmp2
                            ) as view_time

                    from videos a join users b on a.owner_mid=b.mid
                    order by rate desc ,view_time desc )tmp3
                    where rate>0;""";
            StringBuilder with_zone= new StringBuilder("""
                    with key_word as(
                        select distinct *
                        from (values""");
            String[] key_words=keywords.split(" ");
            for (int i=0;i<key_words.length;i++){
                if (i!=0)
                    with_zone.append(",");
                with_zone.append("('%"+key_words[i]+"%')");
            }
            with_zone.append(")as bieming114514)");
            query1=with_zone+query1;
            PreparedStatement query_1 = conn.prepareStatement(query1);
           // for (int i=0;i<key_words.length;i++)
           //     query_1.setString(i+1,key_words[i]);
            ResultSet resultSet1 = query_1.executeQuery();
            if (resultSet1.absolute(pageNum * pageNum))
                return null;
            else
            {
                int cou = 0;
                while (true)
                {
                    res.add(resultSet1.getString("bv"));
                    cou++;
                    if (cou == pageSize)
                        break;
                    if (!resultSet1.next())
                        break;
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public double getAverageViewRate(String bv)
    {
        double res;
        try
        {
            Connection conn = dataSource.getConnection();
            String query = """
                    select *from (
                                        select sum(time),count(*) from view where bv=?)tmp,
                   (SELECT duration FROM videos WHERE BV=?)tmp1
                    where tmp is not null
                    ;""";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1,bv);
            preparedStatement.setString(2,bv);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return -1;
            double tmp = resultSet.getFloat(1);
            int cou = resultSet.getInt(2);
            res = tmp / (cou * resultSet.getInt(3));
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public Set<Integer> getHotspot(String bv)
    {
        Set<Integer> res = new HashSet<>();
        try
        {
            Connection conn = dataSource.getConnection();
            String query2 = """
                    select time2 from (
                    select distinct time2,rank() over (order by cou desc ) as rank from (
                    select floor(time/10) as time2,count(*) over (partition by floor(time/10)) as cou from danmu where bv=?)tmp)tmp2
                    where rank=1::bigint;""";
            PreparedStatement preparedStatement2 = conn.prepareStatement(query2);
            preparedStatement2.setString(1,bv);
            ResultSet resultSet2 = preparedStatement2.executeQuery();
            resultSet2.next();
            while (resultSet2.next()){
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
        try
        {
            if (isAuthValid(auth,dataSource)==-1)
                return false;
            Connection conn = dataSource.getConnection();
            String query4 = "SELECT * FROM videos WHERE BV=?;";
            PreparedStatement preparedStatement4 = conn.prepareStatement(query4);
            preparedStatement4.setString(1, bv);
            ResultSet resultSet4 = preparedStatement4.executeQuery();
            if (!resultSet4.next() || resultSet4.getLong(1) == auth.getMid())
                return false;
            String query6="SELECT identity FROM USERS where mid= ?";
            PreparedStatement preparedStatement=conn.prepareStatement(query6);
            preparedStatement.setLong(1,auth.getMid());
            ResultSet resultSet=preparedStatement.executeQuery();
            if (!resultSet.next())
                return false;
            String query3 = "SELECT * FROM review WHERE BV=?;";
            PreparedStatement preparedStatement3 = conn.prepareStatement(query3);
            preparedStatement3.setLong(1, auth.getMid());
            ResultSet resultSet3 = preparedStatement3.executeQuery();
            if (!resultSet3.next())

            {
                String query5 = "INSERT INTO review(bv,reviewer_mid,review_time) values (?,?,?);";
                PreparedStatement preparedStatement5 = conn.prepareStatement(query5);
                preparedStatement5.setLong(2, auth.getMid());
                preparedStatement5.setString(1, bv);
                preparedStatement5.setTime(3, new Time(System.currentTimeMillis()));
                preparedStatement5.executeUpdate();
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
        String query4 = "SELECT * FROM videos WHERE BV = ? ;";
        try(Connection conn = dataSource.getConnection();)
        {
            if (isAuthValid(auth,dataSource)==-1)
                return false;
            PreparedStatement preparedStatement4 = conn.prepareStatement(query4);
            preparedStatement4.setString(1, bv);
            ResultSet resultSet4 = preparedStatement4.executeQuery();
            if (!resultSet4.next() || resultSet4.getLong(1) == auth.getMid())
                return false;

            List<String> res = searchVideo(auth, bv, 1, 1);

            if (res == null)
                return false;

            String query2 = "SELECT * FROM users WHERE MID=?;";
            PreparedStatement preparedStatement2 = conn.prepareStatement(query2);
            preparedStatement2.setLong(1, auth.getMid());
            ResultSet resultSet2 = preparedStatement2.executeQuery();
            if (resultSet2.getInt("coin") == 0)
                return false;

            String query3 = "SELECT * FROM coin WHERE MID=?;";
            PreparedStatement preparedStatement3 = conn.prepareStatement(query3);
            preparedStatement3.setLong(1, auth.getMid());
            ResultSet resultSet3 = preparedStatement3.executeQuery();
            if (!resultSet3.next())
                return false;

            String query5 = "INSERT INTO coin (bv, mid) values (?,?);";
            PreparedStatement preparedStatement5 = conn.prepareStatement(query5);
            preparedStatement5.setLong(2, auth.getMid());
            preparedStatement5.setString(1, bv);
            preparedStatement5.executeUpdate();
            return true;

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean likeVideo(AuthInfo auth, String bv)
    {
        try
        {
            if (isAuthValid(auth,dataSource)==-1)
                return false;
            Connection conn = dataSource.getConnection();

            String query4 = "SELECT * FROM videos WHERE BV=?;";
            PreparedStatement preparedStatement4 = conn.prepareStatement(query4);
            preparedStatement4.setString(1, bv);
            ResultSet resultSet4 = preparedStatement4.executeQuery();
            if (!resultSet4.next() || resultSet4.getLong(1) == auth.getMid())
                return false;

            List<String> res = searchVideo(auth, bv, 1, 1);
            if (res == null)
                return false;


            String query3 = "SELECT * FROM likes WHERE MID= ? ;";
            PreparedStatement preparedStatement3 = conn.prepareStatement(query3);
            preparedStatement3.setLong(1, auth.getMid());
            ResultSet resultSet3 = preparedStatement3.executeQuery();
            if (!resultSet3.next())

            {
                String query5 = "INSERT INTO likes (bv,mid) values (?,?);";
                PreparedStatement preparedStatement5 = conn.prepareStatement(query5);
                preparedStatement5.setLong(2, auth.getMid());
                preparedStatement5.setString(1, bv);
                preparedStatement5.executeUpdate();
                return true;
            }
            else
            {
                String query5 = "DELETE FROM likes where mid = ? and bv= ?;";
                PreparedStatement preparedStatement5 = conn.prepareStatement(query5);
                preparedStatement5.setLong(1, auth.getMid());
                preparedStatement5.setString(2, bv);
                preparedStatement5.executeUpdate();
                return false;
            }

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean collectVideo(AuthInfo auth, String bv)
    {
        try
        {
            if (isAuthValid(auth,dataSource)==-1)
                return false;
            Connection conn = dataSource.getConnection();

            String query4 = "SELECT * FROM videos WHERE BV=?;";
            PreparedStatement preparedStatement4 = conn.prepareStatement(query4);
            preparedStatement4.setString(1, bv);
            ResultSet resultSet4 = preparedStatement4.executeQuery();
            if (!resultSet4.next() || resultSet4.getLong(1) == auth.getMid())
                return false;

            List<String> res = searchVideo(auth, bv, 1, 1);
            if (res == null)
                return false;


            String query3 = "SELECT * FROM favorite WHERE MID = ?;";
            PreparedStatement preparedStatement3 = conn.prepareStatement(query3);
            preparedStatement3.setLong(1, auth.getMid());
            ResultSet resultSet3 = preparedStatement3.executeQuery();
            if (!resultSet3.next())

            {
                String query5 = "INSERT INTO favorite(bv,mid) values (?,?);";
                PreparedStatement preparedStatement5 = conn.prepareStatement(query5);
                preparedStatement5.setLong(2, auth.getMid());
                preparedStatement5.setString(1, bv);
                preparedStatement5.executeUpdate();
                return true;
            }
            else
            {
                String query5 = "DELETE FROM favorite where mid = ? and bv= ?;";
                PreparedStatement preparedStatement5 = conn.prepareStatement(query5);
                preparedStatement5.setLong(1, auth.getMid());
                preparedStatement5.setString(2, bv);
                preparedStatement5.executeUpdate();
                return false;
            }

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

}