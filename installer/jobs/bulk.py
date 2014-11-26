import os, sys
import re
import urllib, urllib2
from datetime import *
import MySQLdb

password = ""
dbcontent = MySQLdb.connect (host = "localhost",
                             user = "root",
                             passwd = password,
                             charset = "utf8",
                             use_unicode = True,
                             db = "nncloudtv_content")
cursor = dbcontent.cursor()

# get dir list
brand = sys.argv[1]
cursor.execute("""
   select id from nncloudtv_nnuser1.nnuser where email = %s      
       """, brand + '@flipr.tv')
row = cursor.fetchone()
userIdStr = "1-" + str(row[0])

brand = brand + "-9x9-dev"
dirList = brand + ".dirList"
#command = "s3cmd ls s3://" + brand + " | grep 'DIR' | awk '{ print $2 }' > " + dirList
#os.system(command)

jobDate = "2014-01-01 00:00"
f = open(brand + ".jobDate", "rU")
for line in f:
   line = line.rstrip()
   jobDate = datetime.strptime(line, "%Y-%m-%d %H:%M")
f.close()

dateNow = datetime.now().strftime("%Y-%m-%d %H:%M")
f = open(brand + ".jobDate", 'w')
f.write(str(dateNow))
f.close()

dirListFile = open(dirList, "rU")
for line in dirListFile:
   if line.endswith("warehouse/\n"):
      break
   line = line.replace("/\n", "")   
   splits = line.split('/')
   chName = splits[len(splits)-1]
   command = "s3cmd --recursive ls " + line + " | awk '{ print $1,$2 \"\t\" $3 \"\t\" $4 }' > " + chName
   #os.system(command)
   videoListFile = open(chName, "rU")
   videoDic = {}
   nnprogramDic = {}
   for l in videoListFile:
     l = l.replace("\n", "")
     l = l.split('\t')
     fileDateStr = l[0] #2014-09-06 00:41
     fileDate = datetime.strptime(fileDateStr, "%Y-%m-%d %H:%M")
     fileSize = int(l[1])
     filePath = l[2]
     if (fileSize != 0 and fileDate > jobDate):
       video = filePath.split('/')
       video = video[len(video)-1]
       if video.endswith(".mp4"):
          videoDic[video] = video
          print "file to process:" + video
       if video.endswith("README"):
          command = "s3cmd get --force " + filePath + " " + chName + ".readme"
          print command
          #os.system(command) 
   if (len(videoDic) > 0):
      readmeFile = open(chName + ".readme", "rU")
      for l in readmeFile:
         l = l.split(":")
         chRealName = l[1]
         print "chRealName:" + chRealName 
         cursor.execute("""
            select id from nnchannel where name = %s and userIdStr = %s
             """, (chRealName, userIdStr))
         row = cursor.fetchone()
         if row == None:
            cursor.execute("""
               insert into nnchannel (contentType, createDate, intro, imageUrl, isPublic, 
                                      lang, sourceUrl, status, updateDate, sphere, isTemp, sorting, name, userIdStr) values  
                                     (6, now(), null, null, true, 'en', null, 0, now(), 'en', false, 1, %s, %s);
               """, (chRealName, userIdStr))
            chId = cursor.lastrowid
            dbcontent.commit()
         else:
            chId = row[0]
         print "chId:" + str(chId)
         cursor.execute("""
              select fileUrl from nnprogram where channelId=%s
              """, (chId))
         rows = cursor.fetchall()
         programDic = {}
         for r in rows:
            nnprogramDic[r[0]] = r[0]
   for key in videoDic:
      command = "s3cmd mv " + key + " s3://cfvideo." + brand
      print "moving command : " + command  
      os.system(command) 
      #s3cmd mv s3://yourapp3-9x9-dev/_DSC0006-X3.jpg s3://cfvideo.yourapp.test
      if key not in nnprogramDic:
         cursor.execute("""
            insert into nnepisode (name, imageUrl, intro, channelId, storageId, publishDate, updateDate, isPublic, duration, scheduleDate, seq) values 
                                  (%s, null, null, %s,  0, now(), now(), true, 0, null, 1);
            """, (key, chId))
         eId = cursor.lastrowid
         cursor.execute("""
            insert into nnprogram (channelId, contentType, createDate, duration, imageUrl,  intro, isPublic, 
                                   name, fileUrl, seq, subSeq, status, updateDate, publishDate,startTime, endTime, episodeId) values 
                                  (%s, 1, now(), '0', null,  null, true, %s, %s, null, '00000001', 0, now(), now(), 0, 0, %s);
            """, (chId, key, key, eId))
         print "insert " + key
         dbcontent.commit()

