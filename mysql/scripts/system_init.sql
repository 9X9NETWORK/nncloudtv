/**
 * This SQL script will initialize db with minimum schema/data that required.
 */

use nncloudtv_content;

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

