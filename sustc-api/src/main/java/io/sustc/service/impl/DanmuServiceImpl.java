package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;

import io.sustc.service.DanmuService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
class DanmuServiceImpl implements DanmuService
{
    @Autowired
    private DataSource dataSource;

    /*public long sendDanmu(AuthInfo auth, String bv, String content, float time)
    {
        if (content.length() == 0)
            return -1;

        try (Connection connection = dataSource.getConnection())
        {
            // 执行数据库操作，使用connection对象
            String query1 = "SELECT * FROM videos WHERE bv = ?;";
            PreparedStatement query = connection.prepareStatement(query1);
            query.setString(1, bv);
            ResultSet videoRecord = query.executeQuery();
            if (videoRecord == null)
                return -1;
            if (content.equals(" ") || content == null)
                return -1;
            if (videoRecord.getTime("public_time") == null)
                return -1;
            String query2 = "SELECT * FROM view WHERE bv = ?;";
            PreparedStatement query2_ = connection.prepareStatement(query2);
            query2_.setString(1, bv);
            ResultSet viewerRecord = query2_.executeQuery();
            if (viewerRecord == null)
                return -1;
            String query3 = "INSERT INTO danmu(BV, MID, TIME, CONTENT, POST_TIME,ID) VALUES (?, ?, ?, ?, ?, ?);";
            String query4 = "SELECT COUNT(*) FROM danmu;";
            PreparedStatement query4_ = connection.prepareStatement(query4);
            PreparedStatement query3_ = connection.prepareStatement(query3);
            ResultSet q4 = query4_.executeQuery();
            query4_.execute();
            query3_.setString(1, bv);
            query3_.setLong(2, auth.getMid());
            query3_.setTime(3, new Time((long) time));
            query3_.setString(4, content);
            query3_.setTime(5, new Time(System.currentTimeMillis()));
            query3_.setBigDecimal(6, BigDecimal.valueOf(q4.getInt(1)));
            query3_.executeUpdate();
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
        if (timeEnd <= timeEnd | timeEnd < 0 | timeStart < 0)
            return null;
        try (Connection connection = dataSource.getConnection())
        {
            String query1 = "SELECT * FROM videos WHERE bv = ?;";
            PreparedStatement query = connection.prepareStatement(query1);
            query.setString(1, bv);
            ResultSet videoRecord = query.executeQuery();
            if (videoRecord == null)
                return null;
            if (videoRecord.getTime("commit_time") == null)
                return null;
            if (timeEnd > videoRecord.getInt("duration"))
                return null;
            String query2 = "SELECT * FROM danmu WHERE bv = ? and time between ? and ?;";
            PreparedStatement query2_ = connection.prepareStatement(query2);
            query2_.setString(1, bv);
            query2_.setTime(2, new Time((long) timeStart));
            query2_.setTime(3, new Time((long) timeEnd));
            ResultSet danmuRecord = query2_.executeQuery();
            danmuRecord.last();
            int rows = danmuRecord.getRow();
            danmuRecord.first();
            for (int i = 0; i < rows; i++)
            {
                boolean flag = true;
                if (filter)
                {
                    String cur_content = danmuRecord.getString("content");
                    danmuRecord.first();
                    for (int j = 0; j <= i; j++)
                    {
                        if (j != i)
                        {
                            if (danmuRecord.getString("content").equals(cur_content))
                            {
                                flag = false;
                                break;
                            }
                        }
                        danmuRecord.next();
                    }
                }
                if (flag)
                    res.add(danmuRecord.getTime("time").getTime());
                danmuRecord.next();
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return res;
    }*/

