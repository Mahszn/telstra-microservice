package au.com.telstra.simcardactivator;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public record ActivationRequest(
        @NotBlank String iccid,
        @Email String customerEmail
) { }