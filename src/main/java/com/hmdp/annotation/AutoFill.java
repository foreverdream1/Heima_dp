package com.hmdp.annotation;

import java.lang.annotation.*;

/**
 * 自动填充时间字段注解
 * 标注在实体类的 LocalDateTime 字段上，
 * AOP 切面会在 Service 方法执行前自动填入当前时间。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoFill {
}
