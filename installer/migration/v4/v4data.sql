use nncloudtv_nnuser1;
update nnuser_profile set priv='111111' where userId=11607;

use nncloudtv_content;
# mso_config
insert into mso_config (createDate, item, msoId, updateDate, value) values (now(), 'supported-region', 3, now(), 'zh 中文');
insert into mso_config (createDate, item, msoId, updateDate, value) values (now(), 'supported-region', 4, now(), 'zh 中文');

# re-arrange status
update nnchannel set status=3 where status=0;
update nnchannel set status=0 where id in (select channelId from category_map where categoryId in (select id from category where msoId=3));
update nnchannel set status=0 where id in (select channelId from tag_map where tagId in (select id from tag where name in (select concat(name, '(ctszh)') from nnset where msoId=3)));

truncate systag;
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 1,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 2,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 3,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 4,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 5,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 6,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 7,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 8,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 9,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 10, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 11, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 12, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 13, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 14, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 15, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 16, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 17, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 18, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 19, 1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 1,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 2,  1, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 3,  1, false, now(), now());
# systag: nnset 9x9
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 1,   2, true, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (1, 2,   2, true, now(), now());
# systag: nnset cts
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 1,   2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 2,   2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 3,   2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 4,   2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 5,   2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 6,   2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 7,   2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 8,   2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 9,   2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 10,  2, true,  now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 11,  2, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 22,  2, false, now(), now());
insert into systag(msoId, seq, type, featured, createDate, updateDate) values (3, 33,  2, false, now(), now());
# systag: dashboard 9x9
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 1,  3, false, now(), now(), timeStart, timeEnd, '44'   from dashboard where id=1);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 1,  3, false, now(), now(), timeStart, timeEnd, '38'   from dashboard where id=2);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 1,  3, false, now(), now(), timeStart, timeEnd, '39'   from dashboard where id=3);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 1,  3, false, now(), now(), timeStart, timeEnd, '40'   from dashboard where id=4);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 1,  3, false, now(), now(), timeStart, timeEnd, '41'   from dashboard where id=5);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 1,  3, false, now(), now(), timeStart, timeEnd, '42'   from dashboard where id=6);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 1,  3, false, now(), now(), timeStart, timeEnd, '43'   from dashboard where id=7);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 2,  4, false, now(), now(), timeStart, timeEnd, null  from dashboard where id=8);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 3,  5, false, now(), now(), timeStart, timeEnd, null  from dashboard where id=9);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 1, 4,  6, false, now(), now(), timeStart, timeEnd, null  from dashboard where id=10);

# systag: dashboard tzuchi
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 1,  3, false, now(), now(), timeStart, timeEnd,  '54'  from dashboard where id=11);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 1,  3, false, now(), now(), timeStart, timeEnd,  '48'  from dashboard where id=12);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 1,  3, false, now(), now(), timeStart, timeEnd,  '49'  from dashboard where id=13);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 1,  3, false, now(), now(), timeStart, timeEnd,  '50'  from dashboard where id=14);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 1,  3, false, now(), now(), timeStart, timeEnd,  '51'  from dashboard where id=15);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 1,  3, false, now(), now(), timeStart, timeEnd,  '52'  from dashboard where id=16);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 1,  3, false, now(), now(), timeStart, timeEnd,  '53'  from dashboard where id=17);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 2,  4, false, now(), now(), timeStart, timeEnd,  null from dashboard where id=18);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 3,  5, false, now(), now(), timeStart, timeEnd,  null from dashboard where id=19);
insert into systag(msoId, seq, type, featured, createDate, updateDate, timeStart, timeEnd, attr) (select 4, 4,  6, false, now(), now(), timeStart, timeEnd,  null from dashboard where id=20);

