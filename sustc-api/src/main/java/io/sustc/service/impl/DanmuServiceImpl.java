package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.DanmuRecord;
import io.sustc.dto.VideoRecord;
import io.sustc.service.DanmuService;
import io.sustc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

class DanmuServiceImpl implements DanmuService
{
    @Autowired
    private DataSource dataSource;

    public long sendDanmu(AuthInfo auth, String bv, String content, float time)
    {
        if (content.length() == 0)
            return -1;

        try (Connection connection = dataSource.getConnection())
        {
            // 执行数据库操作，使用connection对象
            String query1 = "SELECT * FROM videos WHERE bv = ?";
            PreparedStatement query = connection.prepareStatement(query1);
            query.setString(1, bv);
            VideoRecord videoRecord = (VideoRecord) query.executeQuery(query1);
            if (videoRecord == null)
                return -1;
            boolean flag = false;
            for (int i = 0; i < videoRecord.getViewerMids().length; i++)
                if (videoRecord.getViewerMids()[i] == Long.parseLong(bv))
                    flag = true;
            if (!flag)
                return -1;
            String update1 = "INSERT INTO DANMU(BV,MID,TIME) values(?,?,?)";
            PreparedStatement update = connection.prepareStatement(update1);
            update.setString(1, bv);
            update.setLong(2, auth.getMid());
            update.setTime(3, new Time((long) time));

        }
        catch (SQLException e)
        {
            e.printStackTrace(); // 实际开发中请处理异常逻辑
        }
        return auth.getMid();
    }

    public List<Long> displayDanmu(String bv, float timeStart, float timeEnd, boolean filter)
    {
        List<Long> res = new ArrayList<>();
        if (timeEnd < timeEnd | timeEnd < 0 | timeStart < 0)
            return null;
        try (Connection connection = dataSource.getConnection())
        {
            String query1 = "SELECT * FROM videos WHERE bv = ?";
            PreparedStatement query = connection.prepareStatement(query1);
            query.setString(1, bv);
            VideoRecord videoRecord = (VideoRecord) query.executeQuery(query1);
            if (videoRecord == null)
                return null;
            if (videoRecord.getPublicTime().after(new Time(System.currentTimeMillis())))
                return null;
            if (timeEnd > videoRecord.getDuration())
                return null;
            String query2 = "SELECT * FROM danmu WHERE bv = ? and time between ? and ?";
            PreparedStatement query2_ = connection.prepareStatement(query2);
            query2_.setString(1, bv);
            query2_.setTime(2, new Time((long) timeStart));
            query2_.setTime(3, new Time((long) timeEnd));
            DanmuRecord danmuRecord = (DanmuRecord) query2_.executeQuery();
            res.add(danmuRecord.getMid());
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return res;
    }

    public boolean likeDanmu(AuthInfo auth, long id)
    {
        boolean res = false;
        try (Connection connection = dataSource.getConnection())
        {
            String query1 = "SELECT * FROM users WHERE bv = ?";
            PreparedStatement query = connection.prepareStatement(query1);
            query.setLong(1, auth.getMid());
            UserService userService = (UserService) query.executeQuery(query1);
            if (userService == null)
                return false;
            String query2 = "SELECT * FROM danmu WHERE id = ? ";
            PreparedStatement query2_ = connection.prepareStatement(query2);
            query2_.setLong(1, id);
            DanmuRecord danmuRecord = (DanmuRecord) query2_.executeQuery();
            if (danmuRecord == null)
                return false;
            int index = 0;
            for (int i = 0; i < danmuRecord.getLikedBy().length; i++)
            {
                index = i;
                if (danmuRecord.getLikedBy()[i] == auth.getMid())
                {
                    res = false;
                    break;
                }
                if (danmuRecord.getLikedBy()[i] == 0)
                {
                    res = true;
                    break;
                }
            }
            String update;
            if (res)
            {
                update = "insert into DANMU (Mid,id) values(?,?)";
            }
            else
                update = "DELETE FROM DANMU WHERE ID=? and MID=?";
            PreparedStatement update1 = connection.prepareStatement(update);
            update1.setLong(1, auth.getMid());
            update1.setLong(2, id);
            update1.execute();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return res;
    }

}