package org.qrflash.DTO.Admin.establishment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EstablishmentConfigDTO {
    private String name;
    private String address;
    private String description;


    @JsonProperty("contact_info")
    private List<Object>[] contactInfo;
}
