package termux.util.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import termux.util.bean.Contact;
import termux.util.service.ContactsService;

import java.util.List;
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

    @GetMapping
    public ResponseEntity<Contact> getContactByName(@RequestParam String name) {
        return ResponseEntity.ok(contactsService.getContactByName(name));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Contact>> queryContactsByName(@RequestParam String name) {
        return ResponseEntity.ok(contactsService.searchContacts(name));
    }

    @PostMapping(value = "/refresh", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> refreshContacts() {
        contactsService.refreshContacts();
        return ResponseEntity.ok("Contacts Refreshed!");
    }

    @GetMapping(value = "/set-primary", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> setPrimaryNumberForContact(
        @RequestParam String name,
        @RequestParam int index
    ) {
        Contact contact = contactsService.getContactByName(name);
        if (contact.getPhoneNumbers() == null) {
            return ResponseEntity.badRequest().body("No Contact Found!");
        }
        contact.setPrimaryNumber(contact.getPhoneNumbers().get(index));
        return ResponseEntity.ok("Successfully set " +
            contact.getPhoneNumbers().get(index) + " as primary for " +
            contact.getName());
    }

}
