package com.cpt202.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 泛型 Entity→VO 转换工具。
 * <p>
 * 消除各 ServiceImpl 中重复的 {@code toXxxVOList()} 样板代码。
 * <p>
 * 使用示例：
 * <pre>{@code
 * List<ProjectVO> vos = VoConverter.toList(projects, this::toProjectVO);
 * }</pre>
 */
public final class VoConverter {

    private VoConverter() { /* utility class */ }

    /**
     * 将实体列表逐项转换为 VO 列表。
     *
     * @param entities 实体列表（可为 null）
     * @param mapper   单项转换函数
     * @param <E>      实体类型
     * @param <V>      VO 类型
     * @return VO 列表（不会为 null）
     */
    public static <E, V> List<V> toList(List<E> entities, Function<E, V> mapper) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        List<V> vos = new ArrayList<>(entities.size());
        for (E entity : entities) {
            vos.add(mapper.apply(entity));
        }
        return vos;
    }
}
