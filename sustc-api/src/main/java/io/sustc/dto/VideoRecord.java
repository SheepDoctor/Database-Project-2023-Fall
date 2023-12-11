package io.sustc.dto;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class VideoRecord {

    /**
     * The BV code of this video
     */
    private String bv;

    /**
     * The title of this video with length >= 1, the video titles of an owner cannot be the same
     */
    private String title;

    /**
     * The owner's {@code mid} of this video
     */
    private long ownerMid;

    /**
     * The owner's {@code name} of this video
     */
    private String ownerName;

    /**
     * The commit time of this video
     */
    private Timestamp commitTime;

    /**
     * The review time of this video, can be null
     */
    private Timestamp reviewTime;

    /**
     * The public time of this video, can be null
     */
    private Timestamp publicTime;

    /**
     * The length in seconds of this video
     */
    private float duration;

    /**
     * The description of this video
     */
    private String description;

    /**
     * The reviewer of this video, can be null
     */
    private Long reviewer;

    /**
     * The users' {@code mid}s who liked this video
     */
    private long[] like;

    /**
     * The users' {@code mid}s who gave coin to this video
     */
    private long[] coin;

    /**
     * The users' {@code mid}s who collected to this video
     */
    private long[] favorite;

    /**
     * The users' {@code mid}s who have watched this video
     */
    private long[] viewerMids;

    /**
     * The watch durations in seconds for the viewers {@code viewerMids}
     */
    private float[] viewTime;

    static private long av;

    public String getBv()
    {
        return bv;
    }

    public void setBv(String bv)
    {
        this.bv = bv;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public long getOwnerMid()
    {
        return ownerMid;
    }

    public void setOwnerMid(long ownerMid)
    {
        this.ownerMid = ownerMid;
    }

    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName(String ownerName)
    {
        this.ownerName = ownerName;
    }

    public Timestamp getCommitTime()
    {
        return commitTime;
    }

    public void setCommitTime(Timestamp commitTime)
    {
        this.commitTime = commitTime;
    }

    public Timestamp getReviewTime()
    {
        return reviewTime;
    }

    public void setReviewTime(Timestamp reviewTime)
    {
        this.reviewTime = reviewTime;
    }

    public Timestamp getPublicTime()
    {
        return publicTime;
    }

    public void setPublicTime(Timestamp publicTime)
    {
        this.publicTime = publicTime;
    }

    public float getDuration()
    {
        return duration;
    }

    public void setDuration(float duration)
    {
        this.duration = duration;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getReviewer()
    {
        return reviewer;
    }

    public void setReviewer(Long reviewer)
    {
        this.reviewer = reviewer;
    }

    public long[] getLike()
    {
        return like;
    }

    public void setLike(long[] like)
    {
        this.like = like;
    }

    public long[] getCoin()
    {
        return coin;
    }

    public void setCoin(long[] coin)
    {
        this.coin = coin;
    }

    public long[] getFavorite()
    {
        return favorite;
    }

    public void setFavorite(long[] favorite)
    {
        this.favorite = favorite;
    }

    public long[] getViewerMids()
    {
        return viewerMids;
    }

    public void setViewerMids(long[] viewerMids)
    {
        this.viewerMids = viewerMids;
    }

    public float[] getViewTime()
    {
        return viewTime;
    }

    public void setViewTime(float[] viewTime)
    {
        this.viewTime = viewTime;
    }


    public static long getav()
    {
        av++;
        return av;
    }
}
