package termux.util.service;

public interface SmsService {
    String sendSms(String phoneNumber, String message, int slotId);
}
