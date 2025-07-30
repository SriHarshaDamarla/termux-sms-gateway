package termux.util.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import termux.util.service.SmsService;

import java.io.IOException;

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
            int exitCode = process.waitFor();
        } catch (IOException e) {
            log.error("Failed to send SMS", e);
            return "ERROR: " + e.getMessage();
        } catch (InterruptedException e) {
            log.error("SMS sending interrupted", e);
            return "ERROR: SMS sending interrupted";
        }

        return "SUCCESS";
    }
}
