package com.semillero.ecosistema.request.supplier;

import com.semillero.ecosistema.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SupplierStatusPatchRequest(
        @NotBlank
        @Pattern(regexp = "ACEPTADO|DENEGADO|REQUIERE_CAMBIOS", message ="The status can only be ACCEPTED, DENIED, or REQUIRES_CHANGES.")
        String status,

        @NotBlank(message = "Feedback cannot be null")
        String feedback) {
    public Status toStatus(){
        return Status.valueOf(this.status);
    }
}
