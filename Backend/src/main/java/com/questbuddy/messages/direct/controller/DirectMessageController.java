package com.questbuddy.messages.direct.controller;

import com.questbuddy.messages.direct.dto.DirectMessageCreateDTO;
import com.questbuddy.messages.direct.dto.DirectMessageEditDTO;
import com.questbuddy.messages.direct.dto.DirectMessageResponseDTO;
import com.questbuddy.messages.direct.service.DirectMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v10/direct")
public class DirectMessageController {

    private final DirectMessageService service;

    public DirectMessageController(DirectMessageService service) {
        this.service = service;
    }

    // GET - List of "limit" messages before "beforeId" for a 1:1 conversation
    @GetMapping("/{peerId}/messages")
    public List<DirectMessageResponseDTO> list(@RequestHeader("X-User-Id") Long me,
                                               @PathVariable Long peerId,
                                               @RequestParam(required = false) Long beforeId,
                                               @RequestParam(defaultValue = "50") int limit) {
        return service.list(me, peerId, beforeId, limit);
    }

    // POST - send a direct message
    @PostMapping("/{peerId}/messages")
    public DirectMessageResponseDTO post(@RequestHeader("X-User-Id") Long me,
                                         @PathVariable Long peerId,
                                         @RequestBody @Valid DirectMessageCreateDTO in) {
        return service.post(me, peerId, in);
    }

    // PUT - edit a message
    @PutMapping("/{peerId}/messages/{messageId}")
    public DirectMessageResponseDTO edit(@RequestHeader("X-User-Id") Long me,
                                         @PathVariable Long peerId,
                                         @PathVariable Long messageId,
                                         @RequestBody @Valid DirectMessageEditDTO in) {
        return service.edit(me, peerId, messageId, in);
    }

    // DELETE - delete a message
    @DeleteMapping("/{peerId}/messages/{messageId}")
    public void delete(@RequestHeader("X-User-Id") Long me,
                       @PathVariable Long peerId,
                       @PathVariable Long messageId) {
        service.delete(me, peerId, messageId);
    }

    // POST - toggle a reaction
    @PostMapping("/{peerId}/messages/{messageId}/reactions")
    public Map<String, Integer> react(@RequestHeader("X-User-Id") Long me,
                                      @PathVariable Long peerId,
                                      @PathVariable Long messageId,
                                      @RequestParam String emoji) {
        return service.toggleReaction(me, peerId, messageId, emoji);
    }

    // DELETE - delete a reaction (specific emoji)
    @DeleteMapping("/{peerId}/messages/{messageId}/reactions/{emoji}")
    public Map<String, Integer> unreact(@RequestHeader("X-User-Id") Long me,
                                        @PathVariable Long peerId,
                                        @PathVariable Long messageId,
                                        @PathVariable String emoji) {
        return service.toggleReaction(me, peerId, messageId, emoji);
    }

    // POST - mark as read (recipient only)
    @PostMapping("/{peerId}/messages/{messageId}/read")
    public void markRead(@RequestHeader("X-User-Id") Long me,
                         @PathVariable Long peerId,
                         @PathVariable Long messageId) {
        service.markRead(me, peerId, messageId);
    }

    @GetMapping("/messages/ping")
    public String ping() { return "ok"; }
}
