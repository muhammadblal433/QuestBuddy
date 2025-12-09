package com.questbuddy.notification.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.notification.Notification;
import com.questbuddy.notification.NotificationType;
import com.questbuddy.user.model.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;

import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.class)
public class NotificationBroadcasterTest {

    @Mock
    private ObjectMapper om;

    @InjectMocks
    private NotificationBroadcaster broadcaster;

    @Before
    public void setup() throws Exception {
        when(om.writeValueAsString(any())).thenReturn("{json}");
    }

    @Test
    public void testPublishNew_success() throws Exception {
        Notification n = buildNotification(5L);

        try (MockedStatic<NotificationEndpoint> mocked = mockStatic(NotificationEndpoint.class)) {

            broadcaster.publishNew(n);

            mocked.verify(() ->
                    NotificationEndpoint.sendToUser(eq(5L), anyString()), times(1)
            );
        }
    }

    @Test
    public void testPublishNew_swallowException() throws Exception {
        Notification n = buildNotification(5L);

        try (MockedStatic<NotificationEndpoint> mocked = mockStatic(NotificationEndpoint.class)) {

            mocked.when(() ->
                    NotificationEndpoint.sendToUser(eq(5L), anyString())
            ).thenThrow(new RuntimeException("ws fail"));

            // does NOT throw
            broadcaster.publishNew(n);

            mocked.verify(() ->
                    NotificationEndpoint.sendToUser(eq(5L), anyString()), times(1)
            );
        }
    }

    private Notification buildNotification(Long userId) {
        Notification n = new Notification();
        n.setId(10L);
        n.setTitle("A");
        n.setMessage("B");
        n.setType(NotificationType.INVITE);
        n.setCreatedAt(Instant.now());
        n.setRead(false);

        User u = new User();
        u.setId(userId);
        n.setRecipient(u);

        return n;
    }
}