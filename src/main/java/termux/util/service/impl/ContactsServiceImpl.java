package termux.util.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import termux.util.bean.Contact;
import termux.util.bean.TermuxContact;
import termux.util.service.ContactsService;
import termux.util.service.ContactsSupplier;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContactsServiceImpl implements ContactsService {

    private static final String TERMUX_CONTACTS_CMD = "termux-contact-list";

    private final String contactsFilePath;

    public ContactsServiceImpl(@Value("contacts.path:NO_PATH") String contactsFilePath) {
        this.contactsFilePath = contactsFilePath;
    }

    private Set<Contact> contacts;

    @Override
    public Set<Contact> getAllContacts() {
        if (contacts == null) {
            refreshContacts();
        }
        return contacts;
    }

    @Override
    public void refreshContacts() {
        contacts = ContactsSupplier.getIf(
                contactsFilePath,
                path -> path.equals("NO_PATH"),
                this::loadContactsFromTermux
        ).get();

    }

    @Override
    public Contact getContactByName(String name) {
        return null;
    }

    @Override
    public Contact getContactByPhoneNumber(String phoneNumber) {
        return null;
    }

    @Override
    public List<Contact> searchContacts(String query) {
        return List.of();
    }

    private Set<Contact> loadContactsFromTermux() {
        String[] cmd = {
                "sh", "-c",
                String.format(TERMUX_CONTACTS_CMD)
        };
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            log.error("exception while executing Termux contacts command", e);
            return null;
        }
        try (BufferedInputStream inputStream = new BufferedInputStream(process.getInputStream())) {
            int exitCode = process.waitFor();
            log.info("Termux contacts command executed with exit code: {}", exitCode);
            String output = new String(inputStream.readAllBytes());
            ObjectMapper objectMapper = new ObjectMapper();
            List<TermuxContact> termuxContacts = objectMapper.readValue(output, new TypeReference<>() {
            });

            return termuxContacts.stream().map(tm -> {
                Contact contact = new Contact();
                contact.setName(tm.getName());
                contact.setPhoneNumbers(List.of(tm.getNumber()));
                return contact;
            }).collect(Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparing(Contact::getName))
                )
            );
        } catch (IOException | InterruptedException e) {
            log.error("Failed to load contacts from Termux", e);
            return null;
        }
    }
}
