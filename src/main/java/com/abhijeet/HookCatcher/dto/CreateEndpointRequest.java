package com.abhijeet.HookCatcher.dto;


import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEndpointRequest {
    @Size(max=50, message="It should be max of 50 characters")
    private String label;
}
