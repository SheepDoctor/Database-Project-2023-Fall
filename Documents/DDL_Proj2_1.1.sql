-- 创建一个名为 "sustc" 的数据库
-- CREATE DATABASE sustc WITH ENCODING = 'UTF8' LC_COLLATE = 'C' TEMPLATE = template0;

-- 定义一个枚举类型 'identity_type'，包含 'user' 和 'superuser' 两个值
CREATE TYPE identity_type AS ENUM ('USER', 'SUPERUSER');

-- 定义一个枚举类型 'gender_type'，包含 'MALE'、'FEMALE' 和 'UNKNOWN' 三个值
create type gender_type as enum ('男', '女', '保密');


-- 创建一个表示用户的表 'users'
create table users
(
    mid      bigserial primary key,        -- 用户的唯一识别号
    name     varchar,                      -- 用户创建的名称
    sex      gender_type,                  -- 性别，类型为 'gender_type' 枚举
    birthday varchar,                      -- 用户的生日
    level    int,                          -- 用户参与度的等级
    sign     text,                         -- 用户创建的个人描述
    identity identity_type default 'USER', -- 用户角色的值（'user', 'superuser'）
    qq       varchar unique,               -- QQ号
    wechat   varchar unique,               -- 微信号
    password varchar,                      -- 登录密码,使用sha-256存储
    coin     int           default (0)     -- 用户拥有的硬币数量
);


-- 创建一个表示用户之间关注关系的表 'user_follow'
create table user_follow
(
    follow_mid    bigint,                              -- 关注者的用户ID
    follow_by_mid bigint,                              -- 被关注者的用户ID
    primary key (follow_mid, follow_by_mid),
    foreign key (follow_mid) references users (mid),   -- 引用 'users' 表的 'mid'
    foreign key (follow_by_mid) references users (mid) -- 引用 'users' 表的 'mid'
    --,check (follow_by_mid <> follow_mid)             -- 检查防止自我关注（数据里说可以）
);


-- 创建一个表示视频的表 'videos'
create table videos
(
    bv          char(12) primary key, -- 视频的唯一识别字符串，格式为 'BV%'
    title       varchar   not null,   -- 视频标题
    owner_mid   bigint    not null,   -- 视频所有者的用户ID
    commit_time timestamp not null,   -- 视频提交时间
    public_time timestamp,            -- 视频对外公开时间
    duration    float   not null,   -- 视频持续时间（秒）
    description text,                 -- 视频简短介绍
    check ( bv like 'BV%')            -- 确保 'bv' 字段以 'BV' 开头
);


-- 创建一个表 'review'，用于存储视频审核信息
create table review
(
    bv           char(12),                             -- 视频的唯一识别字符串
    reviewer_mid bigint,                               -- 审核者的用户ID
    review_time  timestamp not null,                   -- 审核时间
    primary key (bv, reviewer_mid),
    foreign key (bv) references videos (bv),           -- 引用 'videos' 表的 'bv'
    foreign key (reviewer_mid) references users (mid), -- 引用 'users' 表的 'mid'
    check ( bv like 'BV%')
);


-- 创建一个存储视频弹幕的表 'danmu'
create table danmu
(
    bv        char(12),                       -- 弹幕所属视频的BV
    mid       bigint,                         -- 弹幕发送者的用户ID
    time      float not null,                 -- 自视频开始以来弹幕显示的时间（秒）
    content   text  not null,                 -- 弹幕的内容
    post_time timestamp,                      -- 弹幕的发布时间
    id        BIGSERIAL PRIMARY KEY,          -- 弹幕的唯一标识符
    foreign key (bv) references videos (bv),  -- 引用 'videos' 表的 'bv'
    foreign key (mid) references users (mid), -- 引用 'users' 表的 'mid'
    check ( bv like 'BV%')
);

-- 创建一个表 'danmu_likes'，用于存储用户对弹幕的点赞信息
create table danmu_likes
(
    id  int,                                  -- 弹幕的唯一标识符
    mid bigint,                               -- 点赞用户的ID
    primary key (mid, id),                    -- 主键为用户ID和弹幕ID的组合
    foreign key (mid) references users (mid), -- 引用 'users' 表的 'mid'
    foreign key (id) references danmu (id)    -- 引用 'danmu' 表的 'id'
);


-- 创建一个表 'likes'，用于存储对视频的点赞信息
create table likes
(
    bv  char(12),                             -- 被点赞视频的BV
    mid bigint,                               -- 点赞用户的ID
    primary key (bv, mid),                    -- 主键为视频BV和用户ID的组合
    foreign key (bv) references videos (bv),  -- 引用 'videos' 表的 'bv'
    foreign key (mid) references users (mid), -- 引用 'users' 表的 'mid'
    check ( bv like 'BV%')
);


