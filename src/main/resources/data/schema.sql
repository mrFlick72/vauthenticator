CREATE TABLE ROLE
(
    name        varchar(64)  not null PRIMARY KEY,
    description varchar(255) not null DEFAULT ''
);

CREATE TABLE GROUPS
(
    name        varchar(64)  not null PRIMARY KEY,
    description varchar(255) not null DEFAULT ''
);


CREATE TABLE GROUPS_ROLE
(
    group_name        varchar(64)  not null,
    role_name         varchar(64)  not null,

    FOREIGN KEY (group_name) REFERENCES GROUPS (name) on delete cascade,
    FOREIGN KEY (role_name) REFERENCES ROLE (name) on delete cascade,

    primary key(group_name, role_name)
);

CREATE TABLE ACCOUNT
(
    account_non_expired     boolean      not null default false,
    account_non_locked      boolean      not null default false,
    credentials_non_expired boolean      not null default false,
    enabled                 boolean      not null default false,

    username                varchar(255) not null primary key,
    password                varchar(255) not null,

    email                   varchar(255) not null unique,
    email_verified          boolean      not null default false,

    first_name              varchar(255) not null default '',
    last_name               varchar(255) not null default '',

    birth_date              date,
    phone                   varchar(30)           default '',
    locale                  varchar(10)           default 'en',
    mandatory_action        varchar(100) not null default 'NO_ACTION'
);

CREATE TABLE ACCOUNT_ROLE
(
    account_username varchar(255) not null,
    role_name        varchar(64)  not null,


    FOREIGN KEY (account_username) REFERENCES ACCOUNT (username) on delete cascade,
    FOREIGN KEY (role_name) REFERENCES ROLE (name) on delete cascade
);

CREATE TABLE ACCOUNT_GROUP
(
    account_username varchar(255) not null,
    group_name       varchar(64)  not null,


    FOREIGN KEY (account_username) REFERENCES ACCOUNT (username) on delete cascade,
    FOREIGN KEY (group_name) REFERENCES GROUPS (name) on delete cascade
);

CREATE TABLE KEYS
(
    key_id                        varchar(255) not null primary key,
    master_key_id                 varchar(255) not null,
    key_purpose                   varchar(255) not null,
    key_type                      varchar(255) not null,
    encrypted_private_key         text,
    public_key                    text,
    enabled                       boolean      not null default false,
    key_expiration_date_timestamp bigint       not null default 0
);

CREATE INDEX keys_key_purpose ON KEYS (key_purpose);

CREATE TABLE TICKET
(
    ticket                varchar(255) not null primary key,
    ttl                   bigint       not null default 0,
    user_name             varchar(255) not null,
    client_application_id varchar(255) not null,
    context               text         not null default '{}'
);

CREATE TABLE PASSWORD_HISTORY
(
    user_name  varchar(255) not null,
    created_at bigint       not null default 0,
    password   varchar(255) not null,

    primary key (user_name, password)
);

CREATE TABLE MFA_ACCOUNT_METHODS
(
    user_name          varchar(255) not null,
    mfa_device_id      varchar(255) not null,
    mfa_method         varchar(255) not null,
    mfa_channel        varchar(255) not null,
    key_id             varchar(255) not null,
    associated         varchar(255) not null,
    default_mfa_method boolean default false,

    primary key (user_name, mfa_channel)
);

CREATE INDEX mfa_account_methods_mfa_device_id ON MFA_ACCOUNT_METHODS (mfa_device_id);

CREATE TABLE CLIENT_APPLICATION
(
    client_app_id            varchar(255) not null PRIMARY KEY,
    client_app_name          varchar(255) not null,
    secret                   varchar(255) not null,
    scopes                   text         not null,
    confidential             boolean      not null default true,
    with_pkce                boolean      not null default false,
    authorized_grant_types   varchar(255) not null,
    web_server_redirect_uri  varchar(255) not null,
    allowed_origins          varchar(255) not null,
    access_token_validity    integer      not null default 0,
    refresh_token_validity   integer      not null default 0,
    additional_information   text         not null default '',
    auto_approve             boolean      not null default true,
    post_logout_redirect_uri varchar(255) not null,
    logout_uri               varchar(255) not null
);

CREATE TABLE oauth2_authorization
(
    id                            varchar(100) NOT NULL,
    registered_client_id          varchar(100) NOT NULL,
    principal_name                varchar(200) NOT NULL,
    authorization_grant_type      varchar(100) NOT NULL,
    authorized_scopes             text         DEFAULT NULL,
    attributes                    text         DEFAULT NULL,
    state                         varchar(500) DEFAULT NULL,
    authorization_code_value      text         DEFAULT NULL,
    authorization_code_issued_at  timestamp    DEFAULT NULL,
    authorization_code_expires_at timestamp    DEFAULT NULL,
    authorization_code_metadata   text         DEFAULT NULL,
    access_token_value            text         DEFAULT NULL,
    access_token_issued_at        timestamp    DEFAULT NULL,
    access_token_expires_at       timestamp    DEFAULT NULL,
    access_token_metadata         text         DEFAULT NULL,
    access_token_type             varchar(100) DEFAULT NULL,
    access_token_scopes           text         DEFAULT NULL,
    oidc_id_token_value           text         DEFAULT NULL,
    oidc_id_token_issued_at       timestamp    DEFAULT NULL,
    oidc_id_token_expires_at      timestamp    DEFAULT NULL,
    oidc_id_token_metadata        text         DEFAULT NULL,
    refresh_token_value           text         DEFAULT NULL,
    refresh_token_issued_at       timestamp    DEFAULT NULL,
    refresh_token_expires_at      timestamp    DEFAULT NULL,
    refresh_token_metadata        text         DEFAULT NULL,
    user_code_value               text         DEFAULT NULL,
    user_code_issued_at           timestamp    DEFAULT NULL,
    user_code_expires_at          timestamp    DEFAULT NULL,
    user_code_metadata            text         DEFAULT NULL,
    device_code_value             text         DEFAULT NULL,
    device_code_issued_at         timestamp    DEFAULT NULL,
    device_code_expires_at        timestamp    DEFAULT NULL,
    device_code_metadata          text         DEFAULT NULL,
    PRIMARY KEY (id)
);