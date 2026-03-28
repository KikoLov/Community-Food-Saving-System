package com.food.controller;

import com.food.common.Result;
import com.food.dto.gamification.*;
import com.food.security.LoginUser;
import com.food.service.GamificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 居民端：碳积分游戏化（商城兑换、森林状态）
 */
@RestController
@RequestMapping("/api/consumer/gamification")
@RequiredArgsConstructor
public class ConsumerGamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/catalog")
    public Result<List<GamificationCatalogItemDTO>> catalog() {
        return Result.success(gamificationService.getCatalog());
    }

    @GetMapping("/state")
    public Result<GamificationStateDTO> state(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(gamificationService.getState(loginUser.getUserId()));
    }

    @PostMapping("/redeem")
    public Result<RedeemResultDTO> redeem(Authentication authentication,
                                          @Valid @RequestBody RedeemRequestDTO body) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(gamificationService.redeem(loginUser.getUserId(), body.getItemCode().trim()));
    }
}
