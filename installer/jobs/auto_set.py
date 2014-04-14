import urllib, urllib2, json, urlparse
import MySQLdb
import sys, os

#insert data to db
pwd = ""
host = "localhost"

dbcontent = MySQLdb.connect (host = "localhost",
                             user = "root",
                             passwd = "",
                             charset = "utf8",
                             use_unicode = True,
                             db = "nncloudtv_content")
# Crash Course 10,Joey Graceffa 35,The History Channel 36,Collegehumor 37, Smosh 38, Shane 39, The Fine Bros 41, SpinninRec 42
msos = [10, 35, 36, 37, 38, 39, 41, 42]
playlists = [['crashcourse'],
             ['JoeyGraceffa', 'JoeyGraceffaGames'],
             ['History'],
             [],
             ['Smosh', 'IanH', 'SmoshGames', 'ShutUpCartoons'],
             ['shane', 'ShaneDawsonTV'],
             ['TheFineBros'],
             ['SpinninRec']]

cursor = dbcontent.cursor()
for pl in playlists:
   for p in pl:
     url = "http://gdata.youtube.com/feeds/api/users/" + p + "?v=2.1&prettyprint=true&alt=json"
     print "url:" + url
     stream = urllib.urlopen(url)
     resp = json.load(stream)
     stream.close()
     channelname = resp['entry']['title']['$t']
     cursor.execute("""
        select s.id
          from systag_display d, systag s
         where d.name = %s
           and s.id = d.systagId
           and s.type = 2
          """, (channelname))
     print "channelname: " + channelname
     display = cursor.fetchone()
     if display == None:
        print "<<<< bad >>> " + channelname
     else:
        systagId = display[0]
        print "systagId:" + str(systagId)
        cursor.execute("""
          select n.id, n.updateDate, m.systagId
            from nnchannel n, systag_map m, systag s
           where s.type = 2
             and m.systagId = s.Id
             and m.channelId = n.id
             and m.systagId = %s
           order by n.updateDate desc 
           limit 9
           """, (systagId))
        chrows = cursor.fetchall()
        seq = 1
        for ch in chrows:
           i = seq - 1
           cId = ch[0]
           cursor.execute("""
              select id from systag_map
               where systagId = %s
                 and channelId = %s
                 """, (systagId, cId))
           rows = cursor.fetchall()
           if rows[0] != None:
             print "update seq: systagId: " + str(systagId) + ";cId:" + str(cId)
             cursor.execute("""
               update systag_map
                  set seq = %s
                where systagId = %s and channelId = %s
                """, (seq, systagId, cId))
           else:
              print "change channelId: " + str(cId)
              cursor.execute("""
                 update systag_map 
                    set channelId = %s
                  where systagId = %s and seq = %s
              """, (cId, systagId, seq))
           seq = seq + 1
dbcontent.commit()
cursor.close()

