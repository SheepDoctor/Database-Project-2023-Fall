package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.service.RecommenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommenderServiceImpl implements RecommenderService
{
    @Autowired
    private DataSource dataSource;

    @Override
    public List<String> recommendNextVideo(String bv)
    {
        List<String> str = new ArrayList<>();
        try (Connection conn = dataSource.getConnection())
        {
            String sql = """
                    select bv
                    from view
                    where bv != ?
                    group by bv
                    order by abs(count(*) - (select count(*)
                                          from view
                                          where bv = ?
                                          group by bv))
                    limit 5""";
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
        try (Connection conn = dataSource.getConnection())
        {
            String rank = """
                    select view.bv bvi
                    from (select bv, count(*) view_cnt
                          from view
                          group by bv) view
                             left join (select bv, count(*) like_cnt
                                        from likes
                                        group by bv) likes on view.bv = likes.bv
                             left join (select bv, count(*) fav_cnt
                                        from favorite
                                        group by bv) fav on fav.bv = view.bv
                             left join (select bv, count(*) danmu_cnt
                                        from danmu
                                        group by bv) danmu on danmu.bv = fav.bv
                             left join (select v.bv, avg(v.time / videos.duration) as avg_time_ratio
                                        from view v
                                                 join videos on v.bv = videos.bv
                                        group by v.bv) time on time.bv = danmu.bv
                    order by (like_cnt / view_cnt::float4 + fav_cnt / view_cnt::float4 + danmu_cnt / view_cnt::float4 +
                              avg_time_ratio) desc
                    limit ? offset ?""";
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
        if (UserServiceImpl.isAuthValid(auth, dataSource) == -1)
            return null;
        List<String> str = new ArrayList<>();
        try (Connection conn = dataSource.getConnection())
        {
            String sql = """
                    select view.bv bvi
                    from view
                             left join videos on view.bv = videos.bv
                             left join users on users.mid = videos.owner_mid
                    where view.mid in (select f1.follow_by_mid as friend
                                       from (select * from user_follow where follow_mid = ?) f1
                                                join (select * from user_follow where follow_by_mid = ?) f2
                                                     on f1.follow_by_mid = f2.follow_mid
                                       where f1.follow_mid != f1.follow_by_mid)
                    group by view.bv, users.level, videos.commit_time
                    order by count(*) desc , users.level desc, videos.commit_time desc
                    limit ? offset ?""";
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
        if (UserServiceImpl.isAuthValid(auth, dataSource) == -1)
            return null;
        List<Long> str = new ArrayList<>();
        try (Connection conn = dataSource.getConnection())
        {

            String sql = """
                    select follow_mid
                    from user_follow left join users on user_follow.follow_mid = users.mid
                    where follow_by_mid in (select follow_by_mid
                                            from user_follow
                                            where follow_mid = ?)
                      and follow_mid not in (select f1.follow_by_mid as friend
                                             from (select * from user_follow where follow_mid = ?) f1
                                                      join (select * from user_follow where follow_by_mid = ?) f2
                                                           on f1.follow_by_mid = f2.follow_mid
                                             where f1.follow_mid != f1.follow_by_mid)
                      and follow_mid != ?
                    group by follow_mid, users.level
                    order by count(*) desc, users.level desc 
                    limit ? offset ?""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, auth.getMid());
            stmt.setLong(2, auth.getMid());
            stmt.setLong(3, auth.getMid());
            stmt.setLong(4, auth.getMid());
            stmt.setInt(5, pageSize);
            stmt.setInt(6, pageNum);
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
