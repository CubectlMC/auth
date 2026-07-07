create table users (
    id uuid primary key,
    username varchar(64) not null unique,
    password_hash varchar(255) not null,
    status varchar(32) not null,
    last_login_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null,
    deleted_at timestamp
);

create table roles (
    id uuid primary key,
    code varchar(64) not null unique,
    display_name varchar(128) not null,
    description text,
    system_role boolean not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null
);

create table permissions (
    id uuid primary key,
    code varchar(128) not null unique,
    description text,
    created_at timestamp not null
);

create table role_permissions (
    role_id uuid not null,
    permission_id uuid not null,
    created_at timestamp not null default current_timestamp,
    primary key (role_id, permission_id),
    constraint fk_role_permissions_role foreign key (role_id) references roles(id),
    constraint fk_role_permissions_permission foreign key (permission_id) references permissions(id)
);

create table user_roles (
    user_id uuid not null,
    role_id uuid not null,
    assigned_by uuid,
    created_at timestamp not null default current_timestamp,
    primary key (user_id, role_id),
    constraint fk_user_roles_user foreign key (user_id) references users(id),
    constraint fk_user_roles_role foreign key (role_id) references roles(id)
);

create table refresh_tokens (
    id uuid primary key,
    user_id uuid not null,
    token_hash varchar(128) not null unique,
    expires_at timestamp not null,
    revoked_at timestamp,
    created_at timestamp not null,
    constraint fk_refresh_tokens_user foreign key (user_id) references users(id)
);

create index users_status_idx on users(status);
create index refresh_tokens_user_id_idx on refresh_tokens(user_id);
create index refresh_tokens_expires_at_idx on refresh_tokens(expires_at);
