-- MySQL dump 10.13  Distrib 5.5.22, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: nncloudtv_content
-- ------------------------------------------------------
-- Server version	5.5.22-0ubuntu1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `app`
--

DROP TABLE IF EXISTS `app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `msoId` bigint(20) DEFAULT '0',
  `name` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `intro` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `imageUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `iosStoreUrl` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `androidStoreUrl` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `sphere` varchar(5) DEFAULT 'en',
  `featured` bit(1) NOT NULL,
  `position1` int(11) NOT NULL DEFAULT '0',
  `position2` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `captcha`
--

DROP TABLE IF EXISTS `captcha`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `captcha` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch` bigint(20) NOT NULL,
  `createDate` datetime DEFAULT NULL,
  `fileName` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `lockedDate` datetime DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `random` double NOT NULL,
  `toBeExpired` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=741 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `counter`
--

DROP TABLE IF EXISTS `counter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `counter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `counterName` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `numShards` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `counterName` (`counterName`)
) ENGINE=InnoDB AUTO_INCREMENT=101901 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `counter_shard`
--

DROP TABLE IF EXISTS `counter_shard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `counter_shard` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `shardNumber` int(11) DEFAULT '0',
  `counterName` varchar(255) DEFAULT NULL,
  `count` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `counter_shard_name` (`counterName`)
) ENGINE=InnoDB AUTO_INCREMENT=108789 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mso_config`
--

DROP TABLE IF EXISTS `mso_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mso_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createDate` datetime DEFAULT NULL,
  `item` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `msoId` bigint(20) NOT NULL,
  `updateDate` datetime DEFAULT NULL,
  `value` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=177 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnchannel`
--

DROP TABLE IF EXISTS `nnchannel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnchannel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contentType` smallint(6) NOT NULL,
  `createDate` datetime DEFAULT NULL,
  `oriName` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `imageUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `intro` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `isPublic` bit(1) NOT NULL,
  `isTemp` bit(1) NOT NULL,
  `lang` varchar(5) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `piwik` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `sorting` smallint(6) NOT NULL,
  `sourceUrl` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `status` smallint(6) NOT NULL,
  `tag` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `transcodingUpdateDate` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `updateDate` datetime DEFAULT NULL,
  `poolType` smallint(6) DEFAULT '0',
  `sphere` varchar(5) DEFAULT 'en',
  `cntSubscribe` int(11) NOT NULL DEFAULT '0',
  `cntEpisode` int(11) NOT NULL DEFAULT '0',
  `userIdStr` varchar(25) DEFAULT NULL,
  `note` varchar(10) DEFAULT NULL,
  `subscribersIdStr` varchar(255) DEFAULT NULL,
  `seq` smallint(6) DEFAULT '0',
  `readonly` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `nnchannel_userIdStr` (`userIdStr`),
  KEY `nnchannel_poolType` (`poolType`)
) ENGINE=InnoDB AUTO_INCREMENT=30643 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnchannel_pref`
--

DROP TABLE IF EXISTS `nnchannel_pref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnchannel_pref` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channelid` bigint(20) NOT NULL,
  `createdate` datetime DEFAULT NULL,
  `item` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `updatedate` datetime DEFAULT NULL,
  `value` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `nnchannel_pref_channel_id` (`channelid`)
) ENGINE=InnoDB AUTO_INCREMENT=834 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nndevice`
--

DROP TABLE IF EXISTS `nndevice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nndevice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createDate` datetime DEFAULT NULL,
  `shard` smallint(6) DEFAULT NULL,
  `token` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `userId` bigint(20) NOT NULL,
  `updateDate` datetime DEFAULT NULL,
  `msoId` bigint(20) DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `nndevice_msoId` (`msoId`)
) ENGINE=InnoDB AUTO_INCREMENT=653833 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnprogram`
--

DROP TABLE IF EXISTS `nnprogram`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnprogram` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `audioFileUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `comment` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `channelId` bigint(20) NOT NULL,
  `contentType` smallint(6) NOT NULL,
  `createDate` datetime DEFAULT NULL,
  `duration` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '0',
  `errorCode` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `imageLargeUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `imageUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `intro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `isPublic` bit(1) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fileUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `seq` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `subSeq` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `status` smallint(6) NOT NULL,
  `storageId` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `updateDate` datetime DEFAULT NULL,
  `publishDate` datetime DEFAULT NULL,
  `startTime` varchar(255) DEFAULT '0',
  `endTime` varchar(255) DEFAULT '0',
  `episodeId` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `nnprogram_channel_id` (`channelId`),
  KEY `nnprogram_episodeId` (`episodeId`)
) ENGINE=InnoDB AUTO_INCREMENT=672390 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `poi`
--

DROP TABLE IF EXISTS `poi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=1009 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `poi_campaign`
--

DROP TABLE IF EXISTS `poi_campaign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `poi_campaign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) DEFAULT '0',
  `msoId` bigint(20) DEFAULT '0',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `startdate` datetime DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  `updatedate` datetime DEFAULT NULL,
  `enddate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=473 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `poi_event`
--

