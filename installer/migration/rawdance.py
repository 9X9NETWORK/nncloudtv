import urllib, urllib2
import os
from array import *
import time
import MySQLdb

pwd = ""
root = "http://localhost:8080"

setIds = [372, 373, 374, 375, 376, 377, 199, 200]
                                                 
dbcontent = MySQLdb.connect (host = "localhost",
                             user = "root",
                             passwd = pwd,
                             charset = "utf8",
                             use_unicode = True,
                             db = "nncloudtv_content")

cursor = dbcontent.cursor()

filename = 'rawdance/danceurls.tsv'
feed = open(filename, "rU")
urls = dict()
cIds = dict()
for line in feed:
   data = line.split('\t')
   name = data[0]
   url = data[1]
   url = url.replace('\n', '')
   name = name.replace('\n', '')
   urls[name] = url
   posturl = root + "/wd/channelSubmit?url=" + url + "&lang=en&sphere=en"
   print posturl
   posturl = posturl.replace('\n', '')
   cid = urllib2.urlopen(posturl).read()
   print cid
   cIds[name] = cid
r=1
for x in range(1,9):
   filename = "rawdance/r"+ str(r) + ".tsv"
   feed = open(filename, "rU")
   i = 0
   print "x:" + str(x)
   for line in feed:
      if i > 0:
         line = line.replace('\n', '')
         try:
           print "cid:" + cIds[line] 
           #print urls[line]
         except KeyError: 
           print "key error:" + str(i) + ":" + line
          
         print "setId:" + str(setIds[x-1])
         try:
            cursor.execute("""
               insert into systag_map(systagId, channelId, createDate, updateDate) values (%s, %s, now(), now());
               """, (tagIds[tagId], cid))
         except MySQLdb.IntegrityError as e:
               print "--->SQL Error: %s" % e

      i = i + 1
   r = r + 1
feed.close()
dbcontent.commit()


