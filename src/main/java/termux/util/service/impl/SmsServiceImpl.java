package termux.util.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import termux.util.bean.Contact;
import termux.util.dto.RequestDto;
import termux.util.dto.ResponseDto;
import termux.util.service.SmsService;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

  private static final String TERMUX_SMS_CMD_TEMPLATE =
      "termux-sms-send -n %s -s %d '%s'";

  @Override
  public String sendSms(String phoneNumber, String message, int slotId) {
    try {
      String[] cmd = {
          "sh", "-c",
          String.format(TERMUX_SMS_CMD_TEMPLATE, phoneNumber, slotId, message)
      };

      Process process = Runtime.getRuntime().exec(cmd);
      try (BufferedInputStream inputStream = new BufferedInputStream(process.getErrorStream())) {
        int exitCode = process.waitFor();
        log.info("SMS command executed with exit code: {}", exitCode);
        String s = new String(inputStream.readAllBytes());
        if(!s.isEmpty()) {
          log.info("SMS Error Stream: {}", s);
          return "ERROR: " + s.trim();
        }
      }
    } catch (IOException e) {
      log.error("Failed to send SMS", e);
      return "ERROR: " + e.getMessage();
    } catch (InterruptedException e) {
      log.error("SMS sending interrupted", e);
      return "ERROR: SMS sending interrupted";
    }

    return "SUCCESS";
  }

  @Override
  public List<ResponseDto> sendSms(RequestDto req) {
    List<String> smsParts = partitionSmsMessages(req.getMessage());
    return smsParts.stream()
        .map(part -> {
          ResponseDto response = new ResponseDto();
          String responseMessage = sendSms(
              req.getPhoneNumber(), part, req.getSlotId()
          );
          if (responseMessage.startsWith("ERROR")) {
            response.setStatus("FAILURE");
            response.setMessage(responseMessage);
          } else {
            response.setStatus("SUCCESS");
            response.setMessage("SMS sent successfully");
          }
          return response;
        })
        .toList();
  }

  @Override
  public List<ResponseDto> sendToContact(Contact contact, String message, int slotId) {
    if (null == contact.getPhoneNumbers()
        || contact.getPhoneNumbers().isEmpty()) {
      ResponseDto responseDto = new ResponseDto();
      responseDto.setStatus("FAILED");
      responseDto.setMessage("No Numbers found!");
      return List.of(responseDto);
    }
    if (null == contact.getPrimaryNumber()
        && contact.getPhoneNumbers().size() > 1) {
      ResponseDto responseDto = new ResponseDto();
      responseDto.setStatus("FAILED");
      responseDto.setMessage("No Primary Contact found " +
          "set it via v2/contacts/set-primary?name=" + contact.getName() +
          "&index={zeroBasedIndexSelectionFromList} - " +
          String.join(",", contact.getPhoneNumbers()));
      return List.of(responseDto);
    }
    RequestDto requestDto = new RequestDto();
    requestDto.setMessage(message);
    requestDto.setPhoneNumber(
        contact.getPrimaryNumber() == null
            ? contact.getPhoneNumbers().getFirst()
            : contact.getPrimaryNumber()
    );
    requestDto.setSlotId(slotId);
    return sendSms(requestDto);
  }

  private List<String> partitionSmsMessages(String message) {
    int maxLength = 160;
    if (message.length() <= maxLength) {
      return List.of(message);
    }

    List<String> parts = new ArrayList<>();
    int start = 0;

    while (start < message.length()) {
      int end = Math.min(start + maxLength, message.length());
      parts.add(message.substring(start, end));
      start = end;
    }
    return parts;
  }
}
