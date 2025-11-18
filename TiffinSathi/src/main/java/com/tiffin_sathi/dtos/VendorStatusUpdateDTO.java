package com.tiffin_sathi.dtos;

import com.tiffin_sathi.model.VendorStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VendorStatusUpdateDTO {
    @NotNull
    private VendorStatus status;
    private String reason; // Optional reason for rejection
	public VendorStatus getStatus() {
		return status;
	}
	public void setStatus(VendorStatus status) {
		this.status = status;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
