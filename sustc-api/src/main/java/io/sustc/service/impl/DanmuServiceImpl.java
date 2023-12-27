package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;

import io.sustc.service.DanmuService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
class DanmuServiceImpl implements DanmuService
{
    @Autowired
    private DataSource dataSource;


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
                        return -1; // 视频不存在
                    if (videoResult.getTime("public_time") == null)

                        return -1; // 视频未发布
                    if (danmuTime > videoResult.getFloat("duration") || danmuTime < 0)
                        return -1; // 弹幕时间不合法
                }
            }

            // 检查用户是否已观看过该视频
            String viewerQuerySql = "SELECT * FROM view WHERE bv = ? and mid = ? ;";
            try (PreparedStatement viewerQuery = connection.prepareStatement(viewerQuerySql))
            {
                viewerQuery.setString(1, videoBv);
                viewerQuery.setLong(2, authMid);
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
        // 验证用户是否存在
        long authMid = UserServiceImpl.isAuthValid(auth, dataSource);

        // 检查auth信息是否非法
        if (authMid == -1) return false;

        try (Connection connection = dataSource.getConnection())
        {
            // 检查弹幕是否存在
            String bv;
            String danmuQuerySql = "SELECT bv FROM danmu WHERE id = ?;";
            try (PreparedStatement danmuQuery = connection.prepareStatement(danmuQuerySql))
            {
                danmuQuery.setLong(1, danmuId);
                try (ResultSet danmuResult = danmuQuery.executeQuery())
                {
                    if (!danmuResult.next()) return false; // 弹幕不存在
                    else bv = danmuResult.getString("bv");
                }
            }

            // 检查用户是否看过视频
            String watchedQuerySql = "SELECT count(*) FROM view WHERE mid = ? and bv = ? ;";
            try (PreparedStatement danmuQuery = connection.prepareStatement(watchedQuerySql))
            {
                danmuQuery.setLong(1, authMid);
                danmuQuery.setString(2, bv);
                try (ResultSet danmuResult = danmuQuery.executeQuery())
                {
                    if (danmuResult.next() && danmuResult.getInt(1) == 0)
                    {
                        return false; // 用户未看过视频
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Database error in likeDanmu: " + e.getMessage());
        }
        try (Connection connection = dataSource.getConnection())
        {
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
                            insertLikeStmt.setLong(2, authMid);
                            //log.info("insert danmu: {}", insertLikeStmt);
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
                            //log.info("delete danmu: {}", deleteLikeStmt);
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


}