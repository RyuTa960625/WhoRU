package com.ssafy.whoru.domain.board.api;

import com.ssafy.whoru.domain.board.dto.request.PostBoardRequest;
import com.ssafy.whoru.domain.board.dto.request.PostCommentRequest;
import com.ssafy.whoru.domain.board.dto.response.InquiryRecordResponse;
import com.ssafy.whoru.global.common.dto.SliceResponse;
import com.ssafy.whoru.global.common.dto.WrapResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface BoardAdminApiDocs {

    @Operation(summary = "문의사항 답글 작성 요청", description = "관리자는 문의사항 답글을 작성할 수 있다.")
    @Parameter(content = @Content(schema = @Schema(implementation = PostCommentRequest.class)))
    @ApiResponse(responseCode = "201")
    @PostMapping("/comment")
    public ResponseEntity<WrapResponse<Void>> postComment(@RequestBody PostCommentRequest request);

    @Operation(summary = "문의사항 조회 요청", description = "관리자는 전체 문의사항을 작성할 수 있다.")
    @Parameters( value = {
        @Parameter(name = "page", required = true, description = "최초 페이지 = 0, 응답으로는 +1 값을 리턴"),
        @Parameter(name = "size", description = "default = 12 || 1이상 30이하의 값만 가능"),
        @Parameter(name = "condition", description = "0으로 보낼 시 전체 조회, 1로 보낼 시 답글이 안달린 문의사항들만 조회")
    })
    @ApiResponse(responseCode = "200", description = "Custom Slice Response", content = @Content(schema = @Schema(implementation = SliceResponse.class)))
    @GetMapping("")
    public ResponseEntity<WrapResponse<SliceResponse<InquiryRecordResponse>>> getTotalInquiry(@RequestParam("page") int page,
        @RequestParam(value = "size", required = false) @Min(value = 1, message = "size는 최소 1이상이어야 합니다.") @Max(value = 30, message = "size는 최대 30까지만 적용됩니다.") int size,
        @RequestParam("condition") @Min(value = 0, message = "조건값은 0 또는 1이어야 합니다.") @Max(value = 1, message = "조건값은 0 또는 1이어야 합니다.") int condition);


}
