package com.example.androidexample.messages;
import org.json.JSONObject;

public class DirectMessageDTO {
    public long id;
    public long senderId;
    public long recipientId;

    public String text;              // the content of the message
    public long createdAtEpochMs;    // map from sentAt/savedAt if present
    public boolean edited;
    public boolean deleted;

    public Integer version;
    public JSONObject reactions;
    // (optional) include deleted/read flags if you want:
    // public boolean deleted;

    public static DirectMessageDTO fromJson(JSONObject o) {
        DirectMessageDTO m = new DirectMessageDTO();
        m.id = o.optLong("id");
        m.senderId = o.optLong("senderId");
        m.recipientId = o.optLong("recipientId");

        // content of the message
        m.text   = o.optString("content", "");
        m.edited = o.optBoolean("edited", false);
        m.version = o.has("version") ? o.optInt("version") : null;

        // time when message was sent
        String sentAt  = o.optString("sentAt", null);
        String savedAt = o.optString("savedAt", null);
        if (sentAt != null && !sentAt.isEmpty()) {
            m.createdAtEpochMs = parseInstantMs(sentAt);
        } else if (savedAt != null && !savedAt.isEmpty()) {
            m.createdAtEpochMs = parseInstantMs(savedAt);
        } else {
            m.createdAtEpochMs = o.optLong("createdAt", 0L);
        }

        m.reactions = o.optJSONObject("reactions");
        m.deleted   = o.optBoolean("deleted", false);
        return m;
    }

    private static long parseInstantMs(String iso) {
        try {
            return java.time.Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            return 0L;
        }
    }

    public String reactionsDisplay() {
        if (reactions == null || reactions.length() == 0) return "";
        StringBuilder sb = new StringBuilder();
        java.util.Iterator<String> it = reactions.keys();
        boolean first = true;
        while (it.hasNext()) {
            String emo = it.next();
            int count = reactions.optInt(emo, 0);
            if (count <= 0) continue;
            if (!first) sb.append("   ");
            sb.append(emo).append(" x").append(count);
            first = false;
        }
        return sb.toString();
    }
}