    public long sendDanmu(AuthInfo auth, String videoBv, String danmuContent, float danmuTime)
    {
        long authMid = UserServiceImpl.isAuthValid(auth, dataSource);

        // 检查auth信息是否非法
        if (authMid == -1) return -1;

        // 检查弹幕内容是否为空
        if (danmuContent == null || danmuContent.trim().isEmpty())
        {
            return -1;
        }

        try (Connection connection = dataSource.getConnection())
        {
            // 查询视频信息以确认视频存在
            String videoQuerySql = "SELECT * FROM videos WHERE bv = ?;";
            try (PreparedStatement videoQuery = connection.prepareStatement(videoQuerySql))
            {
                videoQuery.setString(1, videoBv);
                try (ResultSet videoResult = videoQuery.executeQuery())
                {
                    if (!videoResult.next())
                    {
                        return -1; // 视频不存在
                    }
                    if (videoResult.getTime("public_time") == null)
                    {
                        return -1; // 视频未发布
                    }
                }
            }

            // 检查用户是否已观看过该视频
            String viewerQuerySql = "SELECT * FROM view WHERE bv = ? and mid=?;";
            try (PreparedStatement viewerQuery = connection.prepareStatement(viewerQuerySql))
            {
                viewerQuery.setString(1, videoBv);
                viewerQuery.setLong(2,authMid);
                try (ResultSet viewerResult = viewerQuery.executeQuery())
                {
                    if (!viewerResult.next())
                    {
                        return -1; // 用户未观看该视频
                    }
                }
            }

            // 插入新弹幕
            String danmuInsertSql = "INSERT INTO danmu (BV, MID, TIME, CONTENT, POST_TIME) " +
                    "VALUES (?, ?, ?, ?, ?) returning id;";
            try (PreparedStatement danmuInsert = connection.prepareStatement(danmuInsertSql))
            {
                danmuInsert.setString(1, videoBv);
                danmuInsert.setLong(2, authMid);
                danmuInsert.setFloat(3, danmuTime);
                danmuInsert.setString(4, danmuContent);
                danmuInsert.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                try (ResultSet resultSetInsert = danmuInsert.executeQuery())
                {
                    if (resultSetInsert.next())
                    {
                        return resultSetInsert.getLong(1); // 返回生成的自增主键
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1; // 发生异常时返回 -1
        }
        return -1; // 返回 -1
    }


    @Override
    public List<Long> displayDanmu(String bv, float startTime, float endTime, boolean filter)
    {
        List<Long> danmuIDs = new ArrayList<>();
        // 检查时间条件是否有效
        if (endTime <= startTime || endTime < 0 || startTime < 0)
        {
            return null;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement videoQuery = connection.prepareStatement(
                     "SELECT commit_time, duration FROM videos WHERE bv = ?"))
        {

            videoQuery.setString(1, bv);
            try (ResultSet videoResult = videoQuery.executeQuery())
            {
                if (videoResult == null || !videoResult.next() || videoResult.getTime("commit_time") == null)
                {
                    return null;
                }
                if (endTime > videoResult.getInt("duration"))
                {
                    return null;
                }
            }

            try (PreparedStatement danmuQuery = connection.prepareStatement(
                    "SELECT id, content FROM danmu WHERE bv = ? AND time BETWEEN ? AND ?"))
            {

                danmuQuery.setString(1, bv);
                danmuQuery.setFloat(2, startTime);
                danmuQuery.setFloat(3, endTime);
                try (ResultSet danmuResult = danmuQuery.executeQuery())
                {
                    Set<String> seenContents = new HashSet<>();
                    while (danmuResult.next())
                    {
                        String content = danmuResult.getString("content");
                        if (!filter || seenContents.add(content))
                        {
                            danmuIDs.add(danmuResult.getLong("id"));
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
        return danmuIDs;
    }

    @Override
    public boolean likeDanmu(AuthInfo auth, long danmuId)
    {
        try (Connection connection = dataSource.getConnection())
        {
            // 验证用户是否存在
            long authMid = UserServiceImpl.isAuthValid(auth, dataSource);

            // 检查auth信息是否非法
            if (authMid == -1) return false;

            // 检查弹幕是否存在
            String danmuQuerySql = "SELECT * FROM danmu WHERE id = ?;";
            try (PreparedStatement danmuQuery = connection.prepareStatement(danmuQuerySql))
            {
                danmuQuery.setLong(1, danmuId);
                try (ResultSet danmuResult = danmuQuery.executeQuery())
                {
                    if (!danmuResult.next())
                    {
                        return false; // 弹幕不存在
                    }
                }
            }

            // 检查用户是否已经点赞
            String likeCheckSql = "SELECT * FROM danmu_likes WHERE id = ? AND mid = ?;";
            try (PreparedStatement likeCheckQuery = connection.prepareStatement(likeCheckSql))
            {
                likeCheckQuery.setLong(1, danmuId);
                likeCheckQuery.setLong(2, authMid);
                try (ResultSet likeCheckResult = likeCheckQuery.executeQuery())
                {
                    if (!likeCheckResult.next())
                    {
                        // 如果用户尚未点赞，添加点赞
                        String insertLikeSql = "INSERT INTO danmu_likes (id, mid) VALUES (?, ?);";
                        try (PreparedStatement insertLikeStmt = connection.prepareStatement(insertLikeSql))
                        {
                            insertLikeStmt.setLong(1, danmuId);
                            insertLikeStmt.setLong(2,authMid);
                            insertLikeStmt.executeUpdate();
                            return true; // 点赞成功
                        }
                    }
                    else
                    {
                        // 如果用户已经点赞，取消点赞
                        String deleteLikeSql = "DELETE FROM danmu_likes WHERE id = ? AND mid = ?;";
                        try (PreparedStatement deleteLikeStmt = connection.prepareStatement(deleteLikeSql))
                        {
                            deleteLikeStmt.setLong(1, danmuId);
                            deleteLikeStmt.setLong(2, authMid);
                            deleteLikeStmt.executeUpdate();
                            return false; // 取消点赞成功
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Database error in likeDanmu: " + e.getMessage());
        }
    }

    /*public boolean likeDanmu(AuthInfo auth, long id)
    {
        try (Connection connection = dataSource.getConnection())
        {
            String query1 = "SELECT * FROM users WHERE bv = ?";
            PreparedStatement query = connection.prepareStatement(query1);
            query.setLong(1, auth.getMid());
            ResultSet userService = query.executeQuery();
            if (userService == null)
                return false;

            String query2 = "SELECT * FROM danmu WHERE id = ? ";
            PreparedStatement query2_ = connection.prepareStatement(query2);
            query2_.setLong(1, id);
            ResultSet danmuRecord = query2_.executeQuery();
            if (danmuRecord == null)
                return false;

            String q3 = "SELECT * FROM danmu_likes where id = ? and mid = ?";
            PreparedStatement q3_ = connection.prepareStatement(q3);
            q3_.setLong(1, id);
            q3_.setLong(2, auth.getMid());
            ResultSet resultSet3 = q3_.executeQuery();
            if (resultSet3 == null)
            {
                String up = "INSERT INTO danmu_likes(id,mid) values(?,?);";
                PreparedStatement preparedStatement4 = connection.prepareStatement(up);
                preparedStatement4.setLong(1, id);
                preparedStatement4.setLong(2, auth.getMid());
                preparedStatement4.executeUpdate();
                return true;
            }
            else
            {
                String up = "DELETE FROM danmu_likes where id= ? and mid = ? ;";
                PreparedStatement preparedStatement4 = connection.prepareStatement(up);
                preparedStatement4.setLong(1, id);
                preparedStatement4.setLong(2, auth.getMid());
                preparedStatement4.executeUpdate();
                return false;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }*/

}