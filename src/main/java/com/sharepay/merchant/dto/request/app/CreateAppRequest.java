package com.sharepay.merchant.dto.request.app;

import com.sharepay.merchant.entity.enums.AppEnvironment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAppRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private AppEnvironment environment;

    @NotBlank
    @Size(max = 255)
    private String description;

    @Size(max = 2048)
    private String webhookUrl;
}
