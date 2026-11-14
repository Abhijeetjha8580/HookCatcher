package com.abhijeet.HookCatcher.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "captured_requests")
@Getter
@Setter
public class CapturedRequest {

    @Id
    private String id;

    @Indexed
    private String endpointSlug;

    private String method;

    private Map<String, String> headers;

    private Map<String, String> queryParams;

    private String body;

    private String contentType;

    private String ip;

    private LocalDateTime receivedAt;
}