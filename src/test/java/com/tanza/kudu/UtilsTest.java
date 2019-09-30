package com.tanza.kudu;

import com.tanza.kudu.Constants.StatusCode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jtanza
 */
public class UtilsTest {

    @Test
    public void testIsEmpty() {
        assertFalse(Utils.isEmpty("foo"));
        assertFalse(Utils.isEmpty(" "));
        assertTrue(Utils.isEmpty(null));
        assertTrue(Utils.isEmpty(""));
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