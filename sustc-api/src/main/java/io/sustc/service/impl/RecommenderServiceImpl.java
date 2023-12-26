package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.service.RecommenderService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
                    select v2.bv bv
                    from (select bv, mid from view where bv = ?) t1
                             inner join (select * from view where bv != ?) v2 on t1.mid = v2.mid
                    group by v2.bv
                    order by count(v2.mid) desc
                    limit 5;""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, bv);
            stmt.setString(2, bv);
            ResultSet rs = stmt.executeQuery();
            //log.info("SQL: {}", stmt);
            while (rs.next())
            {
                str.add(rs.getString("bv"));
            }
            conn.close();
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
        if (pageNum <= 0 || pageSize <= 0)
        {
            return str;
        }
        try (Connection conn = dataSource.getConnection())
        {
            String rank = """
                    select view.bv                                        bv,
                           (coalesce(like_cnt, 0) / view_cnt::float4 * 100 + coalesce(fav_cnt, 0) / view_cnt::float4 * 100 +
                            coalesce(coin_cnt, 0) / view_cnt::float4 * 100 + coalesce(danmu_cnt, 0) / view_cnt::float4 +
                            coalesce(avg_time_ratio * 100, 0)) as         score
                    from (select bv, count(bv) as view_cnt
                          from view
                          group by bv) view
                             left join (select bv, count(bv) as like_cnt from likes group by bv) likes on view.bv = likes.bv
                             left join (select bv, count(bv) as fav_cnt from favorite group by bv) fav on fav.bv = view.bv
                             left join (select bv, count(bv) as coin_cnt from coin group by bv) coin on coin.bv = view.bv
                             left join (select bv, count(bv) as danmu_cnt from danmu group by bv) danmu on danmu.bv = view.bv
                             left join (select v.bv, avg(v.time / videos.duration) as avg_time_ratio
                                        from view v
                                                 left join videos on v.bv = videos.bv
                                        group by v.bv) time on time.bv = view.bv
                    order by score desc
                    limit ? offset ? ;""";
            PreparedStatement stmt = conn.prepareStatement(rank);
            stmt.setInt(1, pageSize);
            stmt.setInt(2, pageSize * (pageNum - 1));
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                str.add(rs.getString("bv"));
            }
            //log.info("SQL: {}", stmt);
            conn.close();
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
        if (pageNum <= 0 || pageSize <= 0)
        {
            return str;
        }
        long mid = UserServiceImpl.isAuthValid(auth, dataSource);
        if (mid == -1)
            return str;
        try (Connection conn = dataSource.getConnection())
        {
            String sql = """
                    select view.bv bv
                    from view
                             left join videos on view.bv = videos.bv
                             left join users on users.mid = videos.owner_mid
                    where view.mid in (select f1.follow_by_mid as friend
                                       from (select * from user_follow where follow_mid = ?) f1
                                                join (select * from user_follow where follow_by_mid = ?) f2
                                                     on f1.follow_by_mid = f2.follow_mid
                                       where f1.follow_mid != f1.follow_by_mid)
                    and view.bv not in (select bv from view where mid = ?)
                    group by view.bv, users.level, videos.commit_time
                    order by count(*) desc, users.level desc, videos.commit_time desc
                    limit ? offset ?""";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, mid);
            stmt.setLong(2, mid);
            stmt.setLong(3, mid);
            stmt.setInt(4, pageSize);
            stmt.setInt(5, pageSize * (pageNum - 1));
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                str.add(rs.getString("bv"));
            }
            sql = """
                    select count(f1.follow_by_mid) as cnt
                                       from (select * from user_follow where follow_mid = ?) f1
                                                join (select * from user_follow where follow_by_mid = ?) f2
                                                     on f1.follow_by_mid = f2.follow_mid
                                       where f1.follow_mid != f1.follow_by_mid
                    """;
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, mid);
            stmt.setLong(2, mid);
            rs = stmt.executeQuery();
            if (rs.next())
            {
                if (rs.getInt("cnt") == 0)
                {
                    conn.close();
                    return generalRecommendations(pageSize, pageNum);
                }
            }
            //log.info("SQL: {}", stmt);
            conn.close();
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
        if (pageNum <= 0 || pageSize <= 0)
        {
            return str;
        }
        long mid = UserServiceImpl.isAuthValid(auth, dataSource);
        if (mid == -1)
            return null;
        try (Connection conn = dataSource.getConnection())
        {
            String sql = """
                    select follow_mid, count(*), users.level
                    from user_follow
                             left join users on user_follow.follow_mid = users.mid
                    where follow_by_mid in (select follow_by_mid
                                            from user_follow
                                            where follow_mid = ?)
                                        
                      and follow_mid not in (select follow_by_mid from user_follow where follow_mid = ?) --不是朋友
                      and follow_mid != ?
                    group by follow_mid, users.level
                    order by count(*) desc, users.level desc, follow_mid
                    limit ? offset ?;
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, mid);
            stmt.setLong(2, mid);
            stmt.setLong(3, mid);
            stmt.setInt(4, pageSize);
            stmt.setInt(5, pageSize * (pageNum - 1));
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                str.add(rs.getLong("follow_mid"));
            }
            conn.close();
            return str;
        }
        catch (SQLException e)
        {
            System.out.println("recommendFriends" + e);
            return str;
        }
    }
}