alter table rooms add column name varchar(255) not null default 'Room';
alter table rooms add column deck_type varchar(32) not null default 'STORY_POINTS';
alter table rooms add column owner_participant_id uuid;

alter table participants add column role varchar(32) not null default 'PLAYER';
create unique index if not exists uq_participants_room_display_name on participants(room_id, display_name);

drop table if exists votes;
drop table if exists estimation_rounds;

create table estimation_rounds (
    room_id uuid primary key references rooms(id) on delete cascade,
    id uuid not null unique,
    topic varchar(255) not null,
    status varchar(32) not null,
    started_at timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    revealed_at timestamp with time zone null,
    duration_seconds bigint not null
);

create table estimation_round_votes (
    id uuid primary key,
    round_id uuid not null references estimation_rounds(id) on delete cascade,
    participant_id uuid not null,
    vote_value varchar(255) not null
);
create unique index uq_estimation_round_votes_round_participant on estimation_round_votes(round_id, participant_id);

create table round_history (
    id uuid primary key,
    room_id uuid not null references rooms(id) on delete cascade,
    topic varchar(255) not null,
    status varchar(32) not null,
    started_at timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    revealed_at timestamp with time zone null,
    duration_seconds bigint not null
);

create table round_history_votes (
    id uuid primary key,
    round_id uuid not null references round_history(id) on delete cascade,
    participant_id uuid not null,
    vote_value varchar(255) not null
);
create unique index uq_round_history_votes_round_participant on round_history_votes(round_id, participant_id);

create table room_messages (
    id uuid primary key,
    room_id uuid not null references rooms(id) on delete cascade,
    participant_id uuid not null,
    participant_display_name varchar(255) not null,
    content varchar(1000) not null,
    sent_at timestamp with time zone not null
);
