package com.ssafy.whoru.global.config.security;

import com.ssafy.whoru.domain.member.dao.FcmRepository;
import com.ssafy.whoru.domain.member.dao.MemberRepository;
import com.ssafy.whoru.domain.member.dto.CustomOAuth2User;
import com.ssafy.whoru.domain.member.dto.ProviderType;
import com.ssafy.whoru.domain.member.dto.response.GoogleResponse;
import com.ssafy.whoru.domain.member.dto.response.KakaoResponse;
import com.ssafy.whoru.domain.member.domain.Member;
import com.ssafy.whoru.domain.member.dto.MemberDTO;
import com.ssafy.whoru.domain.member.dto.response.OAuth2Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService implements CustomOAuth2UserService{

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("oAuth2User : {}", oAuth2User.toString());

        // 어느 플랫폼의 소셜로그인인지 알아야함
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;

        if(registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
//        else if (registrationId.equals("naver")) {
//            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
//        }
        else {
            return null;
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String memberIdentifier = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        ProviderType providerType = ProviderType.valueOf(oAuth2Response.getProvider());
        String name = oAuth2Response.getName();

        //DB에 해당 유저 존재하는지 확인
        Optional<Member> existData = memberRepository.findByMemberIdentifier(memberIdentifier);

        //없으면 회원가입 시켜주고 저장
        if (existData.isEmpty()){
            Member member = memberRepository.save(Member
                    .builder()
                    .userName(name)
                    .role("ROLE_USER")
                    .isEnabled(true)
                    .provider(providerType)
                    .memberIdentifier(memberIdentifier)
                    .build());


            MemberDTO memberDTO = MemberDTO
                    .builder()
                    .memberIdentifier(memberIdentifier)
                    .role(member.getRole())
                    .isEnabled(member.getIsEnabled())
                    .userName(name)
                    .id(member.getId())
                    .build();
            return new CustomOAuth2User(memberDTO);
        }else{   //있으면 패스
            Member targetMember = existData.get();
            MemberDTO memberDTO = MemberDTO
                    .builder()
                    .role(targetMember.getRole())
                    .memberIdentifier(memberIdentifier)
                    .isEnabled(targetMember.getIsEnabled())
                    .userName(name)
                    .id(targetMember.getId())
                    .build();

            return new CustomOAuth2User(memberDTO);
        }
    }
}
