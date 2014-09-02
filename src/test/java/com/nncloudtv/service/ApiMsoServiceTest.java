package com.nncloudtv.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.support.NnTestAll;
import com.nncloudtv.web.json.cms.Category;

/**
 * This is unit test for ApiMsoService's method, use Mockito mock dependence object.
 * Each test case naming begin with target method name, plus dash and a serial number. 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoConfigManager.class, MsoManager.class})
@org.junit.experimental.categories.Category(NnTestAll.class)
public class ApiMsoServiceTest {
    
    protected static final Logger log = Logger.getLogger(ApiMsoServiceTest.class.getName());
    
    /** target class for testing */
    private ApiMsoService apiMsoService;
    
    @Mock private SetService setService;
    @Mock private SysTagManager sysTagMngr;
    @Mock private SysTagMapManager sysTagMapMngr;
    @Mock private NnChannelManager channelMngr;
    @Mock private StoreListingManager storeListingMngr;
    @Mock private MsoManager msoMngr;
    @Mock private CategoryService categoryService;
    @Mock private MsoConfigManager configMngr;
    
    @Before  
    public void setUp() {
        apiMsoService = new ApiMsoService(setService, sysTagMngr,
                sysTagMapMngr, channelMngr, storeListingMngr, msoMngr,
                categoryService, configMngr);
    }
    
    @After
    public void tearDown() {
        setService = null;
        sysTagMngr = null;
        sysTagMapMngr = null;
        channelMngr = null;
        storeListingMngr = null;
        msoMngr = null;
        categoryService = null;
        configMngr = null;
        
        apiMsoService = null;
    }
    
    /**
     * normal case : order by sequence TODO
     */
    @Test
    public void testSetChannelsOrderBySeq() {
        
        // input arguments
        final Long setId = (long) 1;
        
        // mock data
        SysTag systag = new SysTag();
        systag.setId(setId);
        systag.setType(SysTag.TYPE_SET);
        systag.setSorting(SysTag.SORT_SEQ);
        List<NnChannel> channels = new ArrayList<NnChannel>();
        
        // stubs
        
        // execute
        //List<NnChannel> actual = apiMsoService.setChannels(setId);
        
        // verify
        //assertNotNull(actual);
    }
    
    /**
     * normal case : order by update time TODO
     */
    @Test
    public void testSetChannelsOrderByUpdateTime() {
        
        // input arguments
        final Long setId = (long) 1;
        
        // mock data
        SysTag systag = new SysTag();
        systag.setId(setId);
        systag.setType(SysTag.TYPE_SET);
        systag.setSorting(SysTag.SORT_DATE);
        List<NnChannel> channels = new ArrayList<NnChannel>();
        
        // stubs
        
        // execute
        //List<NnChannel> actual = apiMsoService.setChannels(setId);
        
        // verify
        //assertNotNull(actual);
    }
    
    /**
     * missing arguments : setId
     */
    @Test
    public void testSetChannelsMissingArgus() {
        
        // input arguments
        final Long setId = null;
        
        // execute
        List<NnChannel> actual = apiMsoService.setChannels(setId);
        
        // verify
        assertNotNull(actual);
    }
    
    /**
     * normal case
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSetChannelsSorting() {
        
        // input arguments
        final Long setId = (long) 1;
        final List<Long> sortedChannels = new ArrayList<Long>();
        sortedChannels.add((long) 3);
        sortedChannels.add((long) 2);
        sortedChannels.add((long) 1);
        
        // mocks
        List<SysTagMap> setChannels = new ArrayList<SysTagMap>();
        for(int count = 1; count <= 3; count++) {
            SysTagMap sysTagMap = new SysTagMap(setId, count);
            sysTagMap.setSeq((short) count);
            setChannels.add(sysTagMap);
        }
        
        // stubs
        when(sysTagMapMngr.findBySysTagId((Long) anyLong())).thenReturn(setChannels);
        
        // execute
        apiMsoService.setChannelsSorting(setId, sortedChannels);
        
        // verify
        verify(sysTagMapMngr).findBySysTagId(setId);
        
        ArgumentCaptor<List<SysTagMap>> captureSysTagMaps = ArgumentCaptor.forClass((Class) List.class);
        verify(sysTagMapMngr).save(captureSysTagMaps.capture());
        List<SysTagMap> sysTagMaps = captureSysTagMaps.getValue();
        assertEquals(3, sysTagMaps.size());
        assertEquals(3, sysTagMaps.get(0).getChannelId());
        assertEquals(2, sysTagMaps.get(1).getChannelId());
        assertEquals(1, sysTagMaps.get(2).getChannelId());
    }
    
    /**
     * normal case
     */
    @Test
    public void testStoreChannelRemove() {
        
        // input arguments
        final Long msoId = (long) 1;
        final List<Long> channelIds = new ArrayList<Long>();
        channelIds.add(new Long(1));
        
        // execute
        apiMsoService.storeChannelRemove(msoId, channelIds);
        
        // verify
        verify(storeListingMngr).addChannelsToBlackList(channelIds, msoId);
    }
    
    /**
     * missing arguments : msoId, channelIds
     */
    @Test
    public void testStoreChannelRemoveMissingArgus() {
        
        // input arguments
        final Long msoId = null;
        final List<Long> channelIds = null;
        
        // execute
        apiMsoService.storeChannelRemove(msoId, channelIds);
        
        // verify
        verifyZeroInteractions(storeListingMngr);
    }
    
    /**
     * normal case
     */
    @Test
    public void testStoreChannelAdd() {
        
        // input arguments
        final Long msoId = (long) 1;
        final List<Long> channelIds = new ArrayList<Long>();
        channelIds.add(new Long(1));
        
        // execute
        apiMsoService.storeChannelAdd(msoId, channelIds);
        
        // verify
        verify(storeListingMngr).removeChannelsFromBlackList(channelIds, msoId);
    }
    
    /**
     * missing arguments : msoId, channelIds
     */
    @Test
    public void testStoreChannelAddMissingArgus() {
        
        // input arguments
        final Long msoId = null;
        final List<Long> channelIds = null;
        
        // execute
        apiMsoService.storeChannelAdd(msoId, channelIds);
        
        // verify
        verifyZeroInteractions(storeListingMngr);
    }
    
    /**
     * normal case
     */
    @Test
    public void testMso() {
        
        // input argument
        final Long msoId = (long) 1;
        
        // mocks
        Mso mso = new Mso("testName", "testIntro", "testEmail", Mso.TYPE_MSO);
        mso.setId(msoId);
        MsoConfig maxSets = new MsoConfig(msoId, MsoConfig.MAX_SETS, String.valueOf(MsoConfig.MAXSETS_DEFAULT + 1));
        MsoConfig maxChPerSet = new MsoConfig(msoId, MsoConfig.MAX_CH_PER_SET,
                String.valueOf(MsoConfig.MAXCHPERSET_DEFAULT + 1));
        
        // stubs
        when(msoMngr.findById((Long) anyLong(), anyBoolean())).thenReturn(mso);
        when(configMngr.findByMsoAndItem(mso, MsoConfig.MAX_SETS)).thenReturn(maxSets);
        when(configMngr.findByMsoAndItem(mso, MsoConfig.MAX_CH_PER_SET)).thenReturn(maxChPerSet);
        
        PowerMockito.mockStatic(MsoManager.class);
        
        // execute
        Mso actual = apiMsoService.mso(msoId);
        
        // verify
        verify(msoMngr).findById(msoId, true);
        verify(configMngr).findByMsoAndItem(mso, MsoConfig.MAX_SETS);
        verify(configMngr).findByMsoAndItem(mso, MsoConfig.MAX_CH_PER_SET);
        
        PowerMockito.verifyStatic();
        MsoManager.normalize(mso);
        
        assertEquals(MsoConfig.MAXSETS_DEFAULT + 1, actual.getMaxSets());
        assertEquals(MsoConfig.MAXCHPERSET_DEFAULT + 1, actual.getMaxChPerSet());
    }
    
    /**
     * normal case
     */
    @Test
    public void testMsoCategories() {
        
        // input argument
        final Long msoId = (long) 1;
        
        // mocks
        List<Category> categories = new ArrayList<Category>();
        
        // stubs
        when(categoryService.findByMsoId((Long) anyLong())).thenReturn(categories);
        
        // execute
        List<Category> actual = apiMsoService.msoCategories(msoId);
        
        // verify
        verify(categoryService).findByMsoId(msoId);
        assertNotNull(actual);
    }
    
    /**
     * missing arguments : msoId
     */
    @Test
    public void testMsoCategoriesMissingArgus() {
        
        // input argument
        final Long msoId = null;
        
        // execute
        List<Category> actual = apiMsoService.msoCategories(msoId);
        
        // verify
        verifyZeroInteractions(categoryService);
        assertNotNull(actual);
    }
    
    /**
     * normal case
     */
    @Test
    public void testMsoCategoryCreate() {
        
        // input argument
        final Long msoId = (long) 1;
        final Short seq = 1;
        final String zhName = "中文名";
        final String enName ="english name";
        
        // mock
        Category category = new Category();
        
        // stubs
        when(categoryService.create((Category) anyObject())).thenReturn(category);
        
        // execute
        Category actual = apiMsoService.msoCategoryCreate(msoId, seq, zhName, enName);
        
        // verify
        ArgumentCaptor<Category> arg = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).create(arg.capture());
        Category captureCategory = arg.getValue();
        assertEquals(msoId, (Long) captureCategory.getMsoId());
        assertEquals(seq, (Short) captureCategory.getSeq());
        assertEquals(zhName, captureCategory.getZhName());
        assertEquals(enName, captureCategory.getEnName());
        
        assertEquals(category, actual);
    }
    
    /**
     * normal case
     */
    @Test
    public void testCategory() {
        
        // input argument
        final Long categoryId = (long) 1;
        
        // mock
        Category category = new Category();
        
        // stubs
        when(categoryService.findById((Long) anyLong())).thenReturn(category);
        
        // execute
        Category actual = apiMsoService.category(categoryId);
        
        // verify
        verify(categoryService).findById(categoryId);
        assertEquals(category, actual);
    }
    
    /**
     * missing arguments : categoryId
     */
    @Test
    public void testCategoryMissingArgus() {
        
        // input argument
        final Long categoryId = null;
        
        // execute
        Category actual = apiMsoService.category(categoryId);
        
        // verify
        verifyZeroInteractions(categoryService);
        assertNull(actual);
    }
    
    /**
     * normal case
     */
    @Test
    public void testCategoryUpdate() {
        
        // input argument
        final Long categoryId = (long) 1;
        final Short seq = 1;
        final String zhName = "修改的中文名";
        final String enName = "modified english name";
        
        // mocks
        Category category = new Category();
        category.setId(categoryId);
        
        // stubs
        when(categoryService.findById((Long) anyLong())).thenReturn(category);
        when(categoryService.getCntChannel((Long) anyLong())).thenReturn(3);
        when(categoryService.save((Category) anyObject())).thenReturn(category);
        
        // execute
        Category actual = apiMsoService.categoryUpdate(categoryId, seq, zhName, enName);
        
        // verify
        verify(categoryService).findById(categoryId);
        verify(categoryService).getCntChannel(categoryId);
        
        ArgumentCaptor<Category> arg = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).save(arg.capture());
        Category captureCategory = arg.getValue();
        assertEquals(seq, (Short) captureCategory.getSeq());
        assertEquals(zhName, captureCategory.getZhName());
        assertEquals(enName, captureCategory.getEnName());
        assertEquals(3, captureCategory.getCntChannel());
        
        assertEquals(category, actual);
    }

}