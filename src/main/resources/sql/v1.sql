CREATE TABLE `db_info` (
	`version` SMALLINT NOT NULL,
	`update_date` TIMESTAMP NOT NULL,
	`cities_loaded` BIT NOT NULL
);

INSERT INTO `db_info` VALUES (0, CURRENT_TIMESTAMP(), 0);