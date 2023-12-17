-- 创建一个名为 "Project" 的数据库
-- create
-- database "Project";

-- 定义一个枚举类型 'identity_type'，包含 'user' 和 'superuser' 两个值
CREATE TYPE identity_type AS ENUM ('user', 'superuser');

-- 定义一个枚举类型 'gender_type'，包含 'MALE'、'FEMALE' 和 'UNKNOWN' 三个值
create type gender_type as enum ('MALE', 'FEMALE', 'UNKNOWN');


-- 创建一个表示用户的表 'users'
create table users
(
    mid      bigserial primary key, -- 用户的唯一识别号
    name     varchar(63),        -- 用户创建的名称
    sex      gender_type,        -- 性别，类型为 'gender_type' 枚举
    birthday date,               -- 用户的生日
    level    int,                -- 用户参与度的等级
    sign     text,               -- 用户创建的个人描述
    identity identity_type,      -- 用户角色的值（'user', 'superuser'）
    qq       varchar(10),        -- QQ号，最长10位
    wechat   varchar(20),        -- 微信号，最长20位
    password varchar(63),        -- 登录密码，最长63位
    coin     int                 -- 用户拥有的硬币数量
);


-- 创建一个表示用户之间关注关系的表 'user_follow'
create table user_follow
(
    follow_mid    bigint,                               -- 关注者的用户ID
    follow_by_mid bigint,                               -- 被关注者的用户ID
    primary key (follow_mid, follow_by_mid),
    foreign key (follow_mid) references users (mid),    -- 引用 'users' 表的 'mid'
    foreign key (follow_by_mid) references users (mid), -- 引用 'users' 表的 'mid'
    check (follow_by_mid <> follow_mid)                 -- 检查防止自我关注
);


-- 创建一个表示视频的表 'videos'
create table videos
(
    bv          char(12) primary key,  -- 视频的唯一识别字符串，格式为 'BV%'
    title       varchar(255) not null, -- 视频标题
    owner_mid   bigint       not null, -- 视频所有者的用户ID
    commit_time timestamp    not null, -- 视频提交时间
    public_time timestamp    not null, -- 视频对外公开时间
    duration    integer         not null, -- 视频持续时间（秒）
    description text,                  -- 视频简短介绍
    check ( bv like 'BV%')             -- 确保 'bv' 字段以 'BV' 开头
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
    time      int  not null,                  -- 自视频开始以来弹幕显示的时间（秒）
    content   text not null,                  -- 弹幕的内容
    post_time timestamp,                      -- 弹幕的发布时间
    id        SERIAL PRIMARY KEY,             -- 弹幕的唯一标识符
    foreign key (bv) references videos (bv),  -- 引用 'videos' 表的 'bv'
    foreign key (mid) references users (mid), -- 引用 'users' 表的 'mid'
    check ( bv like 'BV%')
);

-- 创建一个表 'danmu_likes'，用于存储对弹幕的点赞信息
create table danmu_likes
(
    id  int,                                  -- 弹幕的唯一标识符
    bv  char(12),                             -- 弹幕所属视频的BV
    mid bigint,                               -- 点赞用户的ID
    primary key (bv, mid, id),                -- 主键为视频BV、用户ID和弹幕ID的组合
    foreign key (bv) references videos (bv),  -- 引用 'videos' 表的 'bv'
    foreign key (mid) references users (mid), -- 引用 'users' 表的 'mid'
    foreign key (id) references danmu (id),   -- 引用 'danmu' 表的 'id'
    check ( bv like 'BV%')
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

-- 创建一个名为 'collect' 的表，用于记录用户收藏的视频。
create table collect
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
    time timestamp not null,                  -- 最后一次观看视频的时间戳。
    primary key (bv, mid),                    -- 将 'bv' 和 'mid' 组合作为主键，确保唯一性。
    foreign key (bv) references videos (bv),  -- 'bv' 是视频表的外键。
    foreign key (mid) references users (mid), -- 'mid' 是用户表的外键。
    check (bv like 'BV%')                     -- 检查 'bv' 是否以 'BV' 开头，确保BV号格式正确。
);

