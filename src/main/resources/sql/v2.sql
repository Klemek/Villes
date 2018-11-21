CREATE TABLE `City` (
	`code` INT NOT NULL,
	`name` VARCHAR(128) NOT NULL,
	`postal_codes` VARCHAR(256) NOT NULL,
	`geo_lat` DECIMAL(19,15) NOT NULL,
	`geo_long` DECIMAL(19,15) NOT NULL,
	PRIMARY KEY (`code`)
);