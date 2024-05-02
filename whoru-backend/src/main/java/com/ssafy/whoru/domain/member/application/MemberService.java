package com.ssafy.whoru.domain.member.application;

import com.ssafy.whoru.domain.member.domain.Member;
import com.ssafy.whoru.domain.member.dto.response.ChangeIconResponse;

public interface MemberService {

    public ChangeIconResponse changeIcon(Long memberId, int iconId);

    void logout(Member member);
}