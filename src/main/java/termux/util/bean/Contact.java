package termux.util.bean;

import lombok.Data;

import java.util.List;

@Data
public class Contact {
    private String name;
    private List<String> phoneNumbers;
}
