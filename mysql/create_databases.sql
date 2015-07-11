-- create DBs --

create database nncloudtv_analytics character set = utf8;
create database nncloudtv_content character set = utf8;
create database nncloudtv_nnuser1 character set = utf8;
create database nncloudtv_nnuser2 character set = utf8;
create database nncloudtv_billing character set = utf8;

use nncloudtv_content;

-- create tables & insert initial data --

CREATE TABLE `mso` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contactEmail` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `createDate` datetime DEFAULT NULL,
  `intro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `jingleUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `logoUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `lang` varchar(5) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `type` smallint(6) NOT NULL,
  `updateDate` datetime DEFAULT NULL,
  `slogan` varchar(255) DEFAULT NULL,
  `shortintro` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8;

INSERT INTO `mso` VALUES (1,'mso@9x9.tv','2011-02-12 00:03:42','Immerse yourself in sets of well curated videos. You can even cast your favorite YouTube channels onto TV with Chromecast, and remote it by just finger flipping!','http://s3.amazonaws.com/9x9ui/videos/opening.swf','http://s3.amazonaws.com/9x9ui/images/logo_9x9.png','9x9','en','9x9.tv',1,'2011-02-12 00:03:42',NULL,NULL);

--

CREATE TABLE `systag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `msoId` bigint(20) DEFAULT '1',
  `type` smallint(6) NOT NULL,
  `seq` smallint(6) NOT NULL DEFAULT '0',
  `featured` bit(1) NOT NULL,
  `timeStart` smallint(6) DEFAULT '0',
  `timeEnd` smallint(6) DEFAULT '0',
  `attr` varchar(10) DEFAULT NULL,
  `createDate` datetime DEFAULT NULL,
  `updateDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `sorting` smallint(6) DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `mso_id` (`msoId`),
  KEY `type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1031 DEFAULT CHARSET=utf8;

INSERT INTO `systag` VALUES (2,1,1,2,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(3,1,1,3,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(4,1,1,4,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(5,1,1,5,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(6,1,1,6,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(7,1,1,7,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(8,1,1,8,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(9,1,1,9,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(10,1,1,10,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(11,1,1,11,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(12,1,1,12,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(13,1,1,13,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(14,1,1,14,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(15,1,1,15,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(16,1,1,16,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(17,1,1,17,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(18,1,1,18,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1),(19,1,1,19,'\0',0,0,NULL,'2013-06-13 18:41:41','2013-06-13 18:41:41',1);

CREATE TABLE `systag_display` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `systagId` bigint(20) DEFAULT '1',
  `cntChannel` int(11) NOT NULL DEFAULT '0',
  `name` varchar(255) DEFAULT NULL,
  `imageUrl` varchar(255) DEFAULT NULL,
  `lang` varchar(5) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `popularTag` varchar(500) DEFAULT NULL,
  `updateDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `imageUrl2` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `bannerImageUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `bannerImageUrl2` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lang` (`lang`),
  KEY `systag_id` (`systagId`)
) ENGINE=InnoDB AUTO_INCREMENT=1435 DEFAULT CHARSET=utf8;

