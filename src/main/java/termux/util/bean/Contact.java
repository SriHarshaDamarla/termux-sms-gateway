package termux.util.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Contact {
    private String primaryNumber;
    private String name;
    private List<String> phoneNumbers;
}
