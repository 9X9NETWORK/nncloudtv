-- MySQL dump 10.13  Distrib 5.5.34, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: nncloudtv_content
-- ------------------------------------------------------
-- Server version	5.5.34-0ubuntu0.12.04.1-log

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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=150 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=30259 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=370 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `adId` bigint(20) DEFAULT '0',
  `publishDate` timestamp NULL DEFAULT NULL,
  `updateDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isPublic` bit(1) DEFAULT b'0',
  `duration` int(11) DEFAULT '0',
  `scheduleDate` datetime DEFAULT NULL,
  `seq` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `nnepisode_channelId` (`channelId`)
) ENGINE=InnoDB AUTO_INCREMENT=51834 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=252366 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=996 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=410 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=1005 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=267 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=346 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=11193 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=2920 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=9917 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=10330 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=337499695 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-12-24 16:54:39
