package com.cpt202.validation;

/**
 * 目录管理领域约束验证服务。
 * 负责分类、标签的名称唯一性校验等目录相关约束。
 */
public interface CatalogValidationService {

    /**
     * 校验分类名称是否唯一（新增时使用）。
     *
     * @param categoryName 分类名称
     */
    void checkCategoryNameUnique(String categoryName);

    /**
     * 校验分类名称是否唯一（修改时使用，排除自身）。
     *
     * @param categoryName 分类名称
     * @param excludeId    排除的分类主键
     */
    void checkCategoryNameUniqueExcludingId(String categoryName, Long excludeId);

    /**
     * 校验标签名称是否唯一（新增时使用）。
     *
     * @param tagName 标签名称
     */
    void checkTagNameUnique(String tagName);

    /**
     * 校验标签名称是否唯一（修改时使用，排除自身）。
     *
     * @param tagName   标签名称
     * @param excludeId 排除的标签主键
     */
    void checkTagNameUniqueExcludingId(String tagName, Long excludeId);
}
