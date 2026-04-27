package com.hmdp.mapper;

import com.hmdp.entity.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeckillVoucherMapper {

    @Select("SELECT * FROM tb_seckill_voucher WHERE voucher_id = #{voucherId}")
    SeckillVoucher findByVoucherId(Long voucherId);
}
