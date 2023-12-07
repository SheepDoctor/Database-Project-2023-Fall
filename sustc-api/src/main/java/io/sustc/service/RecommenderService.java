package io.sustc.service;

import java.util.List;
import io.sustc.dto.AuthInfo;

public interface RecommenderService {

    /**
     * 为一个视频推荐五个最相似的视频。
     * 相似性定义为在数据库中观看了这两个视频的用户数量。
     *
     * @param bv 当前视频
     * @return 视频 {@code bv} 的列表
     * @apiNote 您可以考虑以下边界情况：
     * <ul>
     *   <li>找不到与 {@code bv} 对应的视频</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code null}。
     */
    List<String> recommendNextVideo(String bv);

    /**
     * 基于视频的流行度为匿名用户推荐视频。
     * 从以下方面评估视频的受欢迎程度：
     * <ol>
     *   <li>"like": 观看视频的用户中也喜欢这个视频的比率</li>
     *   <li>"coin": 观看视频的用户中也给这个视频投币的比率</li>
     *   <li>"fav": 观看视频的用户中也收藏这个视频的比率</li>
     *   <li>"danmu": 每个观看用户发送的弹幕平均数量</li>
     *   <li>"finish": 每个观看用户的平均视频观看完成度</li>
     * </ol>
     * 推荐分数可以这样计算：
     * <pre>
     *   score = like + coin + fav + danmu + finish
     * </pre>
     *
     * @param pageSize 页面大小，如果视频数量少于 {@code pageSize}，返回所有视频
     * @param pageNum  页码，从1开始
     * @return 按推荐分数排序的视频 {@code bv} 列表
     * @implNote
     * 尽管用户可以在不观看视频的情况下对视频进行点赞/投币/收藏，但这些值的比率应限制在1以内。
     * 如果没有人观看这个视频，所有五个分数都应该是0。
     * 如果请求的页面为空，返回一个空列表。
     * @apiNote 您可以考虑以下边界情况：
     * <ul>
     *   <li>{@code pageSize} 和/或 {@code pageNum} 无效（任何一个 <= 0）</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code null}。
     */
    List<String> generalRecommendations(int pageSize, int pageNum);

    /**
     * 根据用户的兴趣为用户推荐视频。
     * 用户的兴趣定义为用户的朋友观看过的视频，过滤掉用户已经观看过的视频。
     * 当前用户的朋友是同时是当前用户的粉丝和关注对象的人。
     * 按以下方式对视频排序：
     * <ol>
     *   <li>观看视频的朋友数量</li>
     *   <li>视频所有者的等级</li>
     *   <li>视频的发布时间（新视频优先）</li>
     * </ol>
     *
     * @param auth     当前用户的认证信息
     * @param pageSize 页面大小，如果视频数量少于 {@code pageSize}，返回所有视频
     * @param pageNum  页码，从1开始
     * @return 视频 {@code bv} 的列表
     * @implNote
     * 如果当前用户的兴趣为空，返回 {@link io.sustc.service.RecommenderService#generalRecommendations(int, int)}。
     * 如果请求的页面为空，返回一个空列表
     * @apiNote 您可以考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>{@code pageSize} 和/或 {@code pageNum} 无效（任何一个 <= 0）</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code null}。
     */
    List<String> recommendVideosForUser(AuthInfo auth, int pageSize, int pageNum);

    /**
     * 基于共同关注推荐朋友给用户。
     * 找到所有未被用户关注且至少有一个共同关注的用户。
     * 按共同关注的数量对用户进行排序，如果两个用户的共同关注数量相同，
     * 则按他们的 {@code level} 排序。
     *
     * @param auth     当前用户的认证信息
     * @param pageSize 页面大小，如果用户数量少于 {@code pageSize}，返回所有用户
     * @param pageNum  页码，从1开始
     * @return 推荐用户的 {@code mid} 列表
     * @implNote 如果请求的页面为空，返回一个空列表
     * @apiNote 您可以考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>{@code pageSize} 和/或 {@code pageNum} 无效（任何一个 <= 0）</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code null}。
     */
    List<Long> recommendFriends(AuthInfo auth, int pageSize, int pageNum);
}
