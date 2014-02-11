package com.nncloudtv.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.nncloudtv.web.json.cms.Set;

@RunWith(MockitoJUnitRunner.class)
public class ApiMsoServiceTest {
    
    /** 測試對象 */
    private ApiMsoService apiMsoService;
    
    /** mocked SetService */
    @Mock
    private SetService setService;
    
    @Before  
    public void setUp() {  
        apiMsoService = new ApiMsoService(setService, null, null, null, null, null, null, null, null, null);  
    }  
  
    @After  
    public void tearDown() {  
        setService = null;       
    }
    
    @Test
    public void set_tc0() {
        
        when(setService.findById((long) 1)).thenReturn(new Set());
        
        Set result = apiMsoService.set((long) 1);
        assertNotNull(result);
        
        verify(setService).findById((long) 1);
    }
}
