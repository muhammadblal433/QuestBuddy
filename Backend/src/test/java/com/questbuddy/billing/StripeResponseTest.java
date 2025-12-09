package com.questbuddy.billing;

import org.junit.Test;
import static org.junit.Assert.*;

public class StripeResponseTest {

    @Test
    public void testGettersSetters() {
        StripeResponse r = new StripeResponse();
        r.setStatus("ok");
        r.setMessage("created");
        r.setSessionId("abc123");
        r.setSessionUrl("http://url");

        assertEquals("ok", r.getStatus());
        assertEquals("created", r.getMessage());
        assertEquals("abc123", r.getSessionId());
        assertEquals("http://url", r.getSessionUrl());
    }

    @Test
    public void testConstructor() {
        StripeResponse r = new StripeResponse("success", "fine", "id7", "http://x");

        assertEquals("success", r.getStatus());
        assertEquals("fine", r.getMessage());
        assertEquals("id7", r.getSessionId());
        assertEquals("http://x", r.getSessionUrl());
    }
}