INSERT INTO `systag_display` VALUES (2,2,16,'Animals & Pets','http://i1.ytimg.com/vi/8-0WVfj76bo/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(3,3,18,'Art & Design','http://i1.ytimg.com/vi/FKxaL8Iau8Q/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(4,4,35,'Autos & Vehicles','http://i.ytimg.com/vi/lT5yB-EWJsI/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(5,5,18,'Cartoons & Animation','http://i1.ytimg.com/vi/gGwg0-t6uzU/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(6,6,25,'Comedy','http://i1.ytimg.com/vi/JQvokR9ra70/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(7,7,83,'Fashion, Food & Living','http://i1.ytimg.com/vi/uju7XmXxNqM/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(8,8,45,'Gaming','http://i1.ytimg.com/vi/SSIgIRYyPmI/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(9,9,31,'How-To','http://i1.ytimg.com/vi/jrBP3Ixk-CE/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(10,10,29,'Education & Lectures','http://i1.ytimg.com/vi/ADfjrh12L38/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(11,11,223,'Music','http://i1.ytimg.com/vi/0h7KGgkMkcg/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(12,12,45,'News','http://i1.ytimg.com/vi/xK3Y73PZnrc/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(13,13,7,'Nonprofits & Faith','http://i1.ytimg.com/vi/RcepvdxUvf0/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(14,14,92,'People, Blogs & Shorts','http://i1.ytimg.com/vi/X5rZm-oF2Y0/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(15,15,6,'Science','http://i1.ytimg.com/vi/JQvokR9ra70/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(16,16,92,'Sports & Health','http://i1.ytimg.com/vi/OmliBRZUxnk/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(17,17,40,'Tech & Apps','http://i1.ytimg.com/vi/OmliBRZUxnk/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(18,18,82,'TV & Film','http://i1.ytimg.com/vi/T5pgn-zmInE/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(19,19,0,'Others','http://i1.ytimg.com/vi/Lts-dRhtu6I/mqdefault.jpg','en',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(21,2,18,'寵物與動物','http://i.ytimg.com/vi/Sy51KTUbLr4/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(22,3,22,'藝術設計','http://i.ytimg.com/vi/Aft08WF6ulQ/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(23,4,36,'動力機械','http://i.ytimg.com/vi/r0_mmvqoFJY/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(24,5,16,'卡通動畫','http://i1.ytimg.com/vi/FjCmQ69BaIs/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(25,6,24,'搞笑小品','http://i.ytimg.com/vi/vwY5Ad1oL4U/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(26,7,97,'時尚美食與生活','http://i1.ytimg.com/vi/2ojo0YquFtw/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(27,8,47,'電玩遊戲','http://i.ytimg.com/vi/L69Wth7r9sE/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(28,9,32,'DIY教學','http://i1.ytimg.com/vi/MyVppitaxfk/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(29,10,84,'教育講座','http://i1.ytimg.com/vi/MyVppitaxfk/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(30,11,263,'音樂','http://i1.ytimg.com/vi/ZWKz5w1n8hE/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(31,12,45,'新聞','http://i1.ytimg.com/vi/9wSksvGcFUU/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(32,13,11,'宗教與非營利','http://i1.ytimg.com/vi/AVsLyTVfqEo/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(33,14,82,'人物與網誌','http://i.ytimg.com/vi/BZlOUElJ9z8/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(34,15,3,'科學知識','http://i.ytimg.com/vi/niTGIaQOAxQ/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(35,16,96,'運動與健康','http://i.ytimg.com/vi/mWRsgZuwf_8/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(36,17,41,'科技3C','http://i.ytimg.com/vi/tUl-INcCEmc/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(37,18,173,'電視與電影','http://i.ytimg.com/vi/v1rh-ifeqoY/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL),(38,19,136,'其他','http://i.ytimg.com/vi/d2uJ95DlRF8/mqdefault.jpg','zh',NULL,'2013-06-13 18:41:41',NULL,NULL,NULL);

CREATE TABLE `systag_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `systagId` bigint(20) NOT NULL,
  `channelId` bigint(20) NOT NULL,
  `seq` smallint(6) NOT NULL DEFAULT '0',
  `createDate` datetime DEFAULT NULL,
  `updateDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `timeStart` smallint(6) DEFAULT '0',
  `timeEnd` smallint(6) DEFAULT '0',
  `attr` varchar(10) DEFAULT NULL,
  `alwaysOnTop` bit(1) DEFAULT b'0',
  `featured` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `systagMap` (`systagId`,`channelId`),
  KEY `channel_id` (`channelId`)
) ENGINE=InnoDB AUTO_INCREMENT=35626 DEFAULT CHARSET=utf8;

--

CREATE TABLE `nnepisode` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `imageUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `intro` varchar(5000) DEFAULT NULL,
  `channelId` bigint(20) DEFAULT '0',
  `storageId` bigint(20) DEFAULT '0',
  `publishDate` timestamp NULL DEFAULT NULL,
  `updateDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isPublic` bit(1) DEFAULT b'0',
  `duration` int(11) DEFAULT '0',
  `scheduleDate` datetime DEFAULT NULL,
  `seq` int(11) DEFAULT '0',
  `contentType` smallint(6) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `nnepisode_channelId` (`channelId`)
)

--

CREATE TABLE `poi_point` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `targetId` bigint(20) DEFAULT '0',
  `type` smallint(6) NOT NULL,
  `startTime` varchar(255) DEFAULT '0',
  `endTime` varchar(255) DEFAULT '0',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `createDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `updateDate` datetime DEFAULT NULL,
  `tag` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1499 DEFAULT CHARSET=utf8;

CREATE TABLE `poi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `programId` bigint(20) DEFAULT '0',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `intro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `startTime` varchar(255) DEFAULT '0',
  `endTime` varchar(255) DEFAULT '0',
  `createDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateDate` datetime DEFAULT NULL,
  `tag` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `campaignId` bigint(20) DEFAULT '0',
  `eventId` bigint(20) DEFAULT '0',
  `pointId` bigint(20) DEFAULT '0',
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `hoursOfWeek` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `nnprogram_program_id` (`programId`)
) ENGINE=InnoDB AUTO_INCREMENT=1488 DEFAULT CHARSET=utf8;

CREATE TABLE `poi_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` smallint(6) DEFAULT '0',
  `message` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `context` varchar(2000) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `createDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateDate` datetime DEFAULT NULL,
  `userId` bigint(20) DEFAULT '1',
  `msoId` bigint(20) DEFAULT '1',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `notifyMsg` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `notifyScheduler` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1493 DEFAULT CHARSET=utf8;

