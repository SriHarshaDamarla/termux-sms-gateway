package termux.util.dto;

import lombok.Data;

@Data
public class RequestDto {
    private String phoneNumber;
    private String message;
    private int slotId;
}
