package termux.util.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDto {
    private String phoneNumber;
    private String message;
    private int slotId;
    private String contactName;
}
