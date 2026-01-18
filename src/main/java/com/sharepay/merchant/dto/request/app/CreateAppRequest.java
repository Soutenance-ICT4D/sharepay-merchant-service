package com.sharepay.merchant.dto.request.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAppRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 2048)
    private String webhookUrl;
}
