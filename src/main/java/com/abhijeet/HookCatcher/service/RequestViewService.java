package com.abhijeet.HookCatcher.service;

import com.abhijeet.HookCatcher.dto.CapturedRequestResponse;
import com.abhijeet.HookCatcher.entity.CapturedRequest;
import com.abhijeet.HookCatcher.entity.EndpointEntity;
import com.abhijeet.HookCatcher.exception.EndpointNotFoundException;
import com.abhijeet.HookCatcher.repository.CapturedRequestRepository;
import com.abhijeet.HookCatcher.repository.EndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestViewService {

    private final EndpointRepository endpointRepository;
    private final CapturedRequestRepository capturedRequestRepository;

    @Transactional(readOnly = true)
    public List<CapturedRequestResponse> listRequests(String username, String slug, String method) {
        EndpointEntity endpoint = endpointRepository.findBySlug(slug)
                .orElseThrow(() -> new EndpointNotFoundException("Endpoint not found: " + slug));

        // ownership check: same 404 as "not found" so other users' slugs aren't revealed
        if (!endpoint.getOwner().getUsername().equals(username)) {
            throw new EndpointNotFoundException("Endpoint not found: " + slug);
        }

        return capturedRequestRepository.findByEndpointSlugOrderByReceivedAtDesc(slug)
                .stream()
                .filter(r -> method == null || method.isBlank() || r.getMethod().equalsIgnoreCase(method))
                .map(this::toResponse)
                .toList();
    }

    private CapturedRequestResponse toResponse(CapturedRequest r) {
        return new CapturedRequestResponse(
                r.getId(),
                r.getMethod(),
                r.getHeaders(),
                r.getQueryParams(),
                r.getBody(),
                r.getContentType(),
                r.getIp(),
                r.getReceivedAt()
        );
    }
}