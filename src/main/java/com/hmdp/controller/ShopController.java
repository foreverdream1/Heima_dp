package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 店铺管理控制器
 * 提供店铺的增删改查等接口
 */
@RestController
@RequestMapping("/shop")
@Tag(name = "店铺管理", description = "店铺相关接口，包括店铺查询、新增、更新等操作")
public class ShopController {

    @Resource
    public IShopService shopService;

    /**
     * 根据ID查询店铺信息
     *
     * @param id 店铺ID
     * @return 店铺信息
     */
    @Operation(summary = "根据ID查询店铺", description = "根据店铺ID查询店铺详细信息，支持缓存")
    @GetMapping("/{id}")
    public Result queryShopById(
            @Parameter(description = "店铺ID", required = true, example = "1")
            @PathVariable("id") Long id) {
        return Result.ok(shopService.findById(id));
    }

    /**
     * 新增店铺
     *
     * @param shop 店铺信息
     * @return 操作结果
     */
    @Operation(summary = "新增店铺", description = "创建新店铺，需要提供店铺基本信息")
    @PostMapping
    public Result saveShop(
            @Parameter(description = "店铺信息", required = true)
            @Valid @RequestBody Shop shop) {
        return shopService.save(shop);
    }

    /**
     * 更新店铺信息
     *
     * @param shop 店铺信息（需包含ID）
     * @return 操作结果
     */
    @Operation(summary = "更新店铺信息", description = "根据店铺ID更新店铺信息，同时清除缓存")
    @PutMapping
    public Result updateShop(
            @Parameter(description = "店铺信息", required = true)
            @Valid @RequestBody Shop shop) {
        return shopService.updateById(shop);
    }

    /**
     * 根据类型查询店铺列表
     *
     * @param typeId  店铺类型ID
     * @param current 当前页码（预留参数，暂未实现分页）
     * @return 店铺列表
     */
    @Operation(summary = "根据类型查询店铺", description = "根据店铺类型ID查询该类型下的所有店铺")
    @GetMapping("/of/type")
    public Result queryShopByType(
            @Parameter(description = "店铺类型ID", required = true, example = "1")
            @RequestParam("typeId") Integer typeId,
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        List<Shop> shops = shopService.findByTypeId((long) typeId);
        return Result.ok(shops != null ? shops : Collections.emptyList());
    }

    /**
     * 根据名称查询店铺（预留接口，暂未实现）
     *
     * @param name    店铺名称（模糊查询）
     * @param current 当前页码（预留参数）
     * @return 空列表（功能待实现）
     */
    @Operation(summary = "根据名称查询店铺", description = "根据店铺名称模糊查询店铺列表（功能待实现）")
    @GetMapping("/of/name")
    public Result queryShopByName(
            @Parameter(description = "店铺名称", required = false)
            @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 预留接口，暂未实现具体功能
        return Result.ok(Collections.emptyList());
    }
}
