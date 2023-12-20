package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;
import io.sustc.dto.VideoRecord;
import io.sustc.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class VideoServiceImp implements VideoService
{
    @Autowired
    private DataSource dataSource;

    public String postVideo(AuthInfo auth, PostVideoReq req)
    {
        LocalDateTime time = LocalDateTime.now();
        Timestamp current_time = new Timestamp(
                time.getYear(),
                time.getMonth().getValue(),
                time.getDayOfMonth(),
                time.getHour(),
                time.getMinute(),
                time.getSecond(),
                time.getNano());
        //long av = VideoRecord.getav();
        Random random = new Random();
        long av = random.nextInt(1000000000);
        String table = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF";
        int[] s = {11, 10, 3, 8, 4, 6};
        long xor = 177451812L;
        long add = 8728348608L;
        char[] bv_c = "BV1  4 1 7  ".toCharArray();
        av = (av ^ xor) + add;
        for (int i = 0; i < 6; i++)
        {
            bv_c[s[i]] = table.charAt((int) (av / Math.pow(58, i) % 58));
        }
        String bv = bv_c.toString();

        String sql = "insert into videos(bv, title, owner_id, commit_time, public_time, duration, description) values(?,?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, bv);
            stmt.setString(2, req.getTitle());
            stmt.setLong(3, auth.getMid());
            stmt.setTimestamp(4, current_time);
            stmt.setTimestamp(5, req.getPublicTime());
            stmt.setLong(6, (long) req.getDuration());
            stmt.setString(7, req.getDescription());
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return bv;
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
            if (user_info.next() && user_info.getString("identity") == "superuser")
            {
                String sql = "delete from videos where bv =" + bv;
                PreparedStatement stmt = conn.prepareStatement(sql);
                return stmt.executeUpdate() != -1;
            }

            // 判断是否是视频的上传者
            String select_video = "select owener_id from videos where bv = " + bv;
            PreparedStatement select_video_stmt = conn.prepareStatement(select_video);
            ResultSet video_info = select_video_stmt.executeQuery();
            if (video_info.next() && video_info.getLong("owener_id") == auth.getMid())
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
            if (video_info.getLong("owener_id") == auth.getMid())
                return false;
            //验证视频时长有无修改
            if (video_info.getLong("duration") != req.getDuration())
                return false;
            //todo
            return false;
        }
        catch (SQLException e)
        {

        }
        return false;
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
            String query1 = "SELECT * FROM VIDEOS join users on bv WHERE TITLE LIKE '%?%' or text LIKE '%?%' or name like '%?%';";
            PreparedStatement query_1 = conn.prepareStatement(query1);
            query_1.setString(1, keywords);
            ResultSet resultSet1 = query_1.executeQuery();
            if (resultSet1.absolute(pageNum * pageNum))
                return null;
            else
            {
                int cou = 0;
                while (true)
                {
                    res.add(resultSet1.getString("id"));
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
            String query1 = "SELECT * FROM videos WHERE BV=?;";
            String query2 = "SELECT * FROM view WHERE BV=?;";
            PreparedStatement preparedStatement1 = conn.prepareStatement(query1);
            PreparedStatement preparedStatement2 = conn.prepareStatement(query2);
            ResultSet resultSet1 = preparedStatement1.executeQuery();
            ResultSet resultSet2 = preparedStatement2.executeQuery();
            if (resultSet1 == null)
                return -1;
            if (resultSet2 == null)
                return -1;
            double tmp = 0;
            int cou = 0;
            while (true)
            {
                cou++;
                tmp += resultSet2.getFloat(1);
                if (!resultSet2.next())
                    break;
            }
            res = tmp / (cou * resultSet1.getInt(1));
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
            String query1 = "SELECT * FROM videos WHERE BV=?;";
            String query2 = "SELECT * FROM danmu WHERE BV=?;";
            PreparedStatement preparedStatement1 = conn.prepareStatement(query1);
            PreparedStatement preparedStatement2 = conn.prepareStatement(query2);
            ResultSet resultSet1 = preparedStatement1.executeQuery();
            ResultSet resultSet2 = preparedStatement2.executeQuery();
            if (resultSet1 == null)
                return null;
            if (resultSet2 == null)
                return null;
            int tmp;
           /* while (true){
                res.add()
                if ()
            }

            */
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


            Connection conn = dataSource.getConnection();

            String query4 = "SELECT * FROM videos WHERE BV=?;";
            PreparedStatement preparedStatement4 = conn.prepareStatement(query4);
            preparedStatement4.setString(1, bv);
            ResultSet resultSet4 = preparedStatement4.executeQuery();
            if (resultSet4 == null || resultSet4.getLong(1) == auth.getMid())
                return false;


            String query3 = "SELECT * FROM review WHERE BV=?;";
            PreparedStatement preparedStatement3 = conn.prepareStatement(query3);
            preparedStatement3.setLong(1, auth.getMid());
            ResultSet resultSet3 = preparedStatement3.executeQuery();
            if (resultSet3 == null)

            {
                String query5 = "INSERT INTO review(bv,reviewer_mid,review_time) value (?,?,?);";
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
        try
        {


            Connection conn = dataSource.getConnection();

            String query4 = "SELECT * FROM videos WHERE BV=?;";
            PreparedStatement preparedStatement4 = conn.prepareStatement(query4);
            preparedStatement4.setString(1, bv);
            ResultSet resultSet4 = preparedStatement4.executeQuery();
            if (resultSet4 == null || resultSet4.getLong(1) == auth.getMid())
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
            if (resultSet3 != null)
                return false;

            String query5 = "INSERT INTO coin(bv,mid) value (?,?);";
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


            Connection conn = dataSource.getConnection();

            String query4 = "SELECT * FROM videos WHERE BV=?;";
            PreparedStatement preparedStatement4 = conn.prepareStatement(query4);
            preparedStatement4.setString(1, bv);
            ResultSet resultSet4 = preparedStatement4.executeQuery();
            if (resultSet4 == null || resultSet4.getLong(1) == auth.getMid())
                return false;

            List<String> res = searchVideo(auth, bv, 1, 1);
            if (res == null)
                return false;


            String query3 = "SELECT * FROM like WHERE MID=?;";
            PreparedStatement preparedStatement3 = conn.prepareStatement(query3);
            preparedStatement3.setLong(1, auth.getMid());
            ResultSet resultSet3 = preparedStatement3.executeQuery();
            if (resultSet3 == null)

            {
                String query5 = "INSERT INTO like(bv,mid) value (?,?);";
                PreparedStatement preparedStatement5 = conn.prepareStatement(query5);
                preparedStatement5.setLong(2, auth.getMid());
                preparedStatement5.setString(1, bv);
                preparedStatement5.executeUpdate();
                return true;
            }
            else
            {
                String query5 = "DELETE FROM like where mid = ? and bv= ?);";
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


            Connection conn = dataSource.getConnection();

            String query4 = "SELECT * FROM videos WHERE BV=?;";
            PreparedStatement preparedStatement4 = conn.prepareStatement(query4);
            preparedStatement4.setString(1, bv);
            ResultSet resultSet4 = preparedStatement4.executeQuery();
            if (resultSet4 == null || resultSet4.getLong(1) == auth.getMid())
                return false;

            List<String> res = searchVideo(auth, bv, 1, 1);
            if (res == null)
                return false;


            String query3 = "SELECT * FROM collect WHERE MID=?;";
            PreparedStatement preparedStatement3 = conn.prepareStatement(query3);
            preparedStatement3.setLong(1, auth.getMid());
            ResultSet resultSet3 = preparedStatement3.executeQuery();
            if (resultSet3 == null)

            {
                String query5 = "INSERT INTO collect(bv,mid) value (?,?);";
                PreparedStatement preparedStatement5 = conn.prepareStatement(query5);
                preparedStatement5.setLong(2, auth.getMid());
                preparedStatement5.setString(1, bv);
                preparedStatement5.executeUpdate();
                return true;
            }
            else
            {
                String query5 = "DELETE FROM collect where mid = ? and bv= ?);";
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