package com.ssafy.whoru.domain.member.dto;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final MemberDTO memberDTO;

    public CustomOAuth2User(MemberDTO memberDTO) {

        this.memberDTO = memberDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return memberDTO.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return memberDTO.getUserName();
    }

    public String getMemberIdentifier(){ return memberDTO.getMemberIdentifier(); }

    public Long getId(){ return memberDTO.getId(); }
}
