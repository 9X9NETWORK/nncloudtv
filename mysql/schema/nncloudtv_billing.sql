-- MySQL dump 10.13  Distrib 5.5.22, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: nncloudtv_billing
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
-- Table structure for table `billing_order`
--

DROP TABLE IF EXISTS `billing_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `billing_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cntpayment` int(11) NOT NULL,
  `createdate` datetime DEFAULT NULL,
  `expirydate` datetime DEFAULT NULL,
  `itemid` bigint(20) NOT NULL,
  `note` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `packageid` bigint(20) NOT NULL,
  `paymentmechanism` smallint(6) NOT NULL,
  `profileid` bigint(20) NOT NULL,
  `status` smallint(6) NOT NULL,
  `totalpaymentamount` int(11) NOT NULL,
  `updatedate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `billing_package`
--

DROP TABLE IF EXISTS `billing_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `billing_package` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `chargecycle` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `chargetype` smallint(6) NOT NULL,
  `createdate` datetime DEFAULT NULL,
  `enddate` datetime DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `note` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `price` int(11) NOT NULL,
  `setupfees` int(11) NOT NULL,
  `startedate` datetime DEFAULT NULL,
  `status` smallint(6) NOT NULL,
  `updatedate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `billing_profile`
--

DROP TABLE IF EXISTS `billing_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `billing_profile` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addr1` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `addr2` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cardholdername` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cardremaindigits` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cardstatus` smallint(6) NOT NULL,
  `city` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `country` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `state` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `token` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tokenexpdate` datetime DEFAULT NULL,
  `updatedate` datetime DEFAULT NULL,
  `zip` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ccreforderid` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ccreftransid` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnitem`
--

DROP TABLE IF EXISTS `nnitem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnitem` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `billingplatform` smallint(6) NOT NULL,
  `channelid` bigint(20) NOT NULL,
  `createdate` datetime DEFAULT NULL,
  `msoid` bigint(20) NOT NULL,
  `productidref` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `status` smallint(6) NOT NULL,
  `updatedate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nnpurchase`
--

DROP TABLE IF EXISTS `nnpurchase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nnpurchase` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` datetime DEFAULT NULL,
  `expiredate` datetime DEFAULT NULL,
  `itemid` bigint(20) NOT NULL,
  `purchasetoken` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `status` smallint(6) NOT NULL,
  `updatedate` datetime DEFAULT NULL,
  `useridstr` varchar(25) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `verified` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Dump completed on 2014-10-16  6:11:14
