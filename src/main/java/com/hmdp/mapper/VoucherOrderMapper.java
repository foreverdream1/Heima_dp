package com.hmdp.mapper;

import com.hmdp.entity.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VoucherOrderMapper {

    @Select("SELECT * FROM tb_voucher_order WHERE id = #{id}")
    VoucherOrder findById(Long id);

    @Select("SELECT * FROM tb_voucher_order WHERE user_id = #{userId}")
    List<VoucherOrder> findByUserId(Long userId);


    void save(VoucherOrder voucherOrder);

    int findByUserIdAndVoucherId(@Param("userId") Long userId, @Param("voucherId") Long voucherId);
}
