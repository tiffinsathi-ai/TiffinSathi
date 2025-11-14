package com.tiffin_sathi.dtos;

import com.tiffin_sathi.model.VendorStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VendorStatusUpdateDTO {
    @NotNull
    private VendorStatus status;
    private String reason; // Optional reason for rejection
}
