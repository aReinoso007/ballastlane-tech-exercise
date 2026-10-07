create table app_user (
    id            bigserial primary key,
    username      varchar(30)  not null unique,
    email         varchar(120) not null unique,
    password_hash varchar(100) not null,
    role          varchar(20)  not null
);

create table pokemon (
    id             integer primary key,
    name           varchar(100) not null,
    height         integer      not null check (height > 0),
    weight         integer      not null check (weight > 0),
    sprite_url     varchar(300),
    category       varchar(100),
    description    varchar(2000),
    -- proprietary fields that motivate replicating Pokemon locally
    localized_name varchar(100),
    region         varchar(50)
);

create table pokemon_ability (
    pokemon_id integer     not null references pokemon (id) on delete cascade,
    position   integer     not null,
    name       varchar(60) not null,
    primary key (pokemon_id, position)
);

create table pokemon_stat (
    pokemon_id integer     not null references pokemon (id) on delete cascade,
    position   integer     not null,
    name       varchar(60) not null,
    base_stat  integer     not null,
    primary key (pokemon_id, position)
);

create table pokemon_tag (
    pokemon_id integer     not null references pokemon (id) on delete cascade,
    position   integer     not null,
    tag        varchar(30) not null,
    primary key (pokemon_id, position)
);

create table pokemon_evolution (
    pokemon_id     integer      not null references pokemon (id) on delete cascade,
    position       integer      not null,
    evolution_id   integer      not null,
    name           varchar(100) not null,
    sprite_url     varchar(300),
    stage          integer      not null,
    evolves_from   varchar(100),
    primary key (pokemon_id, position)
);

create index idx_pokemon_name on pokemon (name);
