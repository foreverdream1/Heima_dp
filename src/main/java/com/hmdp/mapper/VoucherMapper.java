package com.hmdp.mapper;

import com.hmdp.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VoucherMapper {

    @Select("SELECT * FROM tb_voucher WHERE id = #{id}")
    Voucher findById(Long id);

    List<Voucher> queryVoucherOfShop(Long shopId);

    List<Voucher> findAll();

    void save(Voucher voucher);
}
