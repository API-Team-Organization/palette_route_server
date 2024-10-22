USE palette;

CREATE TABLE tbl_user (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    username    VARCHAR(255) NOT NULL,
    birth_date  DATE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    state       ENUM('CREATED', 'ACTIVE', 'DELETED') NOT NULL
);

CREATE TABLE tbl_room (
    id      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title   VARCHAR(50),

    FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE ON UPDATE CASCADE
);
