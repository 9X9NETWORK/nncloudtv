import urllib, urllib2, json, urlparse
import MySQLdb
import sys, os

pwd = ""

#insert data to db
dbcontent = MySQLdb.connect (host = "localhost",
                             user = "root",
                             passwd = pwd,
                             charset = "utf8",
                             use_unicode = True,
                             db = "nncloudtv_content")
# Crash Course 10,Joey Graceffa 35,The History Channel 36,Collegehumor 37, Smosh 38, Shane 39, The Fine Bros 41, SpinninRec 42
msos = [10, 35, 36, 37, 38, 39, 41, 42]
playlists = [['crashcourse', 'HeadsqueezeTV', 'Vsauce', 'Vsauce2', 'scishow', 'numberphile'],
             ['JoeyGraceffa', 'JoeyGraceffaGames'],
             ['History'],
             [],
             ['Smosh', 'IanH', 'SmoshGames', 'ShutUpCartoons'],
             ['shane', 'ShaneDawsonTV', 'ShaneDawsonTV2'],
             ['TheFineBros', 'TheFineBros2'],
             ['SpinninRec']]

cursor = dbcontent.cursor()
i = 0
for mso in msos:
   print "mso:" + str(mso)
   cursor.execute("""
       select name from mso where id = %s
       """, (mso))
   row = cursor.fetchone()
   name = row[0]
   
   for pl in playlists[i]:
      #python playlist.py 9x9 PewDiePie 1
      command = "python playlist.py " + name + " " + pl + " 1"
      print "command:" + command
      os.system(command)
   print "--------------------" 
   i = i + 1 
