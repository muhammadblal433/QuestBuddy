package com.questbuddy.messages.guard;

public interface DirectMessagingGate {
    boolean canDM(Long me, Long peerId);
}