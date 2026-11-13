package com.abhijeet.HookCatcher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EndpointResponse {
    private String slug;
    private String label;
    private LocalDateTime createdAt;
    private String url;
}
