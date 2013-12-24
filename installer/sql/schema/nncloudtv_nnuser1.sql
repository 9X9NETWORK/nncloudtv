-- MySQL dump 10.13  Distrib 5.5.34, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: nncloudtv_nnuser1
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
-- Table structure for table `nnguest`
--

DROP TABLE IF EXISTS `nnguest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnguest` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `captchaId` bigint(20) DEFAULT NULL,
  `createDate` datetime DEFAULT NULL,
  `expiredAt` datetime DEFAULT NULL,
  `guessTimes` int(11) NOT NULL,
  `shard` smallint(6) DEFAULT NULL,
  `token` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `type` smallint(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `nnguest_token` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=302507 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnuser`
--

DROP TABLE IF EXISTS `nnuser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cryptedPassword` mediumblob,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `facebookToken` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `isTemp` bit(1) DEFAULT b'0',
  `salt` mediumblob,
  `type` smallint(6) DEFAULT '4',
  `updateDate` datetime DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `shard` smallint(6) DEFAULT NULL,
  `expires` bigint(20) DEFAULT '0',
  `fbId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `userEmail` (`email`),
  KEY `nnuser1_token` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=40257 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnuser_pref`
--

DROP TABLE IF EXISTS `nnuser_pref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnuser_pref` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createDate` datetime DEFAULT NULL,
  `item` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `updateDate` datetime DEFAULT NULL,
  `userId` bigint(20) NOT NULL,
  `value` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `msoId` bigint(20) DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `nnuser_pref_user_id` (`userId`),
  KEY `nnuserpref_msoId` (`msoId`)
) ENGINE=InnoDB AUTO_INCREMENT=2821 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnuser_profile`
--

DROP TABLE IF EXISTS `nnuser_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnuser_profile` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) DEFAULT '1',
  `createDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dob` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `gender` smallint(6) DEFAULT '0',
  `imageUrl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `intro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `lang` varchar(5) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `msoId` bigint(20) DEFAULT '1',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `sphere` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `updateDate` datetime DEFAULT NULL,
  `cntSubscribe` int(11) DEFAULT '0',
  `cntChannel` int(11) DEFAULT '0',
  `cntFollower` int(11) DEFAULT '0',
  `profileUrl` varchar(255) DEFAULT NULL,
  `featured` bit(1) DEFAULT b'0',
  `phoneNumber` varchar(15) DEFAULT NULL,
  `priv` varchar(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `nnuserprofile_userId` (`userId`),
  KEY `nnuserprofile_msoId` (`msoId`),
  KEY `nnuserprofile_profileUrl` (`profileUrl`)
) ENGINE=InnoDB AUTO_INCREMENT=47370 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnuser_subscribe`
--

DROP TABLE IF EXISTS `nnuser_subscribe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnuser_subscribe` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channelId` bigint(20) NOT NULL,
  `createDate` datetime DEFAULT NULL,
  `seq` smallint(6) NOT NULL,
  `type` smallint(6) NOT NULL,
  `userId` bigint(20) NOT NULL,
  `updateDate` datetime DEFAULT NULL,
  `msoId` bigint(20) DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `nnuser_subscribe_channel_id` (`channelId`),
  KEY `nnuser1_subscribe_seq` (`seq`),
  KEY `nnuser1_user_id` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=143849 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnuser_watched`
--

DROP TABLE IF EXISTS `nnuser_watched`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnuser_watched` (
  `channelId` bigint(20) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createDate` datetime DEFAULT NULL,
  `program` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `userToken` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `updateDate` datetime DEFAULT NULL,
  `userId` bigint(20) NOT NULL,
  `msoId` bigint(20) DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `nnuser_watched_channel_id` (`channelId`),
  KEY `nnuser_watched_user_id` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=32320 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-12-24 16:56:54
