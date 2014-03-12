package com.nncloudtv.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.web.json.cms.Set;

/**
 * This is unit test for ApiMsoService's method, use Mockito mock dependence object.
 * Each test case naming begin with target method name, plus dash and a serial number. 
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiMsoServiceTest {
    
    /** target class for testing */
    private ApiMsoService apiMsoService;
    
    @Mock private SetService setService;
    @Mock private SysTagManager sysTagMngr;
    @Mock private SysTagDisplayManager sysTagDisplayMngr;
    @Mock private SysTagMapManager sysTagMapMngr;
    @Mock private NnChannelManager channelMngr;
    @Mock private StoreService storeService;
    @Mock private StoreListingManager storeListingMngr;
    @Mock private MsoManager msoMngr;
    @Mock private CategoryService categoryService;
    @Mock private MsoConfigManager configMngr;
    
    @Before  
    public void setUp() {  
        apiMsoService = new ApiMsoService(setService, sysTagMngr, sysTagDisplayMngr,
                sysTagMapMngr, channelMngr, storeService, storeListingMngr, msoMngr,
                categoryService, configMngr);
    }
    
    @After
    public void tearDown() {
        setService = null;
        sysTagMngr = null;
        sysTagDisplayMngr = null;
        sysTagMapMngr = null;
        channelMngr = null;
        storeService = null;
        storeListingMngr = null;
        msoMngr = null;
        categoryService = null;
        configMngr = null;
        
        apiMsoService = null;
    }
    
    // if NnSet exist
    @Test
    public void set_0() {
        
        when(setService.findById(anyLong())).thenReturn(new Set());
        
        Set result = apiMsoService.set(anyLong());
        assertNotNull(result);
        
        verify(setService).findById(anyLong());
    }
    
    // if NnSet not exist
    @Test
    public void set_1() {
        
        when(setService.findById(anyLong())).thenReturn(null);
        
        Set result = apiMsoService.set(anyLong());
        assertNull(result);
        
        verify(setService).findById(anyLong());
    }
    
    // if invalid input such as NULL
    @Test
    public void set_2() {
        
        Set result = apiMsoService.set(null);
        assertNull(result);
        
        verifyZeroInteractions(setService);
    }
    
    @Test
    public void setUpdate_0() {
        
        // input arguments
        Long setId = (long) 1;
        String name = "name";
        Short seq = 1;
        String tag = "tag";
        Short sortingType = SysTag.SORT_SEQ;
        
        SysTag sysTag = new SysTag();
        sysTag.setId(setId);
        when(sysTagMngr.findById(setId)).thenReturn(sysTag);
        
        SysTagDisplay sysTagDisplay = new SysTagDisplay();
        when(sysTagDisplayMngr.findBySysTagId(setId)).thenReturn(sysTagDisplay);
        when(sysTagMapMngr.findBySysTagId(setId)).thenReturn(new ArrayList<SysTagMap>());
        
        sysTag.setSeq(seq);
        sysTag.setSorting(sortingType);
        when(sysTagMngr.save(sysTag)).thenReturn(sysTag);
        
        sysTagDisplay.setName(name);
        sysTagDisplay.setPopularTag(tag);
        sysTagDisplay.setCntChannel(0);
        when(sysTagDisplayMngr.save(sysTagDisplay)).thenReturn(sysTagDisplay);
        
        Set expected = new Set();
        expected.setName(name);
        expected.setSeq(seq);
        expected.setTag(tag);
        expected.setSortingType(sortingType);
        expected.setChannelCnt(0);
        when(setService.composeSet(sysTag, sysTagDisplay)).thenReturn(expected);
        
        Set actual = apiMsoService.setUpdate(setId, name, seq, tag, sortingType);
        assertEquals(expected, actual);
        
        verify(sysTagMngr).findById(setId);
        verify(sysTagDisplayMngr).findBySysTagId(setId);
        verify(sysTagMapMngr).findBySysTagId(setId);
        verify(sysTagMngr).save(sysTag);
        verify(sysTagDisplayMngr).save(sysTagDisplay);
        verify(setService).composeSet(sysTag, sysTagDisplay);
    }

}
