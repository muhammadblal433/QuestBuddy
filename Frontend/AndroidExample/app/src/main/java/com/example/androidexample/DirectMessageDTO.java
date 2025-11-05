package com.example.androidexample;
import org.json.JSONObject;

public class DirectMessageDTO {
    public long id;
    public long senderId;
    public long recipientId;
    public String text;              // keep this name in your app, but map from "content"
    public long createdAtEpochMs;    // map from sentAt/savedAt if present
    public boolean edited;
    public JSONObject reactions;
    // (optional) include deleted/read flags if you want:
    // public boolean deleted;

    public static DirectMessageDTO fromJson(JSONObject o) {
        DirectMessageDTO m = new DirectMessageDTO();
        m.id = o.optLong("id");
        m.senderId = o.optLong("senderId");
        m.recipientId = o.optLong("recipientId");

        // Server uses "content" (fallback to "text" if your older API ever returns it)
        m.text = o.has("content") ? o.optString("content", "") : o.optString("text", "");

        // Server uses ISO-8601 Instants: sentAt/savedAt. Fallback to legacy "createdAt" long.
        String sentAt  = o.optString("sentAt", null);
        String savedAt = o.optString("savedAt", null);
        if (sentAt != null && !sentAt.isEmpty()) {
            m.createdAtEpochMs = parseInstantMs(sentAt);
        } else if (savedAt != null && !savedAt.isEmpty()) {
            m.createdAtEpochMs = parseInstantMs(savedAt);
        } else {
            m.createdAtEpochMs = o.optLong("createdAt", 0L);
        }

        m.edited = o.optBoolean("edited", false);
        m.reactions = o.optJSONObject("reactions");
        // m.deleted = o.optBoolean("deleted", false); // if you add this field to DTO
        return m;
    }

    private static long parseInstantMs(String iso) {
        try {
            return java.time.Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            return 0L;
        }
    }
}