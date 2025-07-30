package termux.util.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import termux.util.dto.ResponseDto;
import termux.util.service.SmsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/sms")
public class SmsGatewayController {

    private final SmsService smsService;

    @PostMapping("/send-sms")
    public ResponseEntity<ResponseDto> sendSms(String phoneNumber, String message, int slotId) {
        ResponseDto response = new ResponseDto();
        String responseMessage = smsService.sendSms(phoneNumber, message, slotId);
        if (responseMessage.startsWith("ERROR")) {
            response.setStatus("FAILURE");
            response.setMessage(responseMessage);
            return ResponseEntity.badRequest().body(response);
        } else {
            response.setStatus("SUCCESS");
            response.setMessage("SMS sent successfully");
            return ResponseEntity.ok(response);
        }
    }
}
