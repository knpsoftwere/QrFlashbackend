create schema if not exists personal;
create table if not exists personal.users
(
    id bigserial primary key,
    phone_number varchar(255) not null unique,
    password varchar(255) not null,
    is_active boolean default true
);

create table if not exists personal.establishments
(
    uuid uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(255),
    created_at timestamp default now(),
    status varchar(25),
    language varchar(25),
    address varchar(255)
);


create table if not exists personal.establishment_admins
(
    user_id          bigint not null,
    establishment_id uuid not null,
    constraint pk_user_company primary key (user_id, establishment_id),
    constraint fk_user foreign key (user_id) references personal.users (id) on delete cascade,
    constraint fk_establishment foreign key (establishment_id) references personal.establishments (uuid) on delete cascade
);