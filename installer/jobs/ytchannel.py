# port the latest youtube channel in nnchnanel to ytchannel and avoid the duplication

import urllib, urllib2
import os
from array import *
import MySQLdb
import time, datetime

#!prod! change passwd
dbcontent = MySQLdb.connect (host = "localhost",
                             user = "root",
                             passwd = "",
                             charset = "utf8",
                             use_unicode = True,
                             db = "nncloudtv_content")

#!prod! change to mysql3
#!prod! change passwd
dbrecommend = MySQLdb.connect (host = "localhost",
                          user = "root",
                          passwd = "",
                          charset = "utf8",
                          use_unicode = True,
                          db = "nncloudtv_content")

pwd = os.path.dirname(os.path.realpath(__file__))                                  
lastIdFile = pwd + '/last_id'
f = open(lastIdFile, 'r')       
lastId = f.read()
f.close()
print "lastId:" + lastId

nncursor = dbcontent.cursor()
ytcursor = dbrecommend.cursor()
nncursor.execute("""
   select id, sourceUrl, contentType, imageUrl, intro, lang, name, status, sphere
     from nnchannel
    where contentType = 3 
      and id > %s
      and (status = 0 or status=2 or status=3) 
     """, (lastId)) 

rows = nncursor.fetchall ()
i = 0
for r in rows:
  cId = r[0]
  lastId = cId
  sourceUrl = r[1]
  print "url:" + sourceUrl
  ytcursor.execute("""
    select id 
      from ytchannel
     where sourceUrl = %s
    """, (sourceUrl))    
  count = ytcursor.rowcount
  if count == 0:
    lastId = cId
    contentType = r[2]
    imageUrl = r[3]
    intro = r[4]
    lang = r[5]
    names = r[6]
    print "names:" + names
    name = names.split('|', 1)[0]
    print "name:" + name
    status = r[7]
    sphere = r[8]
    ytcursor.execute("""
       insert into ytchannel (contentType, imageUrl, intro, isPublic, lang, name, sorting, sourceUrl, status, sphere, createDate, updateDate, isTemp)
                      values (%s, %s, %s, true, %s, %s, 1, %s, 0, %s, now(), now(), false)
       """, (contentType, imageUrl, intro, lang, name, sourceUrl, sphere))
    dbrecommend.commit() 
  else:
    print "existed"
  i = i+1
  if i > 50:
    break;

f = open(lastIdFile, 'w')
f.write(str(lastId) + "\n")
f.close()

print "records: " + str(i)
print "lastId: " + str(lastId) 
nncursor.close()
ytcursor.close()
dbcontent.close()
dbrecommend.close()


