package io.sustc.service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.PostVideoReq;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface VideoService {

    /**
     * 发布一个视频。其提交时间应该是 {@link LocalDateTime#now()}。
     *
     * @param auth 当前用户的认证信息
     * @param req  视频的信息
     * @return 视频的 {@code bv}
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>{@code req} 无效
     *     <ul>
     *       <li>{@code title} 为空或者空字符串</li>
     *       <li>存在另一个同名且属于同一用户的视频</li>
     *       <li>{@code duration} 小于10（这样就无法分割成片段）</li>
     *       <li>{@code publicTime} 早于 {@link LocalDateTime#now()}</li>
     *     </ul>
     *   </li>
     * </ul>
     * 如果出现任何边界情况，应该返回 {@code null}。
     */
    String postVideo(AuthInfo auth, PostVideoReq req);

    /**
     * 删除一个视频。
     * 这个操作可以由视频所有者或超级用户执行。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 {@code bv}
     * @return 是否成功
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>{@code auth} 既不是视频所有者也不是超级用户</li>
     * </ul>
     * 如果出现任何边界情况，应该返回 {@code false}。
     */
    boolean deleteVideo(AuthInfo auth, String bv);

    /**
     * 更新视频信息。
     * 只有视频所有者可以更新视频信息。
     * 如果视频之前已经被审核，那么更新后的视频需要重新审核。
     * 视频时长不应修改，因此不需要更新点赞、收藏和弹幕。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 {@code bv}
     * @param req  新的视频信息
     * @return 如果视频需要重新审核（之前已经审核过），则返回 {@code true}，否则返回 {@code false}
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>{@code auth} 不是视频所有者</li>
     *   <li>{@code req} 无效，如 {@link io.sustc.service.VideoService#postVideo(AuthInfo, PostVideoReq)} 中所述</li>
     *   <li>{@code req} 中的 {@code duration} 与当前视频时长不同</li>
     *   <li>{@code req} 与当前信息相同，没有变化</li>
     * </ul>
     * 如果出现任何边界情况，应该返回 {@code false}。
     */
    boolean updateVideoInfo(AuthInfo auth, String bv, PostVideoReq req);

    /**
     * 通过关键词（以空格分隔）搜索视频。
     * 您应该尝试在以下字段中不区分大小写地匹配关键词：
     * <ol>
     *   <li>标题</li>
     *   <li>描述</li>
     *   <li>所有者名称</li>
     * </ol>
     * <p>
     * 根据相关性对结果进行排序（在这三个字段中匹配的关键词数量之和）。
     * <ul>
     *   <li>如果一个关键词出现多次，它应该被重复计数。</li>
     *   <li>
     *     这些字段中的一个字符对于每个关键词只能计数一次
     *     但可以对不同的关键词计数。
     *   </li>
     *   <li>如果两个视频的相关性相同，按照观看次数排序。</li>
     * </u
     * <p>
     * 示例：
     * <ol>
     *   <li>
     *     如果标题是 "1122"，关键词是 "11 12"，
     *     那么标题中的相关性是 2（"11" 一个和 "12" 一个）。
     *   </li>
     *   <li>
     *     如果标题是 "111"，关键词是 "11"，
     *     那么标题中的相关性是 1（"11" 出现一次）。
     *   </li>
     *   <li>
     *     考虑一个视频，标题为 "Java Tutorial"，描述为 "Basic to Advanced Java"，所有者名为 "John Doe"。
     *     如果搜索关键词是 "Java Advanced"，
     *     那么相关性是 3（标题中出现一次，描述中出现两次）。
     *   </li>
     * </ol>
     * <p>
     * 未审核或未发布的视频只对超级用户或视频所有者可见。
     *
     * @param auth     当前用户的认证信息
     * @param keywords 搜索关键词，例如 "sustech database final review"
     * @param pageSize 页面大小，如果视频少于 {@code pageSize}，则返回所有视频
     * @param pageNum  页面号，从 1 开始
     * @return 视频 {@code bv} 的列表
     * @implNote 如果请求的页面为空，则返回一个空列表
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>{@code keywords} 为空或者空字符串</li>
     *   <li>{@code pageSize} 和/或 {@code pageNum} 无效（任何一个 <= 0）</li>
     * </ul>
     * 如果出现任何边界情况，应该返回 {@code null}。
     */
    List<String> searchVideo(AuthInfo auth, String keywords, int pageSize, int pageNum);

    /**
     * 计算视频的平均观看率。
     * 观看率定义为用户的观看时间除以视频的持续时间。
     *
     * @param bv 视频的 {@code bv}
     * @return 平均观看率
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>没有人观看过这个视频</li>
     * </ul>
     * 如果出现任何边界情况，应该返回 {@code -1}。
     */
    double getAverageViewRate(String bv);

    /**
     * 获取视频的热点。
     * 将视频分割成10秒钟的片段，热点定义为弹幕最多的片段。
     *
     * @param bv 视频的 {@code bv} 标识
     * @return 热点片段的索引（从0开始）
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>没有人在这个视频上发送弹幕</li>
     * </ul>
     * 如果发生任何边界情况，应返回一个空集合。
     */
    Set<Integer> getHotspot(String bv);

    /**
     * 由超级用户审核视频。
     * 如果视频已经被审核过，则不修改审核信息。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 {@code bv} 标识
     * @return 如果视频是新成功审核的，则返回 {@code true}，否则返回 {@code false}
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>{@code auth} 不是超级用户或他/她是视频所有者</li>
     *   <li>视频已经被审核过</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */

    boolean reviewVideo(AuthInfo auth, String bv);

    /**
     * 给视频投币。用户对一个视频最多只能投一枚币。
     * 用户只有在能搜索到该视频时（{@link io.sustc.service.VideoService#searchVideo(AuthInfo, String, int, int)}）才能给视频投币。
     * 投币前不强制要求用户观看视频。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 {@code bv} 标识
     * @return 是否成功投币
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>用户无法搜索到这个视频或他/她是视频所有者</li>
     *   <li>用户没有币或已经对这个视频投过币</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */


    boolean coinVideo(AuthInfo auth, String bv);

    /**
     * 点赞视频。
     * 用户只有在能搜索到该视频时才能点赞视频。
     * 如果用户已经点赞了视频，该操作将取消点赞。
     * 点赞前不强制要求用户观看视频。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 {@code bv} 标识
     * @return 该操作后用户对此视频的点赞状态
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>用户无法搜索到这个视频或用户是视频所有者</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */

    boolean likeVideo(AuthInfo auth, String bv);

    /**
     * 收藏视频。
     * 用户只有在能搜索到该视频时才能收藏视频。
     * 如果用户已经收藏了视频，该操作将取消收藏。
     * 收藏前不强制要求用户观看视频。
     *
     * @param auth 当前用户的认证信息
     * @param bv   视频的 {@code bv} 标识
     * @return 该操作后用户对此视频的收藏状态
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>找不到对应 {@code bv} 的视频</li>
     *   <li>用户无法搜索到这个视频或用户是视频所有者</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */

    boolean collectVideo(AuthInfo auth, String bv);
}
