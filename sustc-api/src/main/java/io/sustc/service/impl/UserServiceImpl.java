package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.RegisterUserReq;
import io.sustc.dto.UserInfoResp;
import io.sustc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import javax.swing.*;
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

    @Override
    public long register(RegisterUserReq req)
    {
        // 检查必要字段是否为空
        if (req.getPassword() == null || req.getName() == null || req.getSex() == null)
        {
            return -1;
        }

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
        try
        {
            // 尝试解析日期
            Date date = sdf.parse(birthday);
            return true;
        }
        catch (ParseException e)
        {
            // 解析失败，说明日期无效
            e.printStackTrace();
            return false;
        }
    }

    // 检查唯一性
    private boolean isUnique(String name, String qq, String wechat)
    {
        String sql = "SELECT COUNT(*) FROM users WHERE name = ? OR qq = ? OR wechat = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // 设置参数
            stmt.setString(1, name);
            stmt.setString(2, qq);
            stmt.setString(3, wechat);

            // 执行查询
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                // 如果计数为0，则表示唯一
                return rs.getInt(1) == 0;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
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
            // 设置 PreparedStatement 参数
            stmt.setString(1, req.getName());
            stmt.setString(2, req.getSex().name());
            stmt.setDate(3, java.sql.Date.valueOf(req.getBirthday()));
            stmt.setString(4, req.getSign());
            stmt.setString(5, req.getQq());
            stmt.setString(6, req.getWechat());
            stmt.setString(7, req.getPassword());

            System.out.println(stmt);

            // 执行查询并获取返回的主键
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                return rs.getLong("mid"); // 返回生成的用户ID
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // 如果插入失败，可以返回一个错误代码或抛出异常
        throw new RuntimeException("User creation failed");
    }

    /**
     * 删除用户。
     *
     * @param auth 表示当前用户
     * @param mid  要删除的用户
     * @return 操作是否成功
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>找不到与 {@code mid} 对应的用户</li>
     *   <li>{@code auth} 无效
     *     <ul>
     *       <li>{@code qq} 和 {@code wechat} 都非空但不对应同一用户</li>
     *       <li>{@code mid} 无效，同时 {@code qq} 和 {@code wechat} 也无效（空或找不到）</li>
     *     </ul>
     *   </li>
     *   <li>当前用户是普通用户，但 {@code mid} 不是他/她的</li>
     *   <li>当前用户是超级用户，但 {@code mid} 既不是普通用户的 {@code mid} 也不是他/她的</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */
    @Override
    public boolean deleteAccount(AuthInfo auth, long mid)
    {
        // 首先，验证 auth 是否有效
        if (!isAuthValid(auth))
        {
            return false;
        }

        // 检查 auth 是否拥有删除 mid 的权限
        if (!hasDeletePermission(auth, mid))
        {
            return false;
        }

        // 执行删除操作
        return performDelete(mid);
    }

    private boolean isAuthValid(AuthInfo auth)
    {
        //1.mid和password 2.wechat 3.qq，
        //1.当方式1无效（mid查询出来不存在或mid为空)，qq和wechat此时必须有一个有效
        //2.qq和wechat有效时，这两个必须属于同一个用户
        try
        {
            String str = "TO DO";
            Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(str);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        return true; // 假设验证通过
    }

    private boolean hasDeletePermission(AuthInfo auth, long mid)
    {
        // 检查用户是否有删除 mid 的权限
        // 例如，判断用户是否是超级用户或 mid 是否属于该用户
        // 示例代码省略实际查询逻辑
        return true; // 假设拥有权限
    }

    private boolean performDelete(long mid)
    {
        // 实际的删除操作
        String sql = "DELETE FROM users WHERE mid = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setLong(1, mid);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
        catch (SQLException e)
        {
            // 日志记录或处理异常
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean follow(AuthInfo auth, long followeeMid)
    {
        try
        {
            Connection conn = dataSource.getConnection();
            String check = "select *\n" +
                    "from follow\n" +
                    "where follow_mid = ? and follow_by_mid = ?";
            PreparedStatement check_stmt = conn.prepareStatement(check);
            check_stmt.setLong(1, auth.getMid());
            check_stmt.setLong(2, followeeMid);
            ResultSet check_rs = check_stmt.executeQuery();
            if (check_rs.next())
            {
                String sql = "delete from follow\n" +
                        "where follow_mid = ? and follow_by_mid = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setLong(1, auth.getMid());
                stmt.setLong(2, followeeMid);
                return stmt.executeUpdate() == -1;
            }
            else
            {
                String sql = "insert into follow (follow_mid, follow_by_mid) values (?, ?);";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setLong(1, auth.getMid());
                stmt.setLong(2, followeeMid);
                return stmt.executeUpdate() == -1;
            }
        }
        catch (SQLException e)
        {
            System.out.println("follow: " + e);
            return false;
        }
    }

    @Override
    public UserInfoResp getUserInfo(long mid)
    {
        try
        {
            Connection conn = dataSource.getConnection();
            String str = """
                    select mid,
                           coin,
                           array(select follow_mid
                                 from user_follow
                                 where follow_mid = ?) following,
                           array(select follow_mid
                                 from user_follow
                                 where follow_mid = ?) gollower,
                           array(select bv
                                 from view
                                 where mid = ?)        watched,
                           array(select bv
                                 from likes
                                 where mid = ?)        liked,
                           array(select bv
                                 from collect
                                 where mid = ?)        collated,
                           array(select bv
                                 from videos
                                 where owner_mid = ?)  posted
                    from users""";
            PreparedStatement stmt = conn.prepareStatement(str);
            for (int i = 1; i < 7; i++)
            {
                stmt.setLong(i, mid);
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                return new UserInfoResp(
                        rs.getLong(1),
                        rs.getInt(2),
                        (long[]) rs.getArray(3).getArray(),
                        (long[]) rs.getArray(4).getArray(),
                        (String[]) rs.getArray(5).getArray(),
                        (String[]) rs.getArray(6).getArray(),
                        (String[]) rs.getArray(7).getArray(),
                        (String[]) rs.getArray(8).getArray()
                );
            }
        }
        catch (SQLException e)
        {
            System.out.println("getUserInfo: " + e);
        }
        return null;
    }

}
