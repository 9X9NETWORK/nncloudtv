package com.nncloudtv.support;

import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.IncludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.nncloudtv.lib.CacheFactoryTest;
import com.nncloudtv.lib.FacebookLibTest;
import com.nncloudtv.lib.NnUtilStringTest;
import com.nncloudtv.lib.YouTubeLibTest;
import com.nncloudtv.service.MsoConfigManagerTest;
import com.nncloudtv.service.MsoManagerTest;
import com.nncloudtv.service.PlayerApiServiceTest;
import com.nncloudtv.service.PlayerServiceTest;
import com.nncloudtv.web.api.PlayerApiControllerTest;

@RunWith(Categories.class)
@IncludeCategory(NnTestAll.class)
@SuiteClasses({CacheFactoryTest.class,
               FacebookLibTest.class,
               NnUtilStringTest.class,
               YouTubeLibTest.class,
               
               MsoConfigManagerTest.class,
               MsoManagerTest.class,
               PlayerApiServiceTest.class,
               PlayerServiceTest.class,
               
               PlayerApiControllerTest.class})
public class NnTestAllSuite {

}
