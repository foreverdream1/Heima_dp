package com.hmdp.mapper;

import com.hmdp.entity.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillVoucherMapper {

    @Select("SELECT * FROM tb_seckill_voucher WHERE voucher_id = #{voucherId}")
    SeckillVoucher findByVoucherId(Long voucherId);

    @Update("UPDATE tb_seckill_voucher SET stock = stock - 1 WHERE voucher_id = #{voucherId} AND stock > 0")
    int updateStock(Long voucherId);

    void save(SeckillVoucher seckillVoucher);
}
