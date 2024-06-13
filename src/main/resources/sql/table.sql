CREATE TABLE user (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    username    VARCHAR(255) NOT NULL,
    birth_date  DATE NOT NULL,
    password    VARCHAR(255) NOT NULL
);

CREATE TABLE room (
    id      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title   VARCHAR(50),
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE chat (
    id BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    message     TEXT NOT NULL,
    datetime    TIMESTAMP NOT NULL,
    room_id     BIGINT NOT NULL,
    user_id     BIGINT,
    is_ai       BOOL NOT NULL,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE
);