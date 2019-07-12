-- auto-generated definition
create table oauth_client_details
(
    client_id               varchar(256) not null
        constraint oauth_client_details_pkey
            primary key,
    resource_ids            varchar(256),
    client_secret           varchar(256),
    scope                   varchar(256),
    authorized_grant_types  varchar(256),
    web_server_redirect_uri varchar(256),
    authorities             varchar(256),
    access_token_validity   integer,
    refresh_token_validity  integer,
    additional_information  varchar(4096),
    autoapprove             varchar(256)
);

alter table oauth_client_details
    owner to root;

INSERT INTO public.oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove) VALUES ('client', 'oauth2-resource', '$2a$10$JDYNHlHdtKGB6eZHKx6iauk5dRYc3dqkzDVGZmluaooPXCP5oku.m', 'read,write,trust,openid,profile', 'authorization_code,refresh_token,password', 'http://localhost:8080/site/login/oauth2/code/client', 'ROLE_USER,ROLE_EMPLOYEE', 200, 28800, null, 'true');