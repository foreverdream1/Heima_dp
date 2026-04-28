package com.hmdp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一响应结果封装类
 * 用于规范控制器返回的数据格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    /** 请求是否成功 */
    private Boolean success;
    
    /** 错误信息（成功时为null） */
    private String errorMsg;
    
    /** 响应数据（失败时为null） */
    private Object data;
    
    /** 数据总数（用于分页） */
    private Long total;

    /**
     * 成功响应（无数据）
     * @return 成功结果
     */
    public static Result ok(){
        return new Result(true, null, null, null);
    }
    
    /**
     * 成功响应（带数据）
     * @param data 响应数据
     * @return 成功结果
     */
    public static Result ok(Object data){
        return new Result(true, null, data, null);
    }
    
    /**
     * 成功响应（带列表数据和总数，用于分页）
     * @param data 列表数据
     * @param total 数据总数
     * @return 成功结果
     */
    public static Result ok(List<?> data, Long total){
        return new Result(true, null, data, total);
    }
    
    /**
     * 失败响应
     * @param errorMsg 错误信息
     * @return 失败结果
     */
    public static Result fail(String errorMsg){
        return new Result(false, errorMsg, null, null);
    }
}
