package com.abhijeet.HookCatcher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReplayResponse {

    private int statusCode;
    private String responseBody;
}