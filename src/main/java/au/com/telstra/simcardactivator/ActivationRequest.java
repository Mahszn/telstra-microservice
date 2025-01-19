package au.com.telstra.simcardactivator;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public record ActivationRequest(
        @NotNull String iccid,
        @Email String customerEmail
) { }