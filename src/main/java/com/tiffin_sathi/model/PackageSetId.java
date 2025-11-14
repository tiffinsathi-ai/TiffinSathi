package com.tiffin_sathi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PackageSetId implements Serializable {

    @Column(name = "package_id", length = 50)
    private String packageId;

    @Column(name = "set_id", length = 50)
    private String setId;

    // Default constructor
    public PackageSetId() {}

    public PackageSetId(String packageId, String setId) {
        this.packageId = packageId;
        this.setId = setId;
    }

    // Getters and Setters
    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageSetId that = (PackageSetId) o;
        return Objects.equals(packageId, that.packageId) && Objects.equals(setId, that.setId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageId, setId);
    }
}