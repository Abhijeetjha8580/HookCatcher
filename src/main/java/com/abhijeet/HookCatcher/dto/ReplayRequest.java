package com.abhijeet.HookCatcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplayRequest {

    @NotBlank(message = "targetUrl is required")
    @Size(max = 2048, message = "targetUrl is too long")
    private String targetUrl;
}