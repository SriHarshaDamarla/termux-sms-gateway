package termux.util.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import termux.util.dto.RequestDto;
import termux.util.dto.ResponseDto;
import termux.util.service.ContactsService;
import termux.util.service.SmsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/sms")
public class SmsGatewayController {
    private final SmsService smsService;
    private final ContactsService contactsService;

    @PostMapping("/send-sms")
    public ResponseEntity<List<ResponseDto>> sendSms(@RequestBody RequestDto req) {
        return ResponseEntity.ok(smsService.sendSms(req));
    }

    @PostMapping("/send-to-contact")
    public ResponseEntity<List<ResponseDto>> sendToContact(@RequestBody RequestDto req) {
        return ResponseEntity.ok(
            smsService.sendToContact(
                contactsService.getContactByName(req.getContactName()),
                req.getMessage(),
                req.getSlotId()
            )
        );
    }
}
