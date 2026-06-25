package top.zhaizz.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import top.zhaizz.common.annotation.AutoFill;
import top.zhaizz.common.annotation.AutoFill.OperationType;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自动填充 AOP 切面
 * <p>
 * 拦截带有 {@link AutoFill} 注解的 Mapper 方法，
 * 根据操作类型自动填充实体中的 createTime / updateTime 字段。
 * <p>
 * 切入点：{@code top.zhaizz.mapper} 包下所有带 {@link AutoFill} 注解的方法
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 前置通知：在目标方法执行前，根据注解类型自动填充时间字段
     */
    @Before("execution(* top.zhaizz.mapper.*.*(..)) && @annotation(autoFill)")
    public void autoFill(JoinPoint joinPoint, AutoFill autoFill) {
        log.debug("AutoFill 拦截方法: {}", joinPoint.getSignature().getName());

        // 获取方法参数（实体对象）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        // 取第一个参数作为实体对象
        Object entity = args[0];
        if (entity == null) {
            return;
        }

        OperationType operationType = autoFill.value();
        LocalDateTime now = LocalDateTime.now();

        if (operationType == OperationType.INSERT) {
            // 插入操作：填充 createTime 和 updateTime
            setFieldIfNull(entity, "createTime", now);
            setFieldIfNull(entity, "updateTime", now);
            log.debug("AutoFill INSERT: 已填充 createTime 和 updateTime");
        } else if (operationType == OperationType.UPDATE) {
            // 更新操作：填充 updateTime
            setField(entity, "updateTime", now);
            log.debug("AutoFill UPDATE: 已填充 updateTime");
        }
    }

    /**
     * 通过反射设置字段值（仅当字段为 null 时填充）
     */
    private void setFieldIfNull(Object entity, String fieldName, Object value) {
        try {
            // 使用 getter 方法获取当前值（Lombok @Data 生成的）
            Method getter = entity.getClass().getMethod("get" + capitalize(fieldName));
            Object currentValue = getter.invoke(entity);
            if (currentValue == null) {
                setField(entity, fieldName, value);
            }
        } catch (Exception e) {
            log.warn("AutoFill 读取字段 [{}] 失败: {}", fieldName, e.getMessage());
        }
    }

    /**
     * 通过反射设置字段值（强制覆盖）
     */
    private void setField(Object entity, String fieldName, Object value) {
        try {
            Method setter = entity.getClass().getMethod("set" + capitalize(fieldName), value.getClass());
            setter.invoke(entity, value);
        } catch (Exception e) {
            log.warn("AutoFill 设置字段 [{}] 失败: {}", fieldName, e.getMessage());
        }
    }

    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
