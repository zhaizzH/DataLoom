package top.zhaizz.common.annotation;

import java.lang.annotation.*;

/**
 * 自动填充注解
 * <p>
 * 用于标记 Mapper 方法，在执行时自动填充 createTime 或 updateTime 字段
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoFill {

    /**
     * 操作类型
     */
    OperationType value();

    /**
     * 操作类型枚举
     */
    enum OperationType {
        /**
         * 插入操作，自动填充 createTime 和 updateTime
         */
        INSERT,

        /**
         * 更新操作，自动填充 updateTime
         */
        UPDATE
    }
}
