package io.sustc.service;

import io.sustc.dto.AuthInfo;

import java.util.List;

public interface DanmuService {

    /**
     * 向视频发送弹幕。
     * 用户必须先观看视频，然后才能向其发送弹幕。
     *
     * @param auth    当前用户的认证信息
     * @param bv      视频的bv
     * @param content 弹幕内容
     * @param time    视频开始后的秒数
     * @return 生成的弹幕id
     * @apiNote 您可能需要考虑以下特殊情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>找不到对应于 {@code bv} 的视频</li>
     *   <li>{@code content} 无效（为空或为空）</li>
     *   <li>视频未发布或用户未观看此视频</li>
     * </ul>
     * 如果发生任何特殊情况，应返回 {@code -1}。
     */
    long sendDanmu(AuthInfo auth, String bv, String content, float time);

    /**
     * 在时间范围内显示弹幕。
     * 类似于bilibili的机制，用户可以选择只显示部分弹幕以获得更好的观看体验。
     *
     * @param bv        视频的bv
     * @param timeStart 范围的开始时间
     * @param timeEnd   范围的结束时间
     * @param filter    是否移除重复内容，
     *                  如果 {@code true}，则只返回最早发布的具有相同内容的弹幕
     * @return 按 {@code time} 排序的弹幕id列表
     * @apiNote 您可能需要考虑以下特殊情况：
     * <ul>
     *   <li>找不到对应于 {@code bv} 的视频</li>
     *   <li>
     *     {@code timeStart} 和/或 {@code timeEnd} 无效（{@code timeStart} <= {@code timeEnd}
     *     或者它们中的任何一个 < 0 或 > 视频时长）
     *   </li>
     * <li>视频未发布</li>
     * </ul>
     * 如果发生任何特殊情况，应返回 {@code null}。
     */
    List<Long> displayDanmu(String bv, float timeStart, float timeEnd, boolean filter);

    /**
     * 点赞弹幕。
     * 如果用户已经对弹幕点赞，此操作将取消点赞状态。
     * 用户必须先观看视频，然后才能对其弹幕点赞。
     *
     * @param auth 当前用户的认证信息
     * @param id   弹幕的id
     * @return 此操作后用户对此弹幕的点赞状态
     * @apiNote 您可能需要考虑以下特殊情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>找不到对应于 {@code id} 的弹幕</li>
     * </ul>
     * 如果发生任何特殊情况，应返回 {@code false}。
     */
    boolean likeDanmu(AuthInfo auth, long id);
}