############################################
# systag_display: category 9x9
truncate systag_display;
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 1,  name, null, lang, null, now(), channelCnt from category where id=1);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 2,  name, null, lang, null, now(), channelCnt from category where id=2);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 3,  name, null, lang, null, now(), channelCnt from category where id=3);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 4,  name, null, lang, null, now(), channelCnt from category where id=4);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 5,  name, null, lang, null, now(), channelCnt from category where id=5);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 6,  name, null, lang, null, now(), channelCnt from category where id=6);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 7,  name, null, lang, null, now(), channelCnt from category where id=7);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 8,  name, null, lang, null, now(), channelCnt from category where id=8);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 9,  name, null, lang, null, now(), channelCnt from category where id=9);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 10, name, null, lang, null, now(), channelCnt from category where id=10);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 11, name, null, lang, null, now(), channelCnt from category where id=11);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 12, name, null, lang, null, now(), channelCnt from category where id=12);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 13, name, null, lang, null, now(), channelCnt from category where id=13);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 14, name, null, lang, null, now(), channelCnt from category where id=14);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 15, name, null, lang, null, now(), channelCnt from category where id=15);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 16, name, null, lang, null, now(), channelCnt from category where id=16);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 17, name, null, lang, null, now(), channelCnt from category where id=17);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 18, name, null, lang, null, now(), channelCnt from category where id=18);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 19, name, null, lang, null, now(), channelCnt from category where id=19);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 1,  name, null, lang, null, now(), channelCnt from category where id=20);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 2,  name, null, lang, null, now(), channelCnt from category where id=21);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 3,  name, null, lang, null, now(), channelCnt from category where id=22);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 4,  name, null, lang, null, now(), channelCnt from category where id=23);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 5,  name, null, lang, null, now(), channelCnt from category where id=24);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 6,  name, null, lang, null, now(), channelCnt from category where id=25);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 7,  name, null, lang, null, now(), channelCnt from category where id=26);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 8,  name, null, lang, null, now(), channelCnt from category where id=27);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 9,  name, null, lang, null, now(), channelCnt from category where id=28);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 10, name, null, lang, null, now(), channelCnt from category where id=29);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 11, name, null, lang, null, now(), channelCnt from category where id=30);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 12, name, null, lang, null, now(), channelCnt from category where id=31);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 13, name, null, lang, null, now(), channelCnt from category where id=32);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 14, name, null, lang, null, now(), channelCnt from category where id=33);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 15, name, null, lang, null, now(), channelCnt from category where id=34);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 16, name, null, lang, null, now(), channelCnt from category where id=35);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 17, name, null, lang, null, now(), channelCnt from category where id=36);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 18, name, null, lang, null, now(), channelCnt from category where id=37);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 19, name, null, lang, null, now(), channelCnt from category where id=38);
# systag_display: category cts
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 20, name, null, lang, null, now(), channelCnt from category where msoId = 3 and seq=1);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 21, name, null, lang, null, now(), channelCnt from category where msoId = 3 and seq=2);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 22, name, null, lang, null, now(), channelCnt from category where msoId = 3 and seq=3);
# systag_display: nnset 9x9
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) values (23, 'Internet Daily', null, 'zh', null, now(), 11);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) values (24, 'Music Day',      null, 'zh', null, now(), 22);
# systag_display: nnset cts 
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 25, name, imageUrl, lang, null, now(), cntChannel from nnset where id=1);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 26, name, imageUrl, lang, null, now(), cntChannel from nnset where id=2);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 27, name, imageUrl, lang, null, now(), cntChannel from nnset where id=9);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 28, name, imageUrl, lang, null, now(), cntChannel from nnset where id=3);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 29, name, imageUrl, lang, null, now(), cntChannel from nnset where id=4);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 30, name, imageUrl, lang, null, now(), cntChannel from nnset where id=8);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 31, name, imageUrl, lang, null, now(), cntChannel from nnset where id=10);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 32, name, imageUrl, lang, null, now(), cntChannel from nnset where id=11);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 33, name, imageUrl, lang, null, now(), cntChannel from nnset where id=12);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 34, name, imageUrl, lang, null, now(), cntChannel from nnset where id=13);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 35, name, imageUrl, lang, null, now(), cntChannel from nnset where id=5);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 36, name, imageUrl, lang, null, now(), cntChannel from nnset where id=6);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 37, name, imageUrl, lang, null, now(), cntChannel from nnset where id=7);
# systag_display: dashboard 9x9
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 38,  name, null, "en", null, now(), 9 from dashboard where id=1);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 39,  name, null, "en", null, now(), 9 from dashboard where id=2);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 40,  name, null, "en", null, now(), 9 from dashboard where id=3);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 41,  name, null, "en", null, now(), 9 from dashboard where id=4);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 42,  name, null, "en", null, now(), 9 from dashboard where id=5);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 43,  name, null, "en", null, now(), 9 from dashboard where id=6);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 44,  name, null, "en", null, now(), 9 from dashboard where id=7);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 45,  name, null, "en", null, now(), 9 from dashboard where id=8);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 46,  name, null, "en", null, now(), 9 from dashboard where id=9);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 47,  name, null, "en", null, now(), 9 from dashboard where id=10);

insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 38,  '活力早晨', null, "zh", null, now(), 9 from dashboard where id=1);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 39,  '日間焦點', null, "zh", null, now(), 9 from dashboard where id=2);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 40,  '午後光影', null, "zh", null, now(), 9 from dashboard where id=3);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 41,  '傍晚時分', null, "zh", null, now(), 9 from dashboard where id=4);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 42,  '黃金強檔', null, "zh", null, now(), 9 from dashboard where id=5);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 43,  '夜間精選', null, "zh", null, now(), 9 from dashboard where id=6);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 44,  '午夜小品', null, "zh", null, now(), 9 from dashboard where id=7);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 45,  '前時段',   null, "zh", null, now(), 9 from dashboard where id=8);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 46,  '訂閱',     null, "zh", null, now(), 9 from dashboard where id=9);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 47,  '帳戶',     null, "zh", null, now(), 9 from dashboard where id=10);

# systag_display: dashboard tzuchi
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 48,  '活力早晨', null, "zh", null, now(), 9 from dashboard where id=11);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 49,  '日間焦點', null, "zh", null, now(), 9 from dashboard where id=12);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 50,  '午後光影', null, "zh", null, now(), 9 from dashboard where id=13);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 51,  '傍晚時分', null, "zh", null, now(), 9 from dashboard where id=14);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 52,  '黃金強檔', null, "zh", null, now(), 9 from dashboard where id=15);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 53,  '夜間精選', null, "zh", null, now(), 9 from dashboard where id=16);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 54,  '午夜小品', null, "zh", null, now(), 9 from dashboard where id=17);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 55,  name,       null, "zh", null, now(), 9 from dashboard where id=18);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 56,  name,       null, "zh", null, now(), 9 from dashboard where id=19);
insert into systag_display (systagId, name, imageUrl, lang, popularTag, updateDate, cntChannel) (select 57,  name,       null, "zh", null, now(), 9 from dashboard where id=20);
############################################
# systag_map: category channel map cts
truncate systag_map;
insert into systag_map (systagId, channelId, createDate, updateDate) (select 20, channelId, now(), now() from category_map where categoryId=39);
insert into systag_map (systagId, channelId, createDate, updateDate) (select 21, channelId, now(), now() from category_map where categoryId=52);
insert into systag_map (systagId, channelId, createDate, updateDate) (select 22, channelId, now(), now() from category_map where categoryId=40);

# systag_map: nnset channels, cts
insert into systag_map (systagId, channelId, createDate, updateDate) (select 25, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=1)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 26, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=2)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 27, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=9)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 28, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=3)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 29, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=4)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 30, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=8)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 31, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=10)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 32, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=11)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 33, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=12)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 34, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=13)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 35, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=5)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 36, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=6)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 37, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(name,'(ctszh)') from nnset where id=7)));

#systag_map: nnset 9x9
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 26825, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 8816,  now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 14381, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 14384, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 14383, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 14382, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 8599,  now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 8600,  now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 8601,  now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 8602,  now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (23, 14385, now(), now());

insert into systag_map (systagId, channelId, createDate, updateDate) values (24, 8763, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (24, 8784, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (24, 2659, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (24, 480, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (24, 483, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (24, 8800, now(), now());
insert into systag_map (systagId, channelId, createDate, updateDate) values (24, 8761, now(), now());

# systag_map: dashboard channels 9x9
insert into systag_map (systagId, channelId, createDate, updateDate) (select 38, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(stackName,'(9x9en)') from dashboard where id=1)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 39, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(stackName,'(9x9en)') from dashboard where id=2)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 40, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(stackName,'(9x9en)') from dashboard where id=3)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 41, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(stackName,'(9x9en)') from dashboard where id=4)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 42, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(stackName,'(9x9en)') from dashboard where id=5)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 43, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(stackName,'(9x9en)') from dashboard where id=6)));
insert into systag_map (systagId, channelId, createDate, updateDate) (select 44, channelId, now(), now() from tag_map where tagId in (select id from tag where name = (select concat(stackName,'(9x9en)') from dashboard where id=7)));

# channel status
update nnchannel a, systag_map b set a.status = 0, isPublic=true  where a.id = b.channelId;

