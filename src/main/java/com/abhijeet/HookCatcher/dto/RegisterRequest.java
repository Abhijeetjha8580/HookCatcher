package com.abhijeet.HookCatcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "UserName Required")
    @Size(min = 3,max = 30,message = "Username must be between 3 to 30")
    private String username;

    @NotBlank(message = "Password Is Required")
    @Size(min=6,message = "Password should be atleast of 6 letter")
    private String password;
}
