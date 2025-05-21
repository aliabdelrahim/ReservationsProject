INSERT INTO `troupes` (`id`, `name`, `logo_url`) VALUES
(1, 'Les Acrobates', 'acrobates.jpg'),
(2, 'Les Jongleurs', 'jongleurs.jpg'),
(3, 'Les Illusionnistes', 'illusionnistes.jpg'),
(4, 'La Troupe du Nord', 'nord.jpg'),
(5, 'Les Nomades du Rire', 'nomades.jpg');

UPDATE `artists` SET `troupe_id` = 1 WHERE `id` IN (1, 2, 3);
UPDATE `artists` SET `troupe_id` = 2 WHERE `id` IN (4, 5);
UPDATE `artists` SET `troupe_id` = 3 WHERE `id` IN (6, 7, 8);
UPDATE `artists` SET `troupe_id` = 4 WHERE `id` IN (9, 10);
UPDATE `artists` SET `troupe_id` = 5 WHERE `id` IN (11, 12, 13);