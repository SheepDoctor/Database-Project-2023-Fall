package io.sustc.service;

import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;

import java.util.List;

public interface DatabaseService {

    /**
     * 确认此项目的作者。
     *
     * @return 小组成员的学生id列表
     */
    List<Integer> getGroupMembers();

    /**
     * 将数据导入到空数据库中。
     * 不会提供无效数据。
     *
     * @param danmuRecords 从csv解析的弹幕记录
     * @param userRecords  从csv解析的用户记录
     * @param videoRecords 从csv解析的视频记录
     */
    void importData(
            List<DanmuRecord> danmuRecords,
            List<UserRecord> userRecords,
            List<VideoRecord> videoRecords
    );

    /**
     * 截断数据库中的所有表。
     * <p>
     * 这将只在本地基准测试中使用，以帮助您清理数据库而不删除它，并且不会影响您的得分。
     */
    void truncate();

    /**
     * 通过Postgres求两个数字的和。
     * 此方法仅演示如何通过JDBC访问数据库。
     *
     * @param a 第一个数字
     * @param b 第二个数字
     * @return 两个数字的和
     */
    Integer sum(int a, int b);
}
