package com.myfeed.sms;

import com.myfeed.exception.ExpectedException;
import com.myfeed.response.ErrorCode;
import com.myfeed.service.user.UserService;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {
    final DefaultMessageService messageService;
    @Autowired UserService userService;

    public SmsService() {
        // 반드시 계정 내 등록된 유효한 API 키, API Secret Key를 입력해주셔야 합니다!
        //this.messageService = NurigoApp.INSTANCE.initialize("${spring.coolsms.api.key}", "${spring.coolsms.api.secret}", "https://api.coolsms.co.kr");
        this.messageService = NurigoApp.INSTANCE.initialize("NCSTN8AA40XJD2G1", "V6JX2HOLWB6PRBUJVEIA2E0YZZUUARBT", "https://api.coolsms.co.kr");
    }

    public String sendMessage(SmsRequestDto smsRequestDto) {
        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        //message.setFrom("${spring.coolsms.api.fromnumber}");

        message.setFrom("01077052827");

        if (userService.findByPhoneNumber(smsRequestDto.getPhoneNumber()).isEmpty()) {
            throw new ExpectedException(ErrorCode.USER_NOT_FOUND);
        }

        message.setTo(smsRequestDto.getPhoneNumber());

        try {
            Random rand = new Random();
            String numStr = "";

            for (int i = 0; i <6; i++) {
                String randomNumber = Integer.toString(rand.nextInt(10));
                numStr += randomNumber;
            }

            message.setText("[MyFeed] 인증번호["+numStr+"]를 입력하세요.");

            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            System.out.println(response);

            return numStr;
        } catch (Exception e) {
            log.error("Failed to send AuthCode to number " + message.getTo());
            throw new RuntimeException(e);
        }

    }

}
