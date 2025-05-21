ALTER TABLE `artists`
ADD COLUMN `troupe_id` INT(11) DEFAULT NULL,
ADD CONSTRAINT `fk_artist_troupe`
  FOREIGN KEY (`troupe_id`)
  REFERENCES `troupes`(`id`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;