DROP TABLE IF EXISTS `poi_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=1014 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `poi_map`
--

DROP TABLE IF EXISTS `poi_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `poi_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `poiId` bigint(20) NOT NULL,
  `eventId` bigint(20) NOT NULL,
  `updateDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `poiMap` (`poiId`,`eventId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `poi_point`
--

DROP TABLE IF EXISTS `poi_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=1018 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `store_listing`
--

DROP TABLE IF EXISTS `store_listing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `store_listing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channelid` bigint(20) NOT NULL,
  `msoid` bigint(20) NOT NULL,
  `updatedate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `systag`
--

DROP TABLE IF EXISTS `systag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=295 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `systag_display`
--

DROP TABLE IF EXISTS `systag_display`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  PRIMARY KEY (`id`),
  KEY `lang` (`lang`),
  KEY `systag_id` (`systagId`)
) ENGINE=InnoDB AUTO_INCREMENT=383 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `systag_map`
--

DROP TABLE IF EXISTS `systag_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `systagMap` (`systagId`,`channelId`),
  KEY `channel_id` (`channelId`)
) ENGINE=InnoDB AUTO_INCREMENT=11962 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `updateDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2966 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag_map`
--

DROP TABLE IF EXISTS `tag_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tagId` bigint(20) NOT NULL,
  `channelId` bigint(20) NOT NULL,
  `updateDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tagMap` (`tagId`,`channelId`),
  KEY `tagMap_channelId` (`channelId`)
) ENGINE=InnoDB AUTO_INCREMENT=10017 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `title_card`
--

DROP TABLE IF EXISTS `title_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `title_card` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `color` varchar(20) DEFAULT NULL,
  `align` varchar(20) DEFAULT NULL,
  `effect` varchar(20) DEFAULT NULL,
  `bgColor` varchar(20) DEFAULT NULL,
  `channelId` bigint(20) NOT NULL,
  `duration` varchar(255) DEFAULT NULL,
  `message` varchar(2000) DEFAULT NULL,
  `bgImage` varchar(255) DEFAULT NULL,
  `playerSyntax` varchar(500) DEFAULT NULL,
  `size` varchar(20) DEFAULT NULL,
  `style` varchar(20) DEFAULT NULL,
  `type` smallint(6) DEFAULT NULL,
  `updateDate` datetime DEFAULT NULL,
  `programId` bigint(20) DEFAULT NULL,
  `weight` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `title_card_channelId` (`channelId`),
  KEY `title_card_programId` (`programId`)
) ENGINE=InnoDB AUTO_INCREMENT=10536 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ytprogram`
--

DROP TABLE IF EXISTS `ytprogram`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ytprogram` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channelId` bigint(20) NOT NULL,
  `ytUserName` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ytVideoId` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `intro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `imageUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `duration` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '0',
  `updateDate` datetime DEFAULT NULL,
  `crawlDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ytprogram` (`channelId`,`ytVideoId`),
  KEY `yt_channel_id` (`channelId`)
) ENGINE=InnoDB AUTO_INCREMENT=434433861 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

--
-- Table structure for table `mso_notification`
--

DROP TABLE IF EXISTS `mso_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mso_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `message` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `msoid` bigint(20) NOT NULL,
  `publishdate` datetime DEFAULT NULL,
  `scheduledate` datetime DEFAULT NULL,
  `updatedate` datetime DEFAULT NULL,
  `createDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

--
-- Table structure for table `mso_notification`
--

DROP TABLE IF EXISTS `mso_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mso_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `message` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `msoid` bigint(20) NOT NULL,
  `publishdate` datetime DEFAULT NULL,
  `scheduledate` datetime DEFAULT NULL,
  `updatedate` datetime DEFAULT NULL,
  `createDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=820 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

--
-- Table structure for table `nndevice_notification`
--

DROP TABLE IF EXISTS `nndevice_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nndevice_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  `deviceid` bigint(20) NOT NULL,
  `logo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `message` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `read` bit(1) NOT NULL,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `type` smallint(6) NOT NULL,
  `updatedate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=68995 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

-- MySQL dump 10.13  Distrib 5.5.22, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: nncloudtv_content
-- ------------------------------------------------------
-- Server version	5.5.22-0ubuntu1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `mso`
--

DROP TABLE IF EXISTS `mso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  `slogan` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `shortintro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-07-15  6:28:23
-- MySQL dump 10.13  Distrib 5.5.22, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: nncloudtv_content
-- ------------------------------------------------------
-- Server version	5.5.22-0ubuntu1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `mso_promotion`
--

DROP TABLE IF EXISTS `mso_promotion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mso_promotion` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` datetime DEFAULT NULL,
  `link` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `logourl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `msoid` bigint(20) NOT NULL,
  `seq` smallint(6) NOT NULL,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `type` smallint(6) NOT NULL,
  `updatedate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-07-15  6:29:03
-- MySQL dump 10.13  Distrib 5.5.22, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: nncloudtv_content
-- ------------------------------------------------------
-- Server version	5.5.22-0ubuntu1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `nnepisode`
--

DROP TABLE IF EXISTS `nnepisode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnepisode` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `imageUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `intro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `channelId` bigint(20) DEFAULT '0',
  `storageId` bigint(20) DEFAULT NULL,
  `publishDate` timestamp NULL DEFAULT NULL,
  `updateDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isPublic` bit(1) DEFAULT b'0',
  `duration` int(11) DEFAULT '0',
  `scheduleDate` datetime DEFAULT NULL,
  `seq` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `nnepisode_channelId` (`channelId`)
) ENGINE=InnoDB AUTO_INCREMENT=520207 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-07-15  6:53:41
