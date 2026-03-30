create table rooms (
    id uuid primary key
);

create table participants (
    id uuid primary key,
    room_id uuid not null references rooms(id) on delete cascade,
    display_name varchar(255) not null
);

create table estimation_rounds (
    room_id uuid primary key references rooms(id) on delete cascade,
    topic varchar(255) not null,
    status varchar(32) not null,
    started_at timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    revealed_at timestamp with time zone null
);

create table votes (
    id uuid primary key,
    room_id uuid not null references rooms(id) on delete cascade,
    participant_id uuid not null,
    estimation_value numeric(19, 2) not null
);
