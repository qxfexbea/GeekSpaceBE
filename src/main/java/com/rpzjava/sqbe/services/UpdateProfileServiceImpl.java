package com.rpzjava.sqbe.services;

import com.alibaba.fastjson.JSONObject;
import com.rpzjava.sqbe.entities.pojos.UserEntity;
import com.rpzjava.sqbe.entities.pojos.UserProfile;
import org.springframework.stereotype.Service;

@Service
public class UpdateProfileServiceImpl implements UpdateProfileService {

    @Override
    public UserEntity viaRequest(UserEntity userEntity, JSONObject reqBody) {
        UserProfile profile = userEntity.getUserProfile();
        profile.setBio(reqBody.get("bio").toString());
        profile.setNickName(reqBody.get("nick_name").toString());
        profile.setSex(Integer.parseInt(reqBody.get("sex").toString()));
        profile.setAvatarUrl(reqBody.get("avatarUrl").toString());
        userEntity.setUserProfile(profile);
        return userEntity;
    }

}
