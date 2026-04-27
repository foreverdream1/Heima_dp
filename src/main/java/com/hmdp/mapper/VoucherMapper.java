package com.hmdp.mapper;

import com.hmdp.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VoucherMapper {

    @Select("SELECT * FROM tb_voucher WHERE id = #{id}")
    Voucher findById(Long id);

    @Select("SELECT * FROM tb_voucher WHERE shop_id = #{shopId}")
    List<Voucher> queryVoucherOfShop(Long shopId);

    @Select("SELECT * FROM tb_voucher")
    List<Voucher> findAll();
}
