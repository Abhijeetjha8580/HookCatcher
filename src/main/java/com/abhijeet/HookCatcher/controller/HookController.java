package com.abhijeet.HookCatcher.controller;

import com.abhijeet.HookCatcher.service.CaptureService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HookController {

    private final CaptureService captureService;

    @RequestMapping("/hook/{slug}")
    public ResponseEntity<String> capture(
            @PathVariable String slug,
            @RequestBody(required = false) String body,
            HttpServletRequest request) {

        captureService.capture(slug, body, request);
        return ResponseEntity.ok("Captured");
    }
}