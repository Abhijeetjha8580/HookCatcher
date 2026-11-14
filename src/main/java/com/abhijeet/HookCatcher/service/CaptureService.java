package com.abhijeet.HookCatcher.service;

import com.abhijeet.HookCatcher.entity.CapturedRequest;
import com.abhijeet.HookCatcher.exception.EndpointNotFoundException;
import com.abhijeet.HookCatcher.repository.CapturedRequestRepository;
import com.abhijeet.HookCatcher.repository.EndpointRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CaptureService {

    private final EndpointRepository endpointRepository;
    private final CapturedRequestRepository capturedRequestRepository;

    public void capture(String slug, String body, HttpServletRequest request) {
        if (!endpointRepository.existsBySlug(slug)) {
            throw new EndpointNotFoundException("Endpoint not found: " + slug);
        }

        CapturedRequest captured = new CapturedRequest();
        captured.setEndpointSlug(slug);
        captured.setMethod(request.getMethod());
        captured.setBody(body);
        captured.setContentType(request.getContentType());
        captured.setIp(request.getRemoteAddr());
        captured.setReceivedAt(LocalDateTime.now());
        captured.setHeaders(extractHeaders(request));
        captured.setQueryParams(extractQueryParams(request));

        capturedRequestRepository.save(captured);
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String name : Collections.list(request.getHeaderNames())) {
            headers.put(safeKey(name), request.getHeader(name));
        }
        return headers;
    }

    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        request.getParameterMap().forEach((name, values) ->
                params.put(safeKey(name), String.join(",", values)));
        return params;
    }

    // MongoDB rejects map keys containing dots (and keys starting with $)
    private String safeKey(String key) {
        String cleaned = key.replace(".", "_");
        return cleaned.startsWith("$") ? "_" + cleaned.substring(1) : cleaned;
    }
}