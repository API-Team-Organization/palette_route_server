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
    title   VARCHAR(50),
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE tbl_chat (
    id BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    message     TEXT NOT NULL,
    resource    VARCHAR(255),
    datetime    TIMESTAMP NOT NULL,
    room_id     BIGINT NOT NULL,
    user_id     BIGINT,
    is_ai       BOOL NOT NULL,
    FOREIGN KEY (room_id) REFERENCES tbl_room(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE ON UPDATE CASCADE
);
