/*
 * Generate a SQLite database with one table (USERS) and
 */

CREATE TABLE LoginInformation (
	cardId TEXT UNIQUE,
	username TEXT PRIMARY KEY,
	password TEXT NOT NULL,
	type INTEGER,
	profile Text,
	balance INTEGER,
	profilePicture TEXT
);

INSERT INTO LoginInformation VALUES (999999, "admin", "adminPassword", "admin", "", 0, "");
INSERT INTO LoginInformation VALUES (111111, "user1", "user1Password", "normal", "", 6701, "");
INSERT INTO LoginInformation VALUES (222222, "user2", "user2Password", "normal", "", 5050, "");
INSERT INTO LoginInformation VALUES (333333, "user3", "user3Password", "normal", "", 950, "");
INSERT INTO LoginInformation VALUES (444444, "user4", "user4Password", "normal", "", 162230, "");
INSERT INTO LoginInformation VALUES (555555, "user5", "user5Password", "normal", "", 2147483666, "");
INSERT INTO LoginInformation VALUES (666666, "user6", "user6Password", "normal", "", 2147483666, "");

