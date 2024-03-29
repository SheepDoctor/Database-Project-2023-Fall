package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.RegisterUserReq;
import io.sustc.dto.UserInfoResp;
import io.sustc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class UserServiceImpl implements UserService
{
    @Autowired
    private DataSource dataSource;

    public static long isAuthValid(AuthInfo auth, DataSource dataSource)
    {

        //qq和wechat都为空的时候使用mid
        String type = "mid";

        //有微信用微信
        if (!(auth.getWechat() == null) && !auth.getWechat().trim().equals(""))
        {
            type = "wechat";
        }
        //有qq用qq
        if (!(auth.getQq() == null) && !auth.getQq().trim().equals(""))
        {
            type = "qq";
        }

        String sqlSelectAuth = "select mid from users where " + type + " = ?";
        String sqlCheckMidAndPassWord = "select password from users where mid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmtWechatAndQq = conn.prepareStatement(sqlSelectAuth);
             PreparedStatement stmtMid = conn.prepareStatement(sqlCheckMidAndPassWord)
        )
        {
            switch (type)
            {
                case "wechat", "qq" ->
                {
                    // 对于 wechat 和 qq 登录，只需检查数据库中是否存在对应记录
                    String authValue = type.equals("wechat") ? auth.getWechat() : auth.getQq();
                    stmtWechatAndQq.setString(1, authValue);
                    try (ResultSet resultSet = stmtWechatAndQq.executeQuery();)
                    {
                        if (resultSet.next())
                        {
                            long ans = resultSet.getLong(1);
                            conn.close();
                            return ans; // 如果找到记录，返回对应的 mid
                        }
                    }
                }
                default ->
                {
                    // 对于 mid 登录，需要检查 密码的哈希值是否匹配
                    stmtMid.setLong(1, auth.getMid());
                    try (ResultSet resultSet = stmtMid.executeQuery();
                    )
                    {
                        if (resultSet.next() &&
                                checkSha256Hash(auth.getPassword(), resultSet.getString("password")))
                            return auth.getMid(); // 如果密码的哈希值匹配，返回 mid
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    public static String generateSha256Hash(String password) throws NoSuchAlgorithmException
    {
        // 创建 SHA-256 摘要实例
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // 执行哈希计算
        byte[] hashBytes = digest.digest(password.getBytes());

        // 将字节数组转换为十六进制格式
        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hashBytes)
        {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1)
            {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // 检查给定密码的 SHA-256 哈希是否与预期哈希匹配
    public static boolean checkSha256Hash(String password, String expectedHash) throws NoSuchAlgorithmException
    {
        // 比较生成的哈希与预期哈希
        return generateSha256Hash(password).equals(expectedHash);
    }

    @Override
    public long register(RegisterUserReq req)
    {
        // 检查必要字段是否为空
        if (req.getPassword() == null || req.getName() == null || req.getSex() == null)
        {
            return -1;
        }

        //System.out.println("********************************************");
        //System.out.println(req.getSex());
        // 验证生日格式
        if (req.getBirthday() != null && !isValidBirthday(req.getBirthday()))
        {
            return -1;
        }

        // 检查用户名、QQ、微信的唯一性（这通常需要数据库支持）
        if (!isUnique(req.getName(), req.getQq(), req.getWechat()))
        {
            return -1;
        }

        return createUser(req);
    }

    // 验证生日格式
    private boolean isValidBirthday(String birthday)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        sdf.setLenient(false); // 设置为严格的日期解析，不允许日期溢出（例如2月30日）
        birthday = "2024年" + birthday;
        //System.out.println(birthday);
        //System.out.println("********************************************");
        try
        {
            // 尝试解析日期
            Date date = sdf.parse(birthday);
            return true;
        }
        catch (ParseException e)
        {
            // 解析失败，说明日期无效
            //e.printStackTrace();
            return false;
        }
    }

    // 检查唯一性
    private boolean isUnique(String name, String qq, String wechat)
    {
        String sql;
        if (qq == null && wechat == null)
        {
            sql = "SELECT COUNT(*) cnt FROM users WHERE name = '" + name + "'";
        }
        else if (qq != null && wechat == null)
        {
            sql = "SELECT COUNT(*) cnt FROM users WHERE name = '" + name + "' OR qq = '" + qq + "'";
        }
        else if (qq == null && wechat != null)
        {
            sql = "SELECT COUNT(*) cnt FROM users WHERE name = '" + name + "' OR wechat = '" + wechat + "'";
        }
        else
        {
            sql = "SELECT COUNT(*) cnt FROM users WHERE name = '" + name + "' OR qq = '" + qq + "' OR wechat = '" + wechat + "'";
        }
        //System.out.println(sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // 执行查询
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                // 如果计数为0，则表示唯一
                if (rs.getInt("cnt") == 0)
                {
                    conn.close();
                    return true;
                }
            }
        }
        catch (SQLException e)
        {
            System.out.println(e);
        }
        return false;
    }

    // 创建新用户
    private long createUser(RegisterUserReq req)
    {
        // SQL 语句插入用户数据并返回生成的主键
        String sql = "INSERT INTO users (name, sex, birthday, sign, qq, wechat, password) " +
                "VALUES ( ?, CAST( ? AS gender_type), ?, ?, ?, ?, ?) RETURNING mid";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            String sex = switch (req.getSex().name())
                    {
                        case "MALE" -> "男";
                        case "FEMALE" -> "女";
                        default -> "保密";
                    };
            // 设置 PreparedStatement 参数
            stmt.setString(1, req.getName());
            stmt.setString(2, sex);
            stmt.setString(3, req.getBirthday());
            stmt.setString(4, req.getSign());
            stmt.setString(5, req.getQq());
            stmt.setString(6, req.getWechat());
            stmt.setString(7, req.getPassword());
            //System.out.println("******************************************");
            //System.out.println(stmt);
            long mid = -1;
            {
                while (mid == -1)
                {    // 执行查询并获取返回的主键
                    try (ResultSet rs = stmt.executeQuery())
                    {
                        if (rs.next())
                        {
                            mid = rs.getLong("mid");
                            //System.out.println(mid);
                            String check = "select count(*) cnt from users where mid=" + mid;
                            try (PreparedStatement checkstmt = conn.prepareStatement(check);
                                 ResultSet crs = checkstmt.executeQuery())
                            {
                                if (crs.next())
                                {
                                    int temp = crs.getInt("cnt");
                                    //System.out.println(temp);
                                    if (temp == 0)
                                    {
                                        mid = -1;
                                        continue;
                                    }
                                    else
                                    {
                                        //System.out.println(mid);
                                        //System.out.println("******************************************");
                                        return mid;// 返回生成的用户ID
                                    }
                                }
                                mid = -1;
                            }
                        }
                    }
                    catch (SQLException e)
                    {
                        //System.out.println(e);
                        mid = -1;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            System.out.println(e);
        }

        // 如果插入失败，可以返回一个错误代码或抛出异常
        //throw new RuntimeException("User creation failed");
        return -1;
    }

    @Override
    public boolean deleteAccount(AuthInfo auth, long mid)
    {

        long authMid = isAuthValid(auth, dataSource);
        //System.out.println("********************************************");
        // 首先，验证 auth 是否有效，并得到有效的mid
        if (authMid == -1)
        {
            //System.out.println("auth validation:" + false);
            return false;
        }
        //System.out.println("auth validation: " + true);
        // 检查 auth 是否拥有删除 mid 的权限
        if (!hasDeletePermission(authMid, mid))
        {
            //System.out.println("has permission: " + false);
            return false;
        }
        //System.out.println("has permission: " + true);
        // 执行删除操作
        return performDelete(mid);
    }

    private boolean hasDeletePermission(long authMid, long mid)
    {
        String sql = """
                select case
                    when a and b then ?=?
                    when a and not b then true
                    when not a and b then false
                    when not a and not b then ?=?
                end as c
                from (select operator_is_superuser a, deleted_is_superuser b
                      from (select case
                                       when identity = 'SUPERUSER' then true
                                       when identity = 'USER' then false
                                       end as operator_is_superuser,
                                   'temp'     temp
                            from users
                            where mid = ?) operator
                               join
                           (select case
                                       when identity = 'SUPERUSER' then true
                                       when identity = 'USER' then false
                                       end as deleted_is_superuser,
                                   'temp'     temp
                            from users
                            where mid = ?) deleted on operator.temp = deleted.temp) t;
                """;

        //检查对应用户的identity是否为superuser，且删除用户的identity是user
        boolean flag;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setLong(1, authMid);
            stmt.setLong(2, mid);
            stmt.setLong(3, authMid);
            stmt.setLong(4, mid);
            stmt.setLong(5, authMid);
            stmt.setLong(6, mid);
            //System.out.println(stmt);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                flag = rs.getBoolean("c");
                return flag;
            }
        }
        catch (SQLException e)
        {
            System.out.println(e);
        }
        return false;
    }

    private boolean performDelete(long mid)
    {
        String sql = """
                delete from danmu_likes where mid = ? or danmu_likes.id in (select id from danmu where mid=?);
                delete from user_follow where follow_mid=? or follow_by_mid=?;
                delete from favorite where mid=?;
                delete from view where mid=?;
                delete from coin where mid=?;
                delete from likes where mid=?;
                delete from review where reviewer_mid=?;
                delete from danmu where mid=?;
                delete from users where mid=?;
                delete from videos where owner_mid=?;
                """;//todo
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            for (int i = 1; i <= 12; i++)
            {
                stmt.setLong(i, mid);
            }
            //System.out.println(stmt);
            //System.out.println("********************************************");
            stmt.executeUpdate();
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean follow(AuthInfo auth, long followeeMid)
    {
        // 验证授权信息是否有效
        long authMid = isAuthValid(auth, dataSource);
        // 如果授权信息无效，返回 false
        if (authMid == -1 || authMid == followeeMid)
            return false;

        // SQL 查询，检查用户是否已经关注了 followee
        String checkSql = """
                SELECT *
                FROM user_follow
                WHERE follow_mid = ? AND follow_by_mid = ?""";

        // SQL 删除，用于取消关注
        String deleteSql = """
                DELETE FROM user_follow
                WHERE follow_mid = ? AND follow_by_mid = ?""";

        // SQL 插入，用于添加新的关注
        String insertSql = """
                INSERT INTO user_follow (follow_mid, follow_by_mid)
                VALUES (?, ?)""";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql))
        {

            checkStmt.setLong(1, authMid);
            checkStmt.setLong(2, followeeMid);
            try (ResultSet checkRs = checkStmt.executeQuery())
            {
                if (checkRs.next())
                {
                    // 如果已经关注，则执行删除操作
                    return executeUpdate(conn, deleteSql, authMid, followeeMid) == 1;
                }
                else
                {
                    // 如果尚未关注，则执行插入操作
                    return executeUpdate(conn, insertSql, authMid, followeeMid) == 1;
                }
            }
        }
        catch (SQLException e)
        {
            log.error("Error during follow: " + e);
            return false;
        }
    }

    private int executeUpdate(Connection conn, String sql, long authMid, long followeeMid) throws SQLException
    {
        try (PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setLong(1, authMid); // 设置执行操作的用户的 mid
            stmt.setLong(2, followeeMid); // 设置要关注或取消关注的用户的 mid
            return stmt.executeUpdate(); // 执行更新操作并返回影响的行数
        }
    }

    @Override
    public UserInfoResp getUserInfo(long mid)
    {
        // SQL 查询语句，获取用户信息以及与用户相关的各种列表
        String sql = """
                SELECT mid,
                       coin,
                    array(SELECT follow_mid
                             FROM user_follow
                             WHERE follow_by_mid = ?) following,
                    array(SELECT follow_by_mid
                            FROM user_follow
                            WHERE follow_mid = ?)     follower,
                    array(SELECT bv
                            FROM view
                            WHERE mid = ?)            watched,
                    array(SELECT bv
                            FROM likes
                            WHERE mid = ?)            liked,
                    array(SELECT bv
                            FROM favorite
                            WHERE mid = ?)            favorited,
                    array(SELECT bv
                            FROM videos
                            WHERE owner_mid = ?)      posted
                        FROM users
                        WHERE mid = ?;"""; // 查询用户基本信息及关联的列表

        try (Connection conn = dataSource.getConnection(); // 获取数据库连接
             PreparedStatement stmt = conn.prepareStatement(sql))
        { // 准备 SQL 语句

            // 设置查询参数
            for (int i = 1; i < 8; i++)
            {
                stmt.setLong(i, mid);
            }

            try (ResultSet rs = stmt.executeQuery())
            { // 执行查询并处理结果集
                if (rs.next())
                {
                    //log.info("getUserInfo: {}", stmt);
                    // 封装查询结果到 UserInfoResp 对象并返回
                    return new UserInfoResp(
                            mid,
                            rs.getInt(2),
                            getLongArray(rs.getArray(4)),
                            getLongArray(rs.getArray(3)),
                            (String[]) safeGetArray(rs, 5),
                            (String[]) safeGetArray(rs, 6),
                            (String[]) safeGetArray(rs, 7),
                            (String[]) safeGetArray(rs, 8)
                    );
                }
            }
        }
        catch (SQLException e)
        {
            log.error("Error during getting user info: " + e); // 记录 SQL 异常
        }
        log.error("User info not found");
        return null; // 发生异常或未找到用户时返回 null
    }

    private Object[] safeGetArray(ResultSet rs, int columnIndex) throws SQLException
    {
        Array array = rs.getArray(columnIndex);
        if (array != null)
        {
            return (Object[]) array.getArray();
        }
        return new Object[0]; // 返回空数组
    }

    private long[] getLongArray(Array arrayFromDb) throws SQLException
    {
        Long[] longObjects = (Long[]) arrayFromDb.getArray();

        long[] longs = new long[longObjects.length];
        for (int i = 0; i < longObjects.length; i++)
        {
            Long currentLong = longObjects[i];
            longs[i] = (currentLong != null) ? currentLong : 0L; // 添加空值检查
        }
        return longs;
    }


}
