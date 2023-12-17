package io.sustc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResp {

    /**
     * The user's {@code mid}.
     */
    private long mid;

    /**
     * The number of user's coins that he/she currently owns.
     */
    private int coin;

    /**
     * The user's following {@code mid}s.
     */
    private long[] following;

    /**
     * The user's follower {@code mid}s.
     */
    private long[] follower;

    /**
     * The videos' {@code bv}s watched by this user.
     */
    private String[] watched;

    /**
     * The videos' {@code bv}s liked by this user.
     */
    private String[] liked;

    /**
     * The videos' {@code bv}s collected by this user.
     */
    private String[] collected;

    /**
     * The videos' {@code bv}s posted by this user.
     */
    private String[] posted;

    public long getMid()
    {
        return mid;
    }

    public void setMid(long mid)
    {
        this.mid = mid;
    }

    public int getCoin()
    {
        return coin;
    }

    public void setCoin(int coin)
    {
        this.coin = coin;
    }

    public long[] getFollowing()
    {
        return following;
    }

    public void setFollowing(long[] following)
    {
        this.following = following;
    }

    public long[] getFollower()
    {
        return follower;
    }

    public void setFollower(long[] follower)
    {
        this.follower = follower;
    }

    public String[] getWatched()
    {
        return watched;
    }

    public void setWatched(String[] watched)
    {
        this.watched = watched;
    }

    public String[] getLiked()
    {
        return liked;
    }

    public void setLiked(String[] liked)
    {
        this.liked = liked;
    }

    public String[] getCollected()
    {
        return collected;
    }

    public void setCollected(String[] collected)
    {
        this.collected = collected;
    }

    public String[] getPosted()
    {
        return posted;
    }

    public void setPosted(String[] posted)
    {
        this.posted = posted;
    }
}
