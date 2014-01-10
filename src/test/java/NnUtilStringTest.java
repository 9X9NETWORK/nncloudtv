import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import com.nncloudtv.lib.NnStringUtil;


public class NnUtilStringTest {
    
    protected static final Logger log = Logger.getLogger(NnUtilStringTest.class.getName());
    
    @Test
    public void testUrlencode() {
        
        String expected = "%5B%20SPA%20SE%20%5D";
        String input = "[ SPA SE ]";
        String output = NnStringUtil.urlencode(input);
        log.info("urlencod('" + input + "') = '" + output + "'");
        
        Assert.assertEquals("Spaces should be replaced by '%20'.", expected, output);
    }
}
