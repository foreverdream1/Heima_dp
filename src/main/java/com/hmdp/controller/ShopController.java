package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id) {
        Shop shop = shopService.findById(id);
        return Result.ok(shop);
    }

    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        return Result.fail("功能未完成");
    }

    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        return Result.fail("功能未完成");
    }

    @GetMapping("/of/type")
    public Result queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        List<Shop> shops = shopService.findByTypeId((long) typeId);
        return Result.ok(shops != null ? shops : Collections.emptyList());
    }

    @GetMapping("/of/name")
    public Result queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        return Result.ok(Collections.emptyList());
    }
}
