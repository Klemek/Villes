CREATE TABLE `City` (
	`code` INT NOT NULL,
	`name` VARCHAR(128) NOT NULL,
	`postal_code` INT NOT NULL,
	`geo_lat` DECIMAL(18,15) NOT NULL,
	`geo_long` DECIMAL(18,15) NOT NULL,
	PRIMARY KEY (`code`)
);