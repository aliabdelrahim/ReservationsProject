ALTER TABLE `representations`
  ADD COLUMN `capacity` INT NULL;

-- Option: capacité par défaut (ex. 150)
UPDATE `representations` SET `capacity` = 150 WHERE `capacity` IS NULL;

ALTER TABLE `representations`
  MODIFY `capacity` INT NOT NULL;
