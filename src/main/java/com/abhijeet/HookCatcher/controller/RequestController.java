package com.abhijeet.HookCatcher.controller;

import com.abhijeet.HookCatcher.dto.CapturedRequestResponse;
import com.abhijeet.HookCatcher.dto.ReplayRequest;
import com.abhijeet.HookCatcher.dto.ReplayResponse;
import com.abhijeet.HookCatcher.service.ReplayService;
import com.abhijeet.HookCatcher.service.RequestViewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/endpoints/{slug}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestViewService requestViewService;
    private final ReplayService replayService;
    @GetMapping
    public ResponseEntity<List<CapturedRequestResponse>> list(
            @PathVariable String slug,
            @RequestParam(required = false) String method,
            Authentication authentication) {
        return ResponseEntity.ok(
                requestViewService.listRequests(authentication.getName(), slug, method));
    }

    @PostMapping("/{requestId}/replay")
    public ResponseEntity<ReplayResponse> replay(
            @PathVariable String slug,
            @PathVariable String requestId,
            @Valid @RequestBody ReplayRequest body,
            Authentication authentication) {
        return ResponseEntity.ok(
                replayService.replay(authentication.getName(), slug, requestId, body.getTargetUrl()));
    }
}