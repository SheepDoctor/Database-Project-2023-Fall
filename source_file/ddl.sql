-- drop database "Project";
-- create database "Project";
create table users
(
    mid      bigint primary key,
    name     varchar(63),
    sex      char(2) default '保密',
    birthday date,
    level    int,
    sign     text,
    identity varchar(9)
);

create table user_follow
(
    follow_mid    bigint,
    follow_by_mid bigint,
    primary key (follow_mid, follow_by_mid),
    foreign key (follow_mid) references users (mid),
    foreign key (follow_by_mid) references users (mid),
    check(follow_by_mid <> follow_mid)
);

create table videos
(
    bv          char(12) primary key,
    title       varchar(255) not null,
    owner_id    bigint       not null,
    commit_time time         not null,
    public_time time         not null,
    duration    int          not null,
    description text,
    check ( bv like 'BV%')
);

create table reviewer
(
    reviewer_mid bigint primary key
);

create table review
(
    bv           char(12),
    reviewer_mid bigint,
    review_time  time not null,
    primary key (bv, reviewer_mid),
    foreign key (bv) references videos (bv),
    foreign key (reviewer_mid) references reviewer (reviewer_mid),
    check ( bv like 'BV%')
);
create table comment
(
    bv      char(12),
    mid     bigint,
    time    double precision not null,
    content text not null,
    id SERIAL PRIMARY KEY,
    foreign key (bv) references videos (bv),
    foreign key (mid) references users (mid),
    check ( bv like 'BV%')
);

create table likes
(
    bv  char(12),
    mid bigint,
    primary key (bv, mid),
    foreign key (bv) references videos (bv),
    foreign key (mid) references users (mid),
    check ( bv like 'BV%')
);

create table coin
(
    bv      char(12),
    mid     bigint,
    time    double precision not null,
    content text not null,
    primary key (bv, mid),
    foreign key (bv) references videos (bv),
    foreign key (mid) references users (mid),
    check ( bv like 'BV%')
);

create table favorite
(
    bv      char(12),
    mid     bigint,
    time    double precision not null,
    content text not null,
    primary key (bv, mid),
    foreign key (bv) references videos (bv),
    foreign key (mid) references users (mid),
    check ( bv like 'BV%')
);

create table view
(
    bv      char(12),
    mid     bigint,
    time    double precision not null,
    content text not null,
    primary key (bv, mid),
    foreign key (bv) references videos (bv),
    foreign key (mid) references users (mid),
    check ( bv like 'BV%')
);

create table reviewer_follow
(
    follow_mid    bigint,
    follow_by_mid bigint,
    primary key (follow_mid, follow_by_mid),
    foreign key (follow_mid) references reviewer (reviewer_mid),
    foreign key (follow_by_mid) references users (mid)
);