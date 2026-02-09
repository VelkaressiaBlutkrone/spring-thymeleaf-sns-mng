package com.example.sns.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.sns.config.MapProperties;

import lombok.RequiredArgsConstructor;

/**
 * 루트 경로(/) 페이지 컨트롤러.
 * Step 12: 지도 메인 페이지, 카카오맵 JS API 키 전달.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MapProperties mapProperties;

    @GetMapping("/")
    public String home(Model model) {
        String key = mapProperties.kakaoJsAppKey() != null ? mapProperties.kakaoJsAppKey() : "";
        model.addAttribute("kakaoJsAppKey", key);
        model.addAttribute("kakaoMapScriptUrl",
                !key.isEmpty() ? "https://dapi.kakao.com/v2/maps/sdk.js?appkey=" + key + "&autoload=false" : "");
        return "index";
    }
}
