package com.ssafy.whoru.domain.board.api;


import com.ssafy.whoru.domain.board.application.BoardService;
import com.ssafy.whoru.domain.board.dto.request.PatchInquiryCommentRequest;
import com.ssafy.whoru.domain.board.dto.request.PatchNotificationRequest;
import com.ssafy.whoru.domain.board.dto.request.PostInquiryCommentRequest;
import com.ssafy.whoru.domain.board.dto.request.PostNotificationRequest;
import com.ssafy.whoru.domain.board.dto.response.InquiryDetailResponse;
import com.ssafy.whoru.domain.member.dto.CustomOAuth2User;
import com.ssafy.whoru.global.common.dto.SliceResponse;
import com.ssafy.whoru.global.common.dto.SuccessType;
import com.ssafy.whoru.global.common.dto.WrapResponse;
import com.ssafy.whoru.global.error.exception.BusinessLogicException;
import com.ssafy.whoru.global.error.exception.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/admin/board")
public class BoardAdminApi implements BoardAdminApiDocs{

    private final BoardService boardService;

    /**
     * 시큐리티 콘텍스트에서 ADMIN 권한 체크
     * **/
    private static void checkedAuth() {
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .filter(role -> role.getAuthority().equals("ROLE_ADMIN"))
            .findFirst()
            .orElseThrow(() -> new BusinessLogicException(ErrorCode.FORBIDDEN_ERROR));
    }

    /**
     * 관리자 문의사항 답글 API
     * **/
    @PostMapping("/comment")
    public ResponseEntity<WrapResponse<Void>> postComment(@AuthenticationPrincipal CustomOAuth2User admin, @RequestBody PostInquiryCommentRequest request) {

        log.info("request Member -> {}", admin.getId());

        log.info("request body -> {}", request);

        checkedAuth();

        boardService.postComment(admin.getId(), request);
        return ResponseEntity.ok(WrapResponse.create(SuccessType.STATUS_201));
    }

    /**
     * 관리자 문의사항 조회 API
     * @param page
     * @param size
     * @param condition
     * @return
     */
    @GetMapping("/inquiry")
    public ResponseEntity<WrapResponse<SliceResponse<InquiryDetailResponse>>> getTotalInquiry(@RequestParam("page") @Min(value = 0, message = "page는 최소 0이상이어야 합니다.") int page,
        @RequestParam(value = "size", required = false) @Min(value = 1, message = "size는 최소 1이상이어야 합니다.") @Max(value = 30, message = "size는 최대 30까지만 적용됩니다.") int size,
        @RequestParam("condition") @Min(value = 0, message = "조건값은 0 또는 1이어야 합니다.") @Max(value = 1, message = "조건값은 0 또는 1이어야 합니다.") int condition) {

        log.info("request param -> page: {}, size: {}, condition: {}", page, size, condition);

        checkedAuth();

        SliceResponse<InquiryDetailResponse> response = boardService.getTotalInquiry(page, size, condition);
        return ResponseEntity.ok(WrapResponse.create(response, SuccessType.SIMPLE_STATUS));
    }

    /**
     * 관리자 문의사항 답글 수정 API
     * @param commentId
     * @param request
     * @return
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<WrapResponse<Void>> patchComment(@PathVariable("commentId") Long commentId, @RequestBody PatchInquiryCommentRequest request) {

        log.info("PathVariable -> {}", commentId);

        log.info("request body -> {}", request);

        checkedAuth();

        boardService.patchComment(commentId, request);
        return ResponseEntity.ok(WrapResponse.create(SuccessType.STATUS_204));
    }

    /**
     * 관리자 공지사항 작성 API
     */
    @PostMapping("/noti")
    public ResponseEntity<WrapResponse<Void>> writeNotification(@AuthenticationPrincipal CustomOAuth2User admin, @RequestBody @Valid PostNotificationRequest postNotificationRequest){

        log.info("request Member -> {}", admin.getId());
        log.info("request body -> {}", postNotificationRequest);

        checkedAuth();

        boardService.postNotification(admin.getId(), postNotificationRequest);
        return ResponseEntity.ok(WrapResponse.create(
            SuccessType.STATUS_201
        ));
    }

    /**
     * 관리자 공지사항 수정 API
     */
    @PatchMapping("/noti/{boardId}")
    public ResponseEntity<WrapResponse<Void>> updateNotification(
        @AuthenticationPrincipal CustomOAuth2User admin,
        @RequestBody @Valid PatchNotificationRequest patchNotificationRequest,
        @PathVariable @Min(value = 1, message = "적어도 0보다 커야 합니다.") Long boardId
    ){

        log.info("request Member -> {}", admin.getId());
        log.info("request Pathvariable -> {}", boardId);
        log.info("request body -> {}", patchNotificationRequest);

        checkedAuth();

        boardService.patchNotification(admin.getId(), patchNotificationRequest, boardId);
        return ResponseEntity.ok(WrapResponse.create(
           SuccessType.STATUS_204
        ));
    }

}
