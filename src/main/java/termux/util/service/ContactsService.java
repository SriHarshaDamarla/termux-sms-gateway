package termux.util.service;

import termux.util.bean.Contact;

import java.util.List;
import java.util.Set;

public interface ContactsService {
    Set<Contact> getAllContacts();
    void refreshContacts();
    Contact getContactByName(String name);
    Contact getContactByPhoneNumber(String phoneNumber);
    List<Contact> searchContacts(String query);
}
