package at.aau.serg.models;

import jakarta.validation.constraints.*;

public class Address {
    @NotNull
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    public Double latitude;

    @Size(min = 1, max = 255)
    @NotNull
    public String address;
}
