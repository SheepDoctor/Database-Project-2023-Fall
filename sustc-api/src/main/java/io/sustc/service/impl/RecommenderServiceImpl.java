package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.service.RecommenderService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecommenderServiceImpl implements RecommenderService
{
    @Autowired
    private DataSource dataSource;

    @Override
    public List<String> recommendNextVideo(String bv)
    {
        List<String> str = new ArrayList<>();
        try
        {
            Connection conn = dataSource.getConnection();
            String sql = "select bv\n" +
                    "from view\n" +
                    "where bv != ?\n" +
                    "group by bv\n" +
                    "order by abs(count(*) - (select count(*)\n" +
                    "                      from view\n" +
                    "                      where bv = ?\n" +
                    "                      group by bv))\n" +
                    "limit 5";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, bv);
            stmt.setString(2, bv);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                str.add(rs.getString("bv"));
            }
            return str;
        }
        catch (SQLException e)
        {
            System.out.println("recommendNextVideo: " + e);
            return str;
        }
    }

    @Override
    public List<String> generalRecommendations(int pageSize, int pageNum)
    {
        List<String> str = new ArrayList<>();
        try
        {
            Connection conn = dataSource.getConnection();
            String rank = "select view.bv bvi\n" +
                    "from (select bv, count(*) view_cnt\n" +
                    "      from view\n" +
                    "      group by bv) view\n" +
                    "         left join (select bv, count(*) like_cnt\n" +
                    "                    from likes\n" +
                    "                    group by bv) likes on view.bv = likes.bv\n" +
                    "         left join (select bv, count(*) fav_cnt\n" +
                    "                    from favourites\n" +
                    "                    group by bv) fav on fav.bv = view.bv\n" +
                    "         left join (select bv, count(*) comment_cnt\n" +
                    "                    from comment\n" +
                    "                    group by bv) comment on comment.bv = fav.bv\n" +
                    "         left join (select v.bv, avg(v.time / videos.duration) as avg_time_ratio\n" +
                    "                    from view v\n" +
                    "                             join videos on v.bv = videos.bv\n" +
                    "                    group by v.bv) time on time.bv = comment.bv\n" +
                    "order by (like_cnt / view_cnt::float4 + fav_cnt / view_cnt::float4 + comment_cnt / view_cnt::float4 +\n" +
                    "          avg_time_ratio) desc\n" +
                    "limit ? offset ?";
            PreparedStatement stmt = conn.prepareStatement(rank);
            stmt.setInt(1, pageSize);
            stmt.setInt(2, pageNum);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                str.add(rs.getString("bvi"));
            }
            return str;
        }
        catch (SQLException e)
        {
            System.out.println("generalRecommendations: " + e);
            return str;
        }
    }

    @Override
    public List<String> recommendVideosForUser(AuthInfo auth, int pageSize, int pageNum)
    {
        List<String> str = new ArrayList<>();
        try
        {
            Connection conn = dataSource.getConnection();
            String sql = "select view.bv bvi\n" +
                    "from view\n" +
                    "         left join videos on view.bv = videos.bv\n" +
                    "         left join users on users.mid = videos.owner_id\n" +
                    "where view.mid in (select f1.follow_by_mid as friend\n" +
                    "                   from (select * from follow where follow_mid = ?) f1\n" +
                    "                            join (select * from follow where follow_by_mid = ?) f2\n" +
                    "                                 on f1.follow_by_mid = f2.follow_mid\n" +
                    "                   where f1.follow_mid != f1.follow_by_mid)\n" +
                    "group by view.bv, users.level, videos.commit_time\n" +
                    "order by count(*) desc , users.level desc, videos.commit_time desc\n" +
                    "limit ? offset ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, auth.getMid());
            stmt.setLong(2, auth.getMid());
            stmt.setInt(3, pageSize);
            stmt.setInt(4, pageNum);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                str.add(rs.getString("bvi"));
            }
            return str;
        }
        catch (SQLException e)
        {
            System.out.println("recommendVideosForUser: " + e);
            return str;
        }
    }

    @Override
    public List<Long> recommendFriends(AuthInfo auth, int pageSize, int pageNum)
    {
        List<Long> str = new ArrayList<>();
        try
        {
            Connection conn = dataSource.getConnection();
            String sql = "select follow_mid\n" +
                    "from follow left join users on follow.follow_mid = users.mid\n" +
                    "where follow_by_mid in (select follow_by_mid\n" +
                    "                        from follow\n" +
                    "                        where follow_mid = ?)\n" +
                    "  and follow_mid not in (select f1.follow_by_mid as friend\n" +
                    "                         from (select * from follow where follow_mid = ?) f1\n" +
                    "                                  join (select * from follow where follow_by_mid = 3610) f2\n" +
                    "                                       on f1.follow_by_mid = f2.follow_mid\n" +
                    "                         where f1.follow_mid != f1.follow_by_mid)\n" +
                    "  and follow_mid != ?\n" +
                    "group by follow_mid, users.level\n" +
                    "order by count(*) desc, users.level desc" +
                    "limit ? offset ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, auth.getMid());
            stmt.setLong(2, auth.getMid());
            stmt.setInt(3, pageSize);
            stmt.setInt(4, pageNum);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                str.add(rs.getLong("follow_mid"));
            }
            return str;
        }
        catch (SQLException e)
        {
            System.out.println("recommendFriends" + e);
            return str;
        }
    }
}
