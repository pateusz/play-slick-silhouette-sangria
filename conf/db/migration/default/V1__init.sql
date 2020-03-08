DROP TABLE IF EXISTS users;
CREATE TABLE users(
    id             VARCHAR NOT NULL PRIMARY KEY,
    first_name     VARCHAR NOT NULL,
    last_name      VARCHAR NOT NULL,
    email          VARCHAR NOT NULL,
    is_admin       BOOLEAN NOT NULL
);

insert into users(id, first_name, last_name, email, is_admin)
    values ('testadmin', 'testfirstname', 'testsurname', 'admin@test.com', TRUE);
insert into users(id, first_name, last_name, email, is_admin)
    values ('testuser', 'testfirstname', 'testsurname', 'user@test.com', FALSE);

DROP TABLE IF EXISTS password_info;
CREATE TABLE password_info(
    hasher         VARCHAR NOT NULL,
    password       VARCHAR NOT NULL,
    salt           VARCHAR,
    login_info_id  VARCHAR NOT NULL
);

insert into password_info(hasher, password, salt, login_info_id)
    values ('bcrypt-sha256', '$2a$10$..yEHIO7bmUvWX4r4ypiB.7szrCInBLZesBqKbaZDcNc4S45AH.OK', null, 'testadmin');
insert into password_info(hasher, password, salt, login_info_id)
    values ('bcrypt-sha256', '$2a$10$sHPYXb7H.n1lBu62howGGefsKwaSsVrTXvGqDlcZo4NmPnHzK8khW', null, 'testuser');


DROP TABLE IF EXISTS products;
CREATE TABLE products(
   id               BIGSERIAL PRIMARY KEY,
   name             VARCHAR(255) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS product_opinions;
CREATE TABLE product_opinions(
   id              BIGSERIAL PRIMARY KEY,
   product_id      LONG  NOT NULL,
   text            TEXT NOT NULL,
   FOREIGN KEY (product_id) REFERENCES products(id)
);


INSERT INTO products(id,name) VALUES (1,'First product');
INSERT INTO products(id,name) VALUES (2,'Second product');


INSERT INTO product_opinions(id,product_id,text) VALUES (1, 1, 'good');
INSERT INTO product_opinions(id,product_id,text) VALUES (2, 1, 'very good');
INSERT INTO product_opinions(id,product_id,text) VALUES (3, 2, 'bad');
INSERT INTO product_opinions(id,product_id,text) VALUES (4, 2, 'just dont');

