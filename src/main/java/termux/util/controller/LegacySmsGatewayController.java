package termux.util.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import termux.util.dto.RequestDto;
import termux.util.dto.ResponseDto;
import termux.util.service.SmsService;

@RestController
@RequestMapping("/v1/sms")
@RequiredArgsConstructor
public class LegacySmsGatewayController {

    private final SmsService smsService;
    private static final String DEPRECATED_MESSAGE = "This endpoint is deprecated. Please use /v2/sms/send-sms instead.";

    @PostMapping("/send-sms")
    public ResponseEntity<ResponseDto> sendSms(@RequestBody RequestDto req) {
        ResponseDto response = new ResponseDto();
        String responseMessage = smsService.sendSms(
                req.getPhoneNumber(), req.getMessage(), req.getSlotId()
        );
        if (responseMessage.startsWith("ERROR")) {
            response.setStatus("FAILURE");
            response.setMessage(responseMessage + " " + DEPRECATED_MESSAGE);
            return ResponseEntity.badRequest().body(response);
        } else {
            response.setStatus("SUCCESS");
            response.setMessage("SMS sent successfully " + DEPRECATED_MESSAGE);
            return ResponseEntity.ok(response);
        }
    }
}
