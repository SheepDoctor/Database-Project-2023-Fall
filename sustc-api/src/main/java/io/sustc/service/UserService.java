package io.sustc.service;

import io.sustc.dto.AuthInfo;
import io.sustc.dto.RegisterUserReq;
import io.sustc.dto.UserInfoResp;

public interface UserService {

    /**
     * 注册新用户。
     * {@code password} 是必填字段，而 {@code qq} 和 {@code wechat} 是可选的
     * <a href="https://openid.net/developers/how-connect-works/">OIDC</a> 字段。
     *
     * @param req 新用户的信息
     * @return 新用户的 {@code mid}
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code req} 中的 {@code password} 或 {@code name} 或 {@code sex} 为空或不存在</li>
     *   <li>{@code req} 中的 {@code birthday} 有效（非空）但不是生日（X月X日）</li>
     *   <li>存在另一个用户与 {@code req} 中的 {@code name} 或 {@code qq} 或 {@code wechat} 相同</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code -1}。
     */
    long register(RegisterUserReq req);

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
    boolean deleteAccount(AuthInfo auth, long mid);

    /**
     * 关注 {@code mid} 的用户。
     * 如果该用户已经被关注，取消关注该用户。
     *
     * @param auth        关注者的认证信息
     * @param followeeMid 将被关注的用户
     * @return 此操作后的关注状态
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>{@code auth} 无效，如 {@link io.sustc.service.UserService#deleteAccount(AuthInfo, long)} 中所述</li>
     *   <li>找不到与 {@code followeeMid} 对应的用户</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code false}。
     */
    boolean follow(AuthInfo auth, long followeeMid);

    /**
     * 获取用户的所需信息（以 DTO 形式）。
     *
     * @param mid 要查询的用户
     * @return 给定 {@code mid} 的个人信息
     * @apiNote 您可能需要考虑以下边界情况：
     * <ul>
     *   <li>找不到与 {@code mid} 对应的用户</li>
     * </ul>
     * 如果发生任何边界情况，应返回 {@code null}。
     */
    UserInfoResp getUserInfo(long mid);
}
