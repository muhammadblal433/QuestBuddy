package com.questbuddy.billing;

import com.stripe.exception.ApiException;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

import com.stripe.param.checkout.SessionCreateParams;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import com.stripe.exception.ApiException;
import com.stripe.net.HttpHeaders;

@RunWith(SpringRunner.class)
public class StripeServiceTest {

    @Test
    public void testCheckout_success() throws Exception {
        // Mock session
        Session fakeSession = mock(Session.class);
        when(fakeSession.getId()).thenReturn("sess123");
        when(fakeSession.getUrl()).thenReturn("http://stripe.session");

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(fakeSession);

            StripeService service = new StripeService();
            ProductRequest req = new ProductRequest(500L, 1L, "Premium", "usd");

            StripeResponse res = service.checkout(req, 10L);

            assertEquals("success", res.getStatus());
            assertEquals("sess123", res.getSessionId());
            assertEquals("http://stripe.session", res.getSessionUrl());
        }
    }

    @Test
    public void testCheckout_stripeException() throws Exception {
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {

            sessionMock.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new CardException(
                            "fail",
                            "req_123",
                            null,
                            null,
                            null,
                            null,
                            400,
                            null
                    ));

            StripeService service = new StripeService();
            ProductRequest req = new ProductRequest(500L, 1L, "Premium", "usd");

            StripeResponse res = service.checkout(req, 10L);

            assertEquals("error", res.getStatus());
            assertTrue(res.getMessage().contains("Failed"));
            assertNull(res.getSessionId());
            assertNull(res.getSessionUrl());
        }
    }
}
