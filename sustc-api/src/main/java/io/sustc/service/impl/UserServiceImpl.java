package io.sustc.service.impl;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.RegisterUserReq;
import io.sustc.dto.UserInfoResp;
import io.sustc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        // 创建新用户的逻辑（省略具体实现）
        long newUserId = createUser(req);

        return newUserId;
    }

    @Override
    public boolean deleteAccount(AuthInfo auth, long mid)
    {
        return false;
    }

    @Override
    public boolean follow(AuthInfo auth, long followeeMid)
    {
        return false;
    }

    @Override
    public UserInfoResp getUserInfo(long mid)
    {
        return null;
    }

    // 示例方法：验证生日格式
    private boolean isValidBirthday(String birthday) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false); // 设置为严格的日期解析，不允许日期溢出（例如2月30日）
        try {
            // 尝试解析日期
            Date date = sdf.parse(birthday);
            return true;
        } catch (ParseException e) {
            // 解析失败，说明日期无效
            e.printStackTrace();
            return false;
        }
    }

    // 示例方法：检查唯一性
    private boolean isUnique(String name, String qq, String wechat) {
        String sql = "SELECT COUNT(*) FROM users WHERE name = ? OR qq = ? OR wechat = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // 设置参数
            stmt.setString(1, name);
            stmt.setString(2, qq);
            stmt.setString(3, wechat);

            // 执行查询
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // 如果计数为0，则表示唯一
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }


    // 示例方法：创建新用户
    private long createUser(RegisterUserReq req)
    {
        // 实现创建用户的逻辑，包括存储用户信息到数据库
        // 返回新创建的用户ID（假设ID是自动生成的）
        return 1000L;
    }
}