-- 创建一个名为 'coin' 的表，用于跟踪用户对视频的硬币打赏情况。
-- 此表确保一个用户只能对一个视频打赏一次。
create table coin
(
    bv  char(12),                             -- 视频的唯一标识符 (BV号)。
    mid bigint,                               -- 打赏用户的唯一识别号 (mid)。
    primary key (bv, mid),                    -- 将 'bv' 和 'mid' 组合作为主键，确保唯一性。
    foreign key (bv) references videos (bv),  -- 'bv' 是视频表的外键。
    foreign key (mid) references users (mid), -- 'mid' 是用户表的外键。
    check (bv like 'BV%')                     -- 检查 'bv' 是否以 'BV' 开头，确保BV号格式正确。
);
-- 创建一个名为 'favorite' 的表，用于记录用户收藏的视频。
create table favorite
(
    bv  char(12),                             -- 视频的唯一标识符 (BV号)。
    mid bigint,                               -- 收藏视频的用户的唯一识别号 (mid)。
    primary key (bv, mid),                    -- 将 'bv' 和 'mid' 组合作为主键，确保唯一性。
    foreign key (bv) references videos (bv),  -- 'bv' 是视频表的外键。
    foreign key (mid) references users (mid), -- 'mid' 是用户表的外键。
    check (bv like 'BV%')                     -- 检查 'bv' 是否以 'BV' 开头，确保BV号格式正确。
);

-- 创建一个名为 'view' 的表，用于记录用户观看视频的情况。
create table view
(
    bv   char(12),                            -- 观看的视频的唯一标识符 (BV号)。
    mid  bigint,                              -- 观看视频的用户的唯一识别号 (mid)。
    time float not null,                      -- 最后一次观看视频的时间戳。
    primary key (bv, mid),                    -- 将 'bv' 和 'mid' 组合作为主键，确保唯一性。
    foreign key (bv) references videos (bv),  -- 'bv' 是视频表的外键。
    foreign key (mid) references users (mid), -- 'mid' 是用户表的外键。
    check (bv like 'BV%')                     -- 检查 'bv' 是否以 'BV' 开头，确保BV号格式正确。
);

GRANT
SELECT,
INSERT
,
UPDATE,
DELETE
ON ALL TABLES IN SCHEMA public TO sustc;
GRANT
CONNECT
ON DATABASE "Project" TO sustc;
GRANT TRUNCATE
ON ALL TABLES IN SCHEMA public TO sustc;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE danmu_id_seq TO sustc;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE users_mid_seq TO sustc;


create function search_videos(keywords text[], user_mid bigint, page_size integer, page_num integer)
    returns TABLE
        (
        bv character,
        title character varying,
        description text,
        owner_name character varying,
        relevance numeric,
        view_count bigint,
        title_match text,
        description_match text,
        owner_name_match text
        )
    language plpgsql
as
$$
BEGIN
    -- 返回查询结果
RETURN QUERY
SELECT v.bv,
       v.title,
       v.description,
       u.name                                                                               AS owner_name,
       (SELECT SUM(title_count + description_count + owner_name_count)
        FROM unnest(keywords) tk
                 LEFT JOIN LATERAL (SELECT COUNT(*) AS title_count
                                    FROM regexp_matches(lower(v.title), tk, 'g')) t ON true
                 LEFT JOIN LATERAL (SELECT COUNT(*) AS description_count
                                    FROM regexp_matches(lower(v.description), tk, 'g')) d ON true
                 LEFT JOIN LATERAL (SELECT COUNT(*) AS owner_name_count
                                    FROM regexp_matches(lower(u.name), tk, 'g')) o ON true) AS relevance,
       (SELECT COUNT(*) FROM view vw WHERE vw.bv = v.bv)                                    AS view_count,
       (SELECT string_agg(DISTINCT tk, ', ')
        FROM unnest(keywords) tk
        WHERE lower(v.title) LIKE '%' || tk || '%' ESCAPE '\')                              AS title_match,
       (SELECT string_agg(DISTINCT tk, ', ')
        FROM unnest(keywords) tk
        WHERE lower(v.description) LIKE '%' || tk || '%' ESCAPE '\')                        AS description_match,
       (SELECT string_agg(DISTINCT tk, ', ')
        FROM unnest(keywords) tk
        WHERE lower(u.name) LIKE '%' || tk || '%' ESCAPE '\')                               AS owner_name_match
FROM videos v
         JOIN users u ON v.owner_mid = u.mid
         LEFT JOIN review r ON v.bv = r.bv
WHERE (r.bv IS NOT NULL OR v.owner_mid = user_mid)
  AND v.public_time <= CURRENT_TIMESTAMP
  AND (SELECT SUM(title_count + description_count + owner_name_count)
       FROM unnest(keywords) tk
                LEFT JOIN LATERAL (SELECT COUNT(*) AS title_count
                                   FROM regexp_matches(lower(v.title), tk, 'g')) t ON true
                LEFT JOIN LATERAL (SELECT COUNT(*) AS description_count
                                   FROM regexp_matches(lower(v.description), tk, 'g')) d ON true
                LEFT JOIN LATERAL (SELECT COUNT(*) AS owner_name_count
                                   FROM regexp_matches(lower(u.name), tk, 'g')) o ON true) > 0
ORDER BY relevance DESC, view_count DESC, bv LIMIT page_size
OFFSET (page_num - 1) * page_size;
END;
$$;

ALTER FUNCTION search_videos(text[], bigint, integer, integer) OWNER TO postgres;

CREATE TRIGGER when_deleting_video
    before delete
    on videos
    for each row execute function same_time_delete();
create function same_time_delete()
    returns trigger as $$BEGIN
delete
from coin
where bv = old.bv;
delete
from danmu_likes
where id in (select id from danmu where bv = old.bv);
delete
from danmu
where bv = old.bv;
delete
from favorite
where bv = old.bv;
delete
from likes
where bv = old.bv;
delete
from review
where bv = old.bv;
delete
from view
where bv = old.bv;
return old;
end;
    $$
language plpgsql;