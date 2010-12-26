CREATE TABLE `IMDB_MOVIE_RATING` (
  `ID` int(10) unsigned NOT NULL auto_increment,
  `TITLE` varchar(255) character set utf8 NOT NULL,
  `SUBTITLE` varchar(255) character set utf8 NOT NULL,
  `YEAR` int(11) NOT NULL,
  `RATING` decimal(10,1) NOT NULL,
  UNIQUE KEY `ID_INDEX` (`ID`),
  KEY `TITLE_INDEX` (`TITLE`)
) ENGINE=MyISAM AUTO_INCREMENT=325851 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
