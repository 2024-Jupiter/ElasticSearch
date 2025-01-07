package com.myfeed.sms;

import org.apache.catalina.authenticator.DigestAuthenticator.AuthDigest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

//@RestController
@RequestMapping("/api/send-sms")
@Controller
public class SmsController {

    @Autowired SmsService smsService;

    //@PostMapping("/send-authcode")
    @PostMapping("/send-one")
    @ResponseBody
    public ResponseEntity sendAuthCode(SmsRequestDto smsRequestDto) {
        String authCode = smsService.sendMessage(smsRequestDto);

        SmsResponseDto smsResponseDto = new SmsResponseDto();
        smsResponseDto.setAuthCode(authCode);
        return ResponseEntity.ok().body(smsResponseDto);
    }

}
