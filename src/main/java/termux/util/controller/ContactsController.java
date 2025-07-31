package termux.util.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import termux.util.bean.Contact;
import termux.util.service.ContactsService;

import java.util.Set;

@RestController
@RequestMapping("/v2/contacts")
@RequiredArgsConstructor
public class ContactsController {
    private final ContactsService contactsService;

    @GetMapping("/get-contacts")
    public ResponseEntity<Set<Contact>> getContacts() {
        return ResponseEntity.ok(contactsService.getAllContacts());
    }
}
