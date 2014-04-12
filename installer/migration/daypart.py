#process billboard channels

import urllib, urllib2
import os
from array import *
import time
import MySQLdb
                    
pwd = ""
root = "http://localhost:8080"
                      
themes = ['daypart', 'daytime', 'downtime', 'evening', 'primetime', 'latenight', 'nightowl']
tagIds = [372, 373, 374, 375, 376, 377, 378] 

dbcontent = MySQLdb.connect (host = "localhost",
                             user = "root",
                             passwd = pwd,
                             charset = "utf8",
                             use_unicode = True,
                             db = "nncloudtv_content")

cursor = dbcontent.cursor()
tagId = 0
for theme in themes:      
   print theme + ";tagId:" + str(tagIds[tagId])            
   # submit channels           
   filename = theme + ".txt"                                                                    
   feed = open(filename, "rU")   
   i = 0
   for line in feed:
      data = line.split('\t')                   
      name = data[0]
      name = name.replace(' ', '%20')
      url = data[1]
      #print "name:" + name                                        
      #print "url:" + url        
      posturl = root + "/wd/channelSubmit?url=" + url + "&name=" + name
      posturl = posturl.replace('\n', '')
      print posturl
      cid = urllib2.urlopen(posturl).read()
      print cid   
      # submit tagId and channelId
      try:
         cursor.execute("""
            insert into systag_map(systagId, channelId, createDate, updateDate) values (%s, %s, now(), now());
            """, (tagIds[tagId], cid))
      except MySQLdb.IntegrityError as e:
         print "--->SQL Error: %s" % e  
      i = i+1      
   tagId = tagId + 1
 
print "record done:" + str(i)
feed.close()
dbcontent.commit() 

