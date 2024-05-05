package com.ssafy.whoru.domain.message.api;

/*
* Author 김회창
* */

import com.ssafy.whoru.domain.member.dto.CustomOAuth2User;
import com.ssafy.whoru.domain.message.dto.request.TextSend;
import com.ssafy.whoru.domain.message.dto.response.MessageResponse;
import com.ssafy.whoru.domain.message.dto.response.SliceMessageResponse;
import com.ssafy.whoru.global.common.dto.WrapResponse;
import com.ssafy.whoru.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "메세지 API", description = "메세지 전송 및 조회에 관한 Controller")
public interface MessageApiDocs {

    @Operation(summary = "Text 메세지 전송", description = "Text 메세지를 작성자 고유번호와 함께 보내어 content를 랜덤한 사용자에게 전송합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Text 메세지 전송 완료", content = @Content(schema = @Schema(implementation = WrapResponse.class))),
        @ApiResponse(responseCode = "400", description = "content 길이가 2 미만일때 발생", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "content 길이가 200 초과일때 발생", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "정지된 유저 메세지 전송 권한거부", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "받는사람의 FCM 토큰이 비었거나 null 상태일때", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WrapResponse<Void>> sendTextMessage(CustomOAuth2User member, @RequestBody @Valid TextSend textSend);

    @Operation(summary = "Text 답장 메세지 전송", description = "Text 답장메세지를 작성자 고유번호, 내용, 어떤 메세지에 대한 답장인지를 담아서 전송합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Text 답장 메세지 전송완료",  content = @Content(schema = @Schema(implementation = WrapResponse.class))),
        @ApiResponse(responseCode = "400", description = "content 길이가 2 미만일때 발생", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "content 길이가 200 초과일때 발생", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "messageId의 크기가 1미만일때 발생", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "정지된 유저 메세지 전송 권한거부", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "451", description = "신고된 메세지 답장 전송 권한거부", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "받는사람의 FCM 토큰이 비었거나 null 상태일때", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WrapResponse<Void>> sendTextResponseMessage(
        CustomOAuth2User member,
        @RequestBody @Valid TextSend textSend,
        @Min(value = 1, message = "답장을 보낼 대상 메세지의 고유번호는 0보다 커야 합니다.") Long messageId
    );

    @Operation(summary = "이미지, 보이스 메세지 전송", description = "MultipartForm 을 통해서 이미지 / 보이스 파일과 파일에 대한 타입, 보내는 사람의 고유번호를 담아서 전송")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "이미지 / 보이스 메세지 전송 성공",  content = @Content(schema = @Schema(implementation = WrapResponse.class))),
        @ApiResponse(responseCode = "415", description = "허용되지 않는 확장자를 넣은 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "정지된 유저 메세지 전송 권한거부", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "업로드된 파일이 손상된 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "받는사람의 FCM 토큰이 비었거나 null 상태일때", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "AW3 Upload 에 실패했을 때", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WrapResponse<Void>> sendFileMessage(
        CustomOAuth2User member,
        @RequestPart @Schema(description = "업로드 할 파일", format = "binary", allowableValues = {"image/jpeg", "image/gif", "image/png", "audio/mpeg", "audio/wav"}) MultipartFile file
    );

    @Operation(summary = "이미지, 보이스 답장 메세지 전송", description = "MultipartForm 을 통해서 이미지 / 보이스 파일과 파일에 대한 타입, 보내는 사람의 고유번호, 어떤 메세지의 답장인지를 담아서 전송")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "이미지 / 보이스 답장 메세지 전송 성공",  content = @Content(schema = @Schema(implementation = WrapResponse.class))),
        @ApiResponse(responseCode = "400", description = "messageId의 크기가 1미만일때 발생", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "415", description = "허용되지 않는 확장자를 넣은 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "정지된 유저 메세지 전송 권한거부", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "451", description = "신고된 메세지 답장 전송 권한거부", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "업로드된 파일이 손상된 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "받는사람의 FCM 토큰이 비었거나 null 상태일때", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "AW3 Upload 에 실패했을 때", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WrapResponse<Void>> sendFileResponseMessage(
        CustomOAuth2User member,
        @RequestPart @Schema(description = "업로드 할 파일", format = "binary", allowableValues = {"image/jpeg", "image/gif", "image/png", "audio/mpeg", "audio/wav"}) MultipartFile file,
        @Valid @Min(value = 1, message = "답장을 보낼 대상 메세지의 고유번호는 0보다 커야 합니다.") Long messageId
    );

    @Operation(summary = "메세지 목록 최신건수 조회", description = "firstid 를 받아서 그 id에 해당하는 날짜보다 더 최신의 메세지 목록을 조회함")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메세지 목록 조회 성공", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "204", description = "메세지 목록 조회는 성공했으나 비었을 경우", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 파라미터 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WrapResponse<List<MessageResponse>>> findRecentMessages(
        CustomOAuth2User member,
        @NotNull(message = "최소 번호를 적어주세요") @Min(value = 1, message = "0보다는 커야 합니다.") @RequestParam(name = "firstid")  Long firstId
    );

    @Operation(summary = "메세지 목록 이전 건수 조회", description = "lastid 를 받아서 그 id에 해당하는 메세지 날짜를 기준으로 가장 가까운 메세지 목록을 조회함")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메세지 목록 조회 성공", content = @Content(schema = @Schema(implementation = SliceMessageResponse.class))),
        @ApiResponse(responseCode = "204", description = "메세지 목록 조회는 성공했으나 비었을 경우", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 파라미터 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))}
    )
    ResponseEntity<WrapResponse<SliceMessageResponse>> findOldMessages(
        CustomOAuth2User member,
        @Valid @Min(value = 1, message = "id 최대값은 0보다 커야합니다.") Long lastId,
        @Valid @Min(value = 20, message = "size는 최소 20 이상이어야 합니다.") @RequestParam Integer size
    );
}