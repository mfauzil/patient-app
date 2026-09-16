package io.github.mfauzil.patientapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public class Address {

    @NotBlank
    @Size(max = 200)
    @Column(name = "address_line", nullable = false, length = 200)
    private String addressLine;

    @NotBlank
    @Size(max = 100)
    @Column(name = "suburb", nullable = false, length = 100)
    private String suburb;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 3)
    private AustralianState state;

    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "postcode must be 4 digits")
    @Column(name = "postcode", nullable = false, length = 4)
    private String postcode;

    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }

    public String getSuburb() { return suburb; }
    public void setSuburb(String suburb) { this.suburb = suburb; }

    public AustralianState getState() { return state; }
    public void setState(AustralianState state) { this.state = state; }

    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }
}