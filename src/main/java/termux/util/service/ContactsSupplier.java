package termux.util.service;

import termux.util.bean.Contact;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ContactsSupplier extends Supplier<Set<Contact>> {

    default ContactsSupplier elseFrom(ContactsSupplier other) {
        return () -> {
            Set<Contact> contacts = this.get();
            return contacts != null ? contacts : other.get();
        };
    }

    static <T> ContactsSupplier getIf(T predicateInput, Predicate<T> condition, ContactsSupplier contactsSupplier) {
        return condition.test(predicateInput) ? contactsSupplier : () -> null;
    }
}
