package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.notifications.NotificationModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotificationModelTest {

    private NotificationModel notification;

    @Before
    public void setUp() {
        notification = new NotificationModel(
                1L,
                100L,
                "Test Title",
                "Test Message",
                "INFO",
                "2025-12-08T10:00:00Z",
                false
        );
    }

    @Test
    public void constructor_initializesAllFields() {
        NotificationModel n = new NotificationModel(
                5L,
                200L,
                "Title",
                "Message",
                "ALERT",
                "2025-01-01T00:00:00Z",
                true
        );

        assertEquals(5L, n.getId());
        assertEquals(200L, n.getRecipientId());
        assertEquals("Title", n.getTitle());
        assertEquals("Message", n.getMessage());
        assertEquals("ALERT", n.getType());
        assertEquals("2025-01-01T00:00:00Z", n.getCreatedAt());
        assertTrue(n.isRead());
    }

    @Test
    public void getId_returnsCorrectId() {
        assertEquals(1L, notification.getId());
    }

    @Test
    public void getRecipientId_returnsCorrectRecipientId() {
        assertEquals(100L, notification.getRecipientId());
    }

    @Test
    public void getTitle_returnsCorrectTitle() {
        assertEquals("Test Title", notification.getTitle());
    }

    @Test
    public void getMessage_returnsCorrectMessage() {
        assertEquals("Test Message", notification.getMessage());
    }

    @Test
    public void getType_returnsCorrectType() {
        assertEquals("INFO", notification.getType());
    }

    @Test
    public void getCreatedAt_returnsCorrectTimestamp() {
        assertEquals("2025-12-08T10:00:00Z", notification.getCreatedAt());
    }

    @Test
    public void isRead_returnsCorrectReadStatus() {
        assertFalse(notification.isRead());
    }

    @Test
    public void setRead_updatesReadStatus() {
        assertFalse(notification.isRead());

        notification.setRead(true);
        assertTrue(notification.isRead());

        notification.setRead(false);
        assertFalse(notification.isRead());
    }

    @Test
    public void notification_withReadTrue_returnsTrue() {
        NotificationModel readNotification = new NotificationModel(
                2L, 100L, "Title", "Message", "INFO", "2025-12-08T10:00:00Z", true
        );

        assertTrue(readNotification.isRead());
    }

    @Test
    public void notification_withReadFalse_returnsFalse() {
        NotificationModel unreadNotification = new NotificationModel(
                3L, 100L, "Title", "Message", "INFO", "2025-12-08T10:00:00Z", false
        );

        assertFalse(unreadNotification.isRead());
    }

    @Test
    public void notification_withEmptyStrings_storesEmptyStrings() {
        NotificationModel emptyNotification = new NotificationModel(
                4L, 100L, "", "", "", "", false
        );

        assertEquals("", emptyNotification.getTitle());
        assertEquals("", emptyNotification.getMessage());
        assertEquals("", emptyNotification.getType());
        assertEquals("", emptyNotification.getCreatedAt());
    }

    @Test
    public void notification_withLongValues_storesLongValues() {
        NotificationModel longNotification = new NotificationModel(
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                "Title",
                "Message",
                "Type",
                "Date",
                false
        );

        assertEquals(Long.MAX_VALUE, longNotification.getId());
        assertEquals(Long.MAX_VALUE, longNotification.getRecipientId());
    }

    @Test
    public void notification_withSpecialCharacters_storesSpecialCharacters() {
        NotificationModel specialNotification = new NotificationModel(
                1L,
                100L,
                "Title with @#$%",
                "Message with <>&",
                "Type!",
                "2025-12-08T10:00:00Z",
                false
        );

        assertEquals("Title with @#$%", specialNotification.getTitle());
        assertEquals("Message with <>&", specialNotification.getMessage());
        assertEquals("Type!", specialNotification.getType());
    }

    @Test
    public void setRead_multipleTimesToTrue_staysTrue() {
        notification.setRead(true);
        notification.setRead(true);
        notification.setRead(true);

        assertTrue(notification.isRead());
    }

    @Test
    public void setRead_multipleTimesToFalse_staysFalse() {
        notification.setRead(true);
        notification.setRead(false);
        notification.setRead(false);
        notification.setRead(false);

        assertFalse(notification.isRead());
    }

    @Test
    public void notification_withDifferentTypes_storesCorrectType() {
        String[] types = {"INFO", "WARNING", "ERROR", "SUCCESS", "ALERT"};

        for (String type : types) {
            NotificationModel n = new NotificationModel(
                    1L, 100L, "Title", "Message", type, "2025-12-08T10:00:00Z", false
            );
            assertEquals(type, n.getType());
        }
    }
}