users table, residing in airlines database mysql.


>> mysql -u root -p


username: root
pwd: ****


create database airlines;

use airlines;

create table users(user_id int auto_increment primary key, name varchar(255) not null);

describe users;
+---------+--------------+------+-----+---------+----------------+
| Field   | Type         | Null | Key | Default | Extra          |
+---------+--------------+------+-----+---------+----------------+
| user_id | int          | NO   | PRI | NULL    | auto_increment |
| name    | varchar(255) | NO   |     | NULL    |                |
+---------+--------------+------+-----+---------+----------------+

select * from users;

insert into users(name) values("tej");

TRUNCATE TABLE users;

DELETE FROM users; -- First delete all records
ALTER TABLE users AUTO_INCREMENT = 1; -- Then reset the counter




create table seats(id varchar(255) primary key, user_id varchar(255), trip_id varchar(255));
