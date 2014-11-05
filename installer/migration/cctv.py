import urllib, urllib2
import os
from array import *
import time
import MySQLdb
                    
pwd = ""
root = "http://localhost:8080"
                      
themes = ['tech_english', 'local_chinese', 'movies_english', 'movies_chinese', 'news_english', 'news_chinese',
          'mechanics_english', 'fashion_english', 'talk_english', 'talk_chinese', 'live_english',
          'live_chinese', 'doc_chinese', 'variety_english', 'variety_chinese', 'newdrama_chinese', 'class_english',
          'food_chinese', 'drama_chinese', 'magazine_english', 'magazine_chinese', 'music_english',
          'headline_chinese']
tagIds = [527, 528, 529, 529, 530, 530, 531, 532, 533, 533, 534, 534, 535, 536, 536, 537, 538, 539, 540, 541, 541, 542, 543]

dbcontent = MySQLdb.connect (host = "localhost",
                             user = "root",
                             passwd = pwd,
                             charset = "utf8",
                             use_unicode = True,
                             db = "nncloudtv_content")

cursor = dbcontent.cursor()
t = 0
for theme in themes:      
   # submit channels           
   filename = theme + ".tsv"   
   lang = theme.split('_')[1]
   if (lang == 'chinese'):
      lang = 'zh'
   elif (lang == 'english'):
      lang = 'en'
   else:
      print "lang=" + lang
      break
   print "lang=" + lang
   feed = open( "cctv/"+ filename, "rU")   
   i = 0
   print "tag id = " + str(tagIds[t])
   for line in feed:
      data = line.split('\t')                   
      name = data[0]
      name = name.replace(' ', '%20')
      url = data[1]
      print "name:" + name                                        
      print "url:" + url        
      posturl = root + "/wd/channelSubmit?url=" + url + "&lang=" + lang + "&sphere=" + lang + "&name=" + name
      posturl = posturl.replace('\n', '')
      print posturl
      cid = urllib2.urlopen(posturl).read()
      print cid   
      # submit tagId and channelId
      try:
         cursor.execute("""
            insert into systag_map(systagId, channelId, createDate, updateDate) values (%s, %s, now(), now());
            """, (tagIds[t], cid))
      except MySQLdb.IntegrityError as e:
         print "--->SQL Error: %s" % e  
      i = i+1      
   t = t + 1
   print "record done:" + str(i)
   feed.close()
   if t == 2:
     break

dbcontent.commit() 

