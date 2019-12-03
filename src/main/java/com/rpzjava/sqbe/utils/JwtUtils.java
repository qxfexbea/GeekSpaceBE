package com.rpzjava.sqbe.utils;

import com.rpzjava.sqbe.entities.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtils {

    private static final String SUBJECT = "sqbe";//签名发行者

    private static final String APPSECRET = "sqbe2019";//签名

    /**
     * 生成token
     */
    public static String genJsonWebToken(UserEntity userEntity) {
        String token;
        if (userEntity != null) {
            token = Jwts.builder()
                    .setSubject(SUBJECT)//发行者
                    .claim("sicnuid", userEntity.getSicnuid())
                    .setIssuedAt(new Date())//发行日期
                    .signWith(SignatureAlgorithm.HS256, APPSECRET).compact();//签名
        } else {
            token = "";
        }
        return token;
    }

}