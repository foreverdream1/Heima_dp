package com.hmdp.aspect;

import com.hmdp.annotation.AutoFill;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Aspect
@Component
public class AutoFillAspect {

    /**
     * 拦截 Service 层所有方法，
     * 在方法执行前，为参数中被 @AutoFill 标记的 LocalDateTime 字段自动填值。
     */
    @Before("execution(* com.hmdp.service..*.*(..))")
    public void autoFillTime(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg == null) continue;
            fillFields(arg);
        }
    }

    private void fillFields(Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(AutoFill.class)) {
                if (field.getType() != LocalDateTime.class) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    // createTime 只在为 null 时填入（避免二次更新覆盖）
                    LocalDateTime current = (LocalDateTime) field.get(obj);
                    if (field.getName().equals("createTime")) {
                        if (current == null) {
                            field.set(obj, LocalDateTime.now());
                        }
                    } else {
                        // updateTime / 其他字段：每次都刷新
                        field.set(obj, LocalDateTime.now());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
