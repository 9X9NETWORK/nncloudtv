import urllib, urllib2, json, urlparse
import MySQLdb

msoname = raw_input('mso name:')
ytname = raw_input('youtube name:')
sysType = raw_input('(1)category(2)set') 

#parsing channel meta
url = "http://gdata.youtube.com/feeds/api/users/" + ytname + "?v=2.1&prettyprint=true&alt=json"
print "url:" + url
stream = urllib.urlopen(url)
resp = json.load(stream)
stream.close()
channelname = resp['entry']['title']['$t']
channelthumbnail = resp['entry']['media$thumbnail']['url']
print "channel name:" + channelname
print "channel thumbnail:" + channelthumbnail

#parsing playlist in the channel
foundAll = False
videos = []
gdataUrl = "http://gdata.youtube.com/feeds/api/users/" + ytname + "/playlists?v=2&alt=jsonc&prettyprint=true"
idx = 1
channels = []
while not foundAll:
    url = gdataUrl + '&start-index=' + str(idx)
    print "==>url:" + url
    stream = urllib.urlopen(url)
    resp = json.load(stream)
    stream.close()
    totalItems = resp['data']['totalItems']
    startIndex = resp['data']['startIndex']
    itemsPerPage = resp['data']['itemsPerPage']
    print "==>total:" + str(totalItems) + ";start:" + str(startIndex) + ";itemsPerPage:" + str(itemsPerPage)
    
    items = resp['data']['items']
    for item in items:
        playlistid = item['id']
        name = item['title']
        intro = item['description']
        try:
          thumbnail = item['thumbnail']['hqDefault']
        except KeyError, e:
          print 'I got a KeyError - reason "%s"' % str(e) 
        yturl = "http://www.youtube.com/view_play_list?p=" + playlistid
        print "yturl:" + yturl
        print "name:" + name
        print "intro:" + intro
        print "thumbnail:" + thumbnail
        postUrl = "http://localhost:8080/wd/urlSubmitWithMeta?url=" 
        param = yturl + "&lang=en" + "&name=" + name + "&intro=" + intro + "&imageUrl=" + thumbnail
        postUrl = postUrl + urllib.quote(param.encode('utf8'))
        #postUrl = postUrl + urllib.quote(param)
        print "postUrl:" + postUrl
        response = urllib2.urlopen(postUrl)
        cId = response.readline()
        channels.append(cId)
        print "channel size inside:" + str(len(channels))
        print cId
        print "------------------"
    idx += itemsPerPage - 1
    if idx > totalItems:
        foundAll = True

print "idx: " + str(idx)
print "channel size:" + str(len(channels))
#process channel
ytchannel = "http://www.youtube.com/user/" + ytname
postUrl = "http://localhost:8080/wd/urlSubmit?url=" + ytchannel + "&lang=en&sphere=en"
response = urllib2.urlopen(postUrl)
cId = response.readline()
channels.append(cId)
print "youtube channel:" + str(cId)
print "------------------"

#insert data to db
dbcontent = MySQLdb.connect (host = "localhost",
                             user = "root",
                             passwd = "",
                             charset = "utf8",
                             use_unicode = True,
                             db = "nncloudtv_content")
cursor = dbcontent.cursor()
# get msoId
cursor.execute("""
    select id from mso where name = %s
    """, (msoname))
msoId = cursor.fetchone()[0]
print "msoId: " + str(msoId)

cursor.execute("""
   select systagId from systag_display where name = %s
    """, (channelname))
display = cursor.fetchone()
systagId = 0
if display == None:
   # get seq
   cursor.execute("""
       select max(seq) from systag where msoId = %s and type = %s
       """, (msoId, sysType))
   seq = cursor.fetchone()[0] + 1
   print "max seq: " + str(seq)
   # insert to systag
   cursor.execute("""
       insert into systag (msoId, seq, type, featured, createDate, updateDate) values (%s, %s,  1, false, now(), now());
       """, (msoId, seq))
   systagId = cursor.lastrowid
   print "systagId:" + str(systagId)
   # insert to systag_display
   cursor.execute("""
       insert into systag_display (systagId, name, imageUrl, lang, updateDate, cntChannel) values (%s, %s, %s, 'en', now(), %s);
       """, (systagId, channelname, channelthumbnail, len(channels)))
else:
   systagId = display[0]
   print "existing systag:" + str(systagId) 

# insert to systag_map
for c in channels:
   try:
      cursor.execute("""
         insert into systag_map (systagId, channelId, createDate, updateDate) values (%s, %s, now(), now());
         """, (systagId, c))
   except MySQLdb.IntegrityError as e:
      print "--->SQL Error: %s" % e 
dbcontent.commit()
cursor.close()
  
