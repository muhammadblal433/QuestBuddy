package com.questbuddy.messages.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.config.SpringContext;
import com.questbuddy.messages.guard.TripMembershipGate;
import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageEditDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import com.questbuddy.messages.trip.service.TripMessageService;
import jakarta.websocket.*;
import jakarta.websocket.server.*;
import org.springframework.stereotype.Component;


@Component
@ServerEndpoint("/ws/ping")
public class WsPing {
    @OnOpen
    public void o(Session s){ try{s.getBasicRemote().sendText("{\"event\":\"HELLO\"}");}catch(Exception ignored){} }
}