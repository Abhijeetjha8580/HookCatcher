package com.abhijeet.HookCatcher.service;

import com.abhijeet.HookCatcher.dto.CreateEndpointRequest;
import com.abhijeet.HookCatcher.dto.EndpointResponse;
import com.abhijeet.HookCatcher.entity.EndpointEntity;
import com.abhijeet.HookCatcher.entity.UserEntity;
import com.abhijeet.HookCatcher.exception.EndpointNotFoundException;
import com.abhijeet.HookCatcher.repository.EndpointRepository;
import com.abhijeet.HookCatcher.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EndpointService {

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SLUG_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EndpointRepository endpointRepository;
    private final UserRepository userRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public EndpointResponse createEndpoint(String username, CreateEndpointRequest request) {
        UserEntity owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        EndpointEntity endpoint = new EndpointEntity();
        endpoint.setSlug(generateSlug());
        endpoint.setLabel(request == null ? null : request.getLabel());
        endpoint.setOwner(owner);

        return toResponse(endpointRepository.save(endpoint));
    }

    @Transactional(readOnly = true)
    public List<EndpointResponse> listEndpoints(String username) {
        return endpointRepository.findByOwnerUsername(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteEndpoint(String username, String slug) {
        EndpointEntity endpoint = endpointRepository.findBySlug(slug)
                .orElseThrow(() -> new EndpointNotFoundException("Endpoint not found: " + slug));

        if (!endpoint.getOwner().getUsername().equals(username)) {
            // same 404 as "not found" so we don't reveal that someone else's slug exists
            throw new EndpointNotFoundException("Endpoint not found: " + slug);
        }

        endpointRepository.delete(endpoint);
    }

    private String generateSlug() {
        String slug;
        do {
            StringBuilder sb = new StringBuilder(SLUG_LENGTH);
            for (int i = 0; i < SLUG_LENGTH; i++) {
                sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
            }
            slug = sb.toString();
        } while (endpointRepository.existsBySlug(slug));
        return slug;
    }

    private EndpointResponse toResponse(EndpointEntity e) {
        return new EndpointResponse(
                e.getSlug(),
                e.getLabel(),
                e.getCreatedAt(),
                baseUrl + "/hook/" + e.getSlug()
        );
    }
}