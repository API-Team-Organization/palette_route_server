USE mysql;

CREATE USER 'team-api'@'%' IDENTIFIED BY 'PASSWORD';

CREATE DATABASE palette CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

GRANT SELECT, INSERT, UPDATE, DELETE ON palette.* TO 'team-api'@'%';