package io.sustc.service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface VideoService {

    /**
     * 发布一个视频。其提交时间应为 LocalDateTime.now()。
     *
     * @param auth 当前用户的认证信息
     * @param req 视频信息
     * @return 视频的 {@code bv}
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 所述</li>
     *   <li>{@code req} 无效
     *     <ul>
     *       <li>{@code title} 为空或者无内容</li>
     *       <li>存在另一个同名且同一用户的视频</li>
     *       <li>{@code duration} 小于 10（无法划分任何分块）</li>
     *       <li>{@code publicTime} 早于 LocalDateTime.now()</li>
     *     </ul>
     *   </li>
     * </ul>
     * 如果发生任何边界情况，将返回 {@code null}。
     */
    String postVideo(AuthInfo auth, PostVideoReq req);

    /**
     * 删除一个视频。
     * 这项操作可以由视频所有者或超级用户执行。
     * 视频的硬币将不会返还给捐赠者。
     * 同样，视频的点赞、收藏等也将被移除。
     *
     * @param auth 当前用户的认证信息
     * @param bv 视频的 {@code bv}
     * @return 操作是否成功
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 所述</li>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>{@code auth} 不是视频所有者也不是超级用户</li>
     * </ul>
     * 如果发生任何边界情况，将返回 {@code false}。
     */
    boolean deleteVideo(AuthInfo auth, String bv);

    /**
     * 更新视频信息。
     * 只有视频所有者可以更新视频信息。
     * 如果视频之前已审核，更新后的视频需要重新审核。
     * 视频时长不应被修改，因此点赞、收藏和弹幕不需要更新。
     *
     * @param auth 当前用户的认证信息
     * @param bv 视频的 {@code bv}
     * @param req 新的视频信息
     * @return 如果视频需要重新审核（之前已审核过），则返回 {@code true}，否则返回 {@code false}
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 所述</li>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>{@code auth} 不是视频所有者</li>
     *   <li>{@code req} 无效，如 {@link io.sustc.service.VideoService#postVideo(AuthInfo, PostVideoReq)} 所述</li>
     *   <li>{@code req} 中的 {@code duration} 与当前视频不一致</li>
     *   <li>{@code req} 与当前信息无变化</li>
     * </ul>
     * 如果发生任何边界情况，将返回 {@code false}。
     */
    boolean updateVideoInfo(AuthInfo auth, String bv, PostVideoReq req);

    /**
     * 根据关键字（以空格分隔）搜索视频。
     * 应该尝试在以下字段中不区分大小写地匹配关键字：
     * <ol>
     *   <li>标题</li>
     *   <li>描述</li>
     *   <li>所有者名称</li>
     * </ol>
     * <p>
     * 按相关性对结果进行排序（在这三个字段中匹配关键字的数量之和）。
     * <ul>
     *   <li>如果一个关键字多次出现，应该被多次计数。</li>
     *   <li>
     *     这些字段中的一个字符只能被每个关键字计数一次
     *     但可以为不同的关键字计数。
     *   </li>
     *   <li>如果两个视频相关性相同，按观看次数排序。</li>
     * </u
     * <p>
     * 示例：
     * <ol>
     *   <li>
     *     如果标题是 "1122"，关键字是 "11 12"，
     *     则标题中的相关性为 2（"11" 和 "12" 各计一次）。
     *   </li>
     *   <li>
     *     如果标题是 "111"，关键字是 "11"，
     *     则标题中的相关性为 1（"11" 出现一次）。
     *   </li>
     *   <li>
     *     考虑一个标题为 "Java Tutorial"，描述为 "Basic to Advanced Java"，所有者名称为 "John Doe" 的视频。
     *     如果搜索关键字是 "Java Advanced"，
     *     则相关性为 3（标题中出现一次，描述中出现两次）。
     *   </li>
     * </ol>
     * <p>
     * 未审核或未发布的视频仅对超级用户或视频所有者可见。
     *
     * @param auth 当前用户的认证信息
     * @param keywords 搜索关键字，例如 "sustech database final review"
     * @param pageSize 页面大小，如果视频少于 {@code pageSize}，则返回全部
     * @param pageNum 页面编号，从 1 开始
     * @return 视频 {@code bv} 列表
     * @implNote 如果请求的页面为空，则返回空列表
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 所述</li>
     *   <li>{@code keywords} 为空或无内容</li>
     *   <li>{@code pageSize} 和/或 {@code pageNum} 无效（任一小于等于 0）</li>
     * </ul>
     * 如果发生任何边界情况，将返回 {@code null}。
     */
    List<String> searchVideo(AuthInfo auth, String keywords, int pageSize, int pageNum);

    /**
     * 计算视频的平均观看率。
     * 观看率定义为用户观看时间除以视频时长。
     *
     * @param bv 视频的 {@code bv}
     * @return 平均观看率
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>没有人观看过这个视频</li>
     * </ul>
     * 如果发生任何边界情况，将返回 {@code -1}。
     */
    double getAverageViewRate(String bv);

    /**
     * 获取视频的热点。
     * 将视频分割成 10 秒的小块，热点定义为弹幕最多的小块。
     *
     * @param bv 视频的 {@code bv}
     * @return 热点小块的索引（从 0 开始）
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>没有人在这个视频上发送弹幕</li>
     * </ul>
     * 如果发生任何边界情况，将返回空集合。
     */
    Set<Integer> getHotspot(String bv);

    /**
     * 由超级用户审核视频。
     * 如果视频已被审核过，则不修改审核信息。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 bv
     * @return 如果视频成功被新审核，则返回 {@code true}，否则返回 {@code false}
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 所述</li>
     *   <li>找不到对应于 {@code bv} 的视频</li>
     *   <li>{@code auth} 不是超级用户或者他/她是视频所有者</li>
     *   <li>视频已被审核过</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */
    boolean reviewVideo(AuthInfo auth, String bv);


    /**
     * 给视频投币。一个用户对一个视频最多只能投一枚币。
     * 用户只有在能搜索到视频时才能投币（参见 {@link io.sustc.service.VideoService#searchVideo(AuthInfo, String, int, int)}）。
     * 用户不必先观看视频就可以对其投币。
     * 如果当前用户成功对这个视频投了币，他/她的币数将减少1。
     * 然而，视频所有者的币数不会增加。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 bv
     * @return 是否成功投币
     * @implNote 为简化，在此项目中没有赚取币的方式
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 所述</li>
     *   <li>找不到对应于 {@code bv} 的视频</li>
     *   <li>用户无法搜索到这个视频或者他/她是视频所有者</li>
     *   <li>用户没有币或已对该视频投过币（用户不能撤销投币）</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */
    boolean coinVideo(AuthInfo auth, String bv);


    /**
     * 点赞视频。
     * 用户只有在能搜索到视频时才能点赞（参见 {@link io.sustc.service.VideoService#searchVideo(AuthInfo, String, int, int)}）。
     * 如果用户已经点过赞，该操作将取消点赞。
     * 用户不必先观看视频就可以对其点赞。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 bv
     * @return 此操作后用户对该视频的点赞状态
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 所述</li>
     *   <li>找不到对应于 {@code bv} 的视频</li>
     *   <li>用户无法搜索到这个视频或用户是视频所有者</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */
    boolean likeVideo(AuthInfo auth, String bv);


    /**
     * 收藏视频。
     * 用户只有在能搜索到视频时才能收藏。
     * 如果用户已经收藏了视频，该操作将取消收藏。
     * 用户不必先观看视频就可以对其收藏。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 bv
     * @return 此操作后用户对该视频的收藏状态
     * @apiNote 可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 所述</li>
     *   <li>找不到对应于 {@code bv} 的视频</li>
     *   <li>用户无法搜索到这个视频或用户是视频所有者</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */
    boolean collectVideo(AuthInfo auth, String bv);

}
