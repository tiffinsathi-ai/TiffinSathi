package com.tiffin_sathi.dtos;

import com.tiffin_sathi.model.Role;

public class UpdateRoleRequest {
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
