package com.example.androidexample;
import org.json.JSONObject;


public class DirectMessageDTO {
    public long id;
    public long senderId;
    public long recipientId;
    public String text;
    public long createdAtEpochMs;
    public boolean edited;
    public JSONObject reactions;


    public static DirectMessageDTO fromJson(JSONObject o) {
        DirectMessageDTO m = new DirectMessageDTO();
        m.id = o.optLong("id");
        m.senderId = o.optLong("senderId");
        m.recipientId = o.optLong("recipientId");
        m.text = o.optString("text");
        m.createdAtEpochMs = o.optLong("createdAt");
        m.edited = o.optBoolean("edited", false);
        m.reactions = o.optJSONObject("reactions");
        return m;
    }
}
