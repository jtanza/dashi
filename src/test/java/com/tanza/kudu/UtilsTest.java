package com.tanza.kudu;

import com.tanza.kudu.Constants.StatusCode;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jtanza
 */
public class UtilsTest {

    @Test
    public void testIsEmpty() {
        assertFalse(StringUtils.isEmpty("foo"));
        assertFalse(StringUtils.isEmpty(" "));
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    public void testGetResource() {
        try {
            Utils.getResource(null);
        } catch (Exception e) {
            assertTrue(e instanceof RequestException);
            RequestException re = (RequestException) e;
            assertEquals(StatusCode.INTERNAL_SERVER_ERROR, re.getStatusCode());
            return;
        }
        fail();
    }
}