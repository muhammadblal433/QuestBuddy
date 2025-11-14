package com.example.androidexample.trips;

public class TripMessageCreateDTO {
    private String content;
    private Long parentMessageId;
    private Long forwardFromMessageId;
    private String clientMessageId;
    private String sentAt;


    public TripMessageCreateDTO(String content, Long parentMessageId, Long forwardFromMessageId,
                                String clientMessageId, String sentAt) {
        this.content = content; this.parentMessageId = parentMessageId;
        this.forwardFromMessageId = forwardFromMessageId; this.clientMessageId = clientMessageId; this.sentAt = sentAt;
    }



    public String getContent() { return content; }
    public Long getParentMessageId() { return parentMessageId; }
    public Long getForwardFromMessageId() { return forwardFromMessageId; }
    public String getClientMessageId() { return clientMessageId; }
    public String getSentAt() { return sentAt; }
}
