package com.ssafy.whoru.domain.message.api;

/*
* Author: 김회창
* */

import com.ssafy.whoru.domain.message.dto.MessageResponse;
import com.ssafy.whoru.global.common.domain.SuccessType;
import com.ssafy.whoru.global.common.dto.WrapResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageApi implements MessageApiDocs{

    @GetMapping("/test200")
    public ResponseEntity<WrapResponse<MessageResponse>> test200() {
        WrapResponse<MessageResponse> res = WrapResponse.create(
                MessageResponse.builder()
                        .content("반갑습니다.")
                        .build()
                ,
                SuccessType.STATUS_201
        );
        return ResponseEntity.ok(res);
    }

}
