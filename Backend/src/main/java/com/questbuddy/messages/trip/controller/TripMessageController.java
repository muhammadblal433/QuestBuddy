package com.questbuddy.messages.trip.controller;

import com.questbuddy.messages.trip.dto.TripMessageCreateDTO;
import com.questbuddy.messages.trip.dto.TripMessageEditDTO;
import com.questbuddy.messages.trip.dto.TripMessageResponseDTO;
import com.questbuddy.messages.trip.service.TripMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v9/trips")
public class TripMessageController {

    private final TripMessageService service;

    public TripMessageController(TripMessageService service) {
        this.service = service;
    }

    // GET - List of "limit" messages before "beforeId" for a trip
    @GetMapping("/{tripId}/messages")
    public List<TripMessageResponseDTO> list(@RequestHeader("X-User-Id") Long me,
                                             @PathVariable Long tripId,
                                             @RequestParam(required = false) Long beforeId,
                                             @RequestParam(defaultValue = "50") int limit) {
        return service.list(me, tripId, beforeId, limit);
    }

    // POST - send a message to a trip gc
    @PostMapping("/{tripId}/messages")
    public TripMessageResponseDTO post(@RequestHeader("X-User-Id") Long me,
                                       @PathVariable Long tripId,
                                       @RequestBody @Valid TripMessageCreateDTO in) {
        return service.post(me, tripId, in);
    }


    // PUT - edit a message
    @PutMapping("/{tripId}/messages/{messageId}")
    public TripMessageResponseDTO edit(@RequestHeader("X-User-Id") Long me,
                                       @PathVariable Long tripId,
                                       @PathVariable Long messageId,
                                       @RequestBody @Valid TripMessageEditDTO in) {
        return service.edit(me, tripId, messageId, in);
    }

    // DELETE - delete a message
    @DeleteMapping("/{tripId}/messages/{messageId}")
    public TripMessageResponseDTO delete(@RequestHeader("X-User-Id") Long me,
                                         @PathVariable Long tripId,
                                         @PathVariable Long messageId,
                                         @RequestParam("version") Long version) {
        return service.delete(me, tripId, messageId, version);
    }

    // POST - react to a message
    @PostMapping("/{tripId}/messages/{messageId}/reactions")
    public Map<String, Integer> react(@RequestHeader("X-User-Id") Long me,
                                      @PathVariable Long tripId,
                                      @PathVariable Long messageId,
                                      @RequestBody Map<String, String> body) {
        String emoji = null;
        if (body != null) {
            emoji = body.get("emoji");
        }
        return service.toggleReaction(me, tripId, messageId, emoji);
    }

    // DELETE = delete a reaction
    @DeleteMapping("/{tripId}/messages/{messageId}/reactions/{emoji}")
    public Map<String, Integer> unreact(@RequestHeader("X-User-Id") Long me,
                                        @PathVariable Long tripId,
                                        @PathVariable Long messageId,
                                        @PathVariable String emoji) {
        return service.toggleReaction(me, tripId, messageId, emoji);
    }

    @GetMapping("/messages/ping")
    public String ping() {
        return "ok";
    }
}
