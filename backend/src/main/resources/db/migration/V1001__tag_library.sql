-- Numbered V1001 (not V2) because the demo seed already occupies V1000 in existing databases; Flyway rejects
-- a lower version that appears after a higher one has been applied.
-- Library of tags the user has used before, so they can be reused when customising other Pokemon.
-- Names are unique ignoring case ("Favourite" and "favourite" are the same tag).
create table tag (
    id   bigserial   primary key,
    name varchar(30) not null
);

create unique index uq_tag_name_ci on tag (lower(name));

-- Keep tags that already exist on Pokemon saved before this migration.
insert into tag (name)
select distinct on (lower(tag)) tag
from pokemon_tag
order by lower(tag), tag
on conflict do nothing;
