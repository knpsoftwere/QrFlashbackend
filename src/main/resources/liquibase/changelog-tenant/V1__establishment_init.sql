create table if not exists table_items
(
    id serial primary key,
    table_number int not null unique,
    qr_code varchar(255) not null unique,
    is_active boolean default true,
    created_at timestamp default current_timestamp
);