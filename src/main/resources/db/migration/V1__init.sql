create table keywords
(
    id   bigint auto_increment
        primary key,
    name varchar(255) not null
);

create table urls
(
    id    bigint auto_increment
        primary key,
    link  varchar(255) null,
    title varchar(255) null
);

create table users
(
    id                 bigint auto_increment
        primary key,
    hashed_secret      varchar(255)                        not null,
    is_active          bit                                 not null,
    secret_fingerprint varchar(64)                         not null,
    user_id            varchar(36)                         not null,
    user_type          enum ('ADMIN', 'ANONYMOUS', 'USER') not null,
    constraint UK6efs5vmce86ymf5q7lmvn2uuf
        unique (user_id),
    constraint UK7glpxhbfrbyqogiic88js2i44
        unique (secret_fingerprint)
);

create table fcm_tokens
(
    id        bigint auto_increment
        primary key,
    fcm_token varchar(255) not null,
    model     varchar(100) not null,
    user_id   bigint       not null,
    constraint FKj2kob865pl9dv5vwrs2pmshjv
        foreign key (user_id) references users (id)
);

create table subscriptions
(
    id                bigint auto_increment
        primary key,
    alias             varchar(255) not null,
    created_at        datetime(6)  not null,
    is_alarm_enabled  bit          not null,
    is_urgent         bit          not null,
    last_seen_post_id varchar(255) not null,
    updated_at        datetime(6)  not null,
    url_id            bigint       not null,
    user_id           bigint       not null,
    constraint FKelfr1qwtvicafly1s4txfwakt
        foreign key (url_id) references urls (id),
    constraint FKhro52ohfqfbay9774bev0qinr
        foreign key (user_id) references users (id)
);

create table subscriptions_keywords
(
    id              bigint auto_increment
        primary key,
    keyword_id      bigint not null,
    subscription_id bigint not null,
    constraint FKd663rlok6i960itxrxd9e3cfg
        foreign key (subscription_id) references subscriptions (id)
            on delete cascade,
    constraint FKdmrlbqfh973g9docerskfty6a
        foreign key (keyword_id) references keywords (id)
);

create table summaries
(
    id              bigint auto_increment
        primary key,
    content         varchar(255) not null,
    created_at      datetime(6)  not null,
    hash_tag        varchar(255) not null,
    is_read         bit          not null,
    post_date       varchar(255) null,
    post_url        varchar(255) not null,
    title           varchar(255) not null,
    updated_at      datetime(6)  not null,
    subscription_id bigint       not null,
    constraint FKij0n0t6mdwys3yjgn1wxcqgbe
        foreign key (subscription_id) references subscriptions (id)
            on delete cascade
);
