package termux.util.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import ezvcard.property.Telephone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import termux.util.bean.Contact;
import termux.util.bean.TermuxContact;
import termux.util.service.ContactsService;
import termux.util.service.ContactsSupplier;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
  private Set<Contact> contacts;

  public ContactsServiceImpl(@Value("${contacts.base.path:NO_PATH}") String contactsFilePath) {
    this.contactsFilePath = contactsFilePath;
  }

  private static Contact getContact(VCard card) {
    Contact contact = new Contact();
    String fullName = card.getFormattedName() != null
        ? card.getFormattedName().getValue() : "(no name)";
    contact.setName(fullName);

    List<Telephone> telNums = card.getTelephoneNumbers();
    List<String> numbers = new ArrayList<>();
    telNums.forEach(telNum ->
        numbers.add(telNum.getText())
    );
    contact.setPhoneNumbers(numbers);
    return contact;
  }

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
        )
        .elseFrom(this::getFromVcfFile)
        .get();

  }

  @Override
  public Contact getContactByName(String name) {
    if (null == contacts) {
      refreshContacts();
    }
    Contact contact = new Contact();
    contact.setName(name);

    return contacts.stream()
        .filter(cnt -> name.equals(cnt.getName()))
        .findFirst().orElse(contact);
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

  private Set<Contact> getFromVcfFile() {
    File file = new File(contactsFilePath + "/contacts.vcf");
    TreeSet<Contact> contacts = new TreeSet<>(Comparator.comparing(Contact::getName));
    try (VCardReader vCardReader = new VCardReader(file.toPath());) {
      VCard card;
      while ((card = vCardReader.readNext()) != null) {
        Contact contact = getContact(card);
        List<Telephone> telephoneList = card.getTelephoneNumbers();
        List<String> numbers = telephoneList.stream()
            .map(Telephone::getText)
            .map(text -> text.replace("-","")
                .replace(" ",""))
            .distinct()
            .toList();
        contact.setPhoneNumbers(numbers);
        contacts.add(contact);
      }
    } catch (IOException e) {
      log.error("Error reading vcf file from {}", contactsFilePath, e);
    }

    return contacts;
  }
}
