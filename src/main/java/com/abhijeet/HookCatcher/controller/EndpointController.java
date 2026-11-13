package com.abhijeet.HookCatcher.controller;

import com.abhijeet.HookCatcher.dto.CreateEndpointRequest;
import com.abhijeet.HookCatcher.dto.EndpointResponse;
import com.abhijeet.HookCatcher.service.EndpointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndpointController {

    private final EndpointService endpointService;

    @PostMapping
    public ResponseEntity<EndpointResponse> create(
            @Valid @RequestBody(required = false) CreateEndpointRequest request,
            Authentication authentication) {
        EndpointResponse response = endpointService.createEndpoint(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EndpointResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(endpointService.listEndpoints(authentication.getName()));
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> delete(@PathVariable String slug, Authentication authentication) {
        endpointService.deleteEndpoint(authentication.getName(), slug);
        return ResponseEntity.noContent().build();
    }
}