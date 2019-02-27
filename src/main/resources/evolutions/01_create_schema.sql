
-- Users schema

-- !Ups

CREATE TABLE if not exists User (
    id INT NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
);

INSERT INTO User VALUES (1, 'user 1');
INSERT INTO User VALUES (2, 'user 2');

CREATE TABLE if not exists Document (
    id INT NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
);

INSERT INTO Document VALUES (3, 'doc 1');
INSERT INTO Document VALUES (4, 'doc 2');

-- !Downs