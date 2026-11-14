package com.abhijeet.HookCatcher.service;

import com.abhijeet.HookCatcher.dto.ReplayResponse;
import com.abhijeet.HookCatcher.entity.CapturedRequest;
import com.abhijeet.HookCatcher.entity.EndpointEntity;
import com.abhijeet.HookCatcher.exception.EndpointNotFoundException;
import com.abhijeet.HookCatcher.exception.InvalidTargetException;
import com.abhijeet.HookCatcher.repository.CapturedRequestRepository;
import com.abhijeet.HookCatcher.repository.EndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReplayService {

    private static final int MAX_RESPONSE_CHARS = 2000;

    // headers the JDK HTTP client refuses to set, or that must not be forwarded
    private static final Set<String> SKIPPED_HEADERS = Set.of(
            "host", "content-length", "connection", "transfer-encoding",
            "expect", "upgrade", "keep-alive", "te", "trailer", "proxy-authorization");

    private final EndpointRepository endpointRepository;
    private final CapturedRequestRepository capturedRequestRepository;

    // initialised inline, so Lombok leaves it out of the constructor
    private final RestClient restClient = buildClient();

    @Transactional(readOnly = true)
    public ReplayResponse replay(String username, String slug, String requestId, String targetUrl) {
        EndpointEntity endpoint = endpointRepository.findBySlug(slug)
                .orElseThrow(() -> new EndpointNotFoundException("Endpoint not found: " + slug));

        if (!endpoint.getOwner().getUsername().equals(username)) {
            throw new EndpointNotFoundException("Endpoint not found: " + slug);
        }

        CapturedRequest captured = capturedRequestRepository.findById(requestId)
                .filter(r -> slug.equals(r.getEndpointSlug()))
                .orElseThrow(() -> new EndpointNotFoundException("Request not found: " + requestId));

        URI target = validateTarget(targetUrl);
        HttpMethod method = HttpMethod.valueOf(captured.getMethod());

        RestClient.RequestBodySpec spec = restClient
                .method(method)
                .uri(target)
                .headers(h -> {
                    if (captured.getHeaders() != null) {
                        captured.getHeaders().forEach((name, value) -> {
                            if (!SKIPPED_HEADERS.contains(name.toLowerCase())) {
                                h.set(name, value);
                            }
                        });
                    }
                });

        boolean canHaveBody = !HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method);
        if (canHaveBody && captured.getBody() != null && !captured.getBody().isEmpty()) {
            spec.body(captured.getBody());
        }

        return spec.exchange((req, res) -> {
            String body = new String(res.getBody().readNBytes(MAX_RESPONSE_CHARS), StandardCharsets.UTF_8);
            return new ReplayResponse(res.getStatusCode().value(), body);
        });
    }

    // SSRF protection: only public http/https targets are allowed
    private URI validateTarget(String url) {
        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidTargetException("Invalid target URL");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new InvalidTargetException("Only http and https URLs are allowed");
        }
        if (uri.getHost() == null) {
            throw new InvalidTargetException("Target URL must contain a host");
        }

        try {
            for (InetAddress addr : InetAddress.getAllByName(uri.getHost())) {
                if (isPrivate(addr)) {
                    throw new InvalidTargetException("Target points to a private or local address");
                }
            }
        } catch (UnknownHostException e) {
            throw new InvalidTargetException("Target host could not be resolved");
        }
        return uri;
    }

    private boolean isPrivate(InetAddress a) {
        if (a.isAnyLocalAddress() || a.isLoopbackAddress() || a.isLinkLocalAddress()
                || a.isSiteLocalAddress() || a.isMulticastAddress()) {
            return true;
        }
        // IPv6 unique local addresses (fc00::/7)
        return a instanceof Inet6Address && (a.getAddress()[0] & 0xfe) == 0xfc;
    }

    private static RestClient buildClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)   // a redirect could bounce to an internal address
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(factory).build();
    }
}