-- drop database "Project";
-- create database "Project";
create table users
(
    mid      bigint primary key,
    name     varchar(63) default 'Reviewer',
    sex      char(2) default '保密',
    birthday date default null,
    level    int default 0,
    sign     text default null,
    identity varchar(9),
    is_reviewer boolean default false
);

create table follow
(
    follow_mid    bigint,
    follow_by_mid bigint,
    primary key (follow_mid, follow_by_mid),
    foreign key (follow_mid) references users (mid),
    foreign key (follow_by_mid) references users (mid)
);

create table videos
(
    bv          char(12) primary key,
    title       varchar(255) not null,
    owner_id    bigint       not null,
    commit_time timestamp    not null,
    public_time timestamp    not null,
    duration    int          not null,
    description text,
    check ( bv like 'BV%')
);

create table review
(
    bv           char(12),
    reviewer_mid bigint,
    review_time  timestamp not null,
    primary key (bv, reviewer_mid),
    foreign key (bv) references videos (bv),
    foreign key (reviewer_mid) references users (mid),
    check ( bv like 'BV%')
);
create table comment
(
    bv      char(12),
    mid     bigint,
    time    double precision not null,
    content text             not null,
    id      SERIAL PRIMARY KEY,
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

create table coins
(
    bv  char(12),
    mid bigint,
    primary key (bv, mid),
    foreign key (bv) references videos (bv),
    foreign key (mid) references users (mid),
    check ( bv like 'BV%')
);

create table favourites
(
    bv  char(12),
    mid bigint,
    primary key (bv, mid),
    foreign key (bv) references videos (bv),
    foreign key (mid) references users (mid),
    check ( bv like 'BV%')
);

create table view
(
    bv  char(12),
    mid bigint,
    time double precision not null,
    primary key (bv, mid),
    foreign key (bv) references videos (bv),
    foreign key (mid) references users (mid),
    check ( bv like 'BV%')
);