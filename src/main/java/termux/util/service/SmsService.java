package termux.util.service;

import termux.util.bean.Contact;
import termux.util.dto.RequestDto;
import termux.util.dto.ResponseDto;

import java.util.List;

public interface SmsService {
    String sendSms(String phoneNumber, String message, int slotId);
    List<ResponseDto> sendSms(RequestDto requestDto);
    List<ResponseDto> sendToContact(Contact contact, String message, int slotId);
}
