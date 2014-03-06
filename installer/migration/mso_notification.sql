
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

