package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;
import io.sustc.dto.VideoRecord;
import io.sustc.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
        long av = VideoRecord.getav();
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
            stmt.setLong(6, req.getDuration());
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
        return null;
    }

    @Override
    public double getAverageViewRate(String bv)
    {
        return 0;
    }

    @Override
    public Set<Integer> getHotspot(String bv)
    {
        return null;
    }

    @Override
    public boolean reviewVideo(AuthInfo auth, String bv)
    {
        return false;
    }

    @Override
    public boolean coinVideo(AuthInfo auth, String bv)
    {
        return false;
    }

    @Override
    public boolean likeVideo(AuthInfo auth, String bv)
    {
        return false;
    }

    @Override
    public boolean collectVideo(AuthInfo auth, String bv)
    {
        return false;
    }
}