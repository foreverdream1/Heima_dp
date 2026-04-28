package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 店铺类型管理控制器
 * 提供店铺类型查询接口
 */
@RestController
@RequestMapping("/shop-type")
@Tag(name = "店铺类型管理", description = "店铺类型相关接口，查询所有店铺类型")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    /**
     * 查询所有店铺类型
     *
     * @return 店铺类型列表
     */
    @Operation(summary = "查询店铺类型列表", description = "获取所有店铺类型（如美食、娱乐等）")
    @GetMapping("list")
    public Result queryTypeList() {
        List<ShopType> typeList = typeService.findAll();
        return Result.ok(typeList);
    }
}
