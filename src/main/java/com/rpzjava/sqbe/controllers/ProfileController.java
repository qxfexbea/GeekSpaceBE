package com.rpzjava.sqbe.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rpzjava.sqbe.daos.IProfileDao;
import com.rpzjava.sqbe.daos.IUserDAO;
import com.rpzjava.sqbe.entities.UserEntity;
import com.rpzjava.sqbe.entities.UserProfile;
import com.rpzjava.sqbe.services.UpdateProfileService;
import com.rpzjava.sqbe.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/profile")
@Slf4j
public class ProfileController {

    private final IProfileDao iProfileDao;
    private final IUserDAO iUserDAO;
    private final UpdateProfileService updateProfileService;

    public ProfileController(
            IProfileDao iProfileDao,
            IUserDAO iUserDAO,
            UpdateProfileService updateProfileService
    ) {
        this.iProfileDao = iProfileDao;
        this.iUserDAO = iUserDAO;
        this.updateProfileService = updateProfileService;
    }

    @PutMapping("/{id}")//前提条件是前端要提交完整的资料信息（也就是不能单独修改一个，或几个，必须全部）
    public Object updateProfile(
            @RequestBody JSONObject reqBody,
            @PathVariable String id) {

        long uid = Long.parseLong(id);
        Optional<UserEntity> userEntity = iUserDAO.findById(uid);
        if (userEntity.isPresent()) {
            iUserDAO.save(updateProfileService.viaRequest(userEntity.get(), reqBody));
            return ResultUtils.success("修改 uid: " + uid + " 用户资料成功！");
        }

        return ResultUtils.error("修改 uid: " + uid + " 用户资料失败！");
    }


    @GetMapping("/{id}")
    public Object findProfileById(@PathVariable String id) {
        Long uid = Long.parseLong(id);
        Optional<UserProfile> foundProfile = iProfileDao.findById(uid);
        return foundProfile.map(userProfile ->
            ResultUtils.success(
                JSON.toJSON(userProfile),
            "获取用户资料成功")).orElseGet(() -> ResultUtils.error("没有找到 uid:" + uid + " 用户的资料"));
    }

}
