package com.cpt202.service;

import com.cpt202.dto.CategoryDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.result.PageResult;
import com.cpt202.vo.CategoryVO;

import java.util.List;

/**
 * 分类管理服务接口。
 * <p>
 * 该接口负责定义管理端分类相关的查询与维护操作：
 * 查询类方法返回前端需要展示的数据，
 * 修改类方法仅负责执行业务动作，不强制返回更新后的对象。
 */
public interface CategoryService {

    /**
     * 查询全部分类列表。
     *
     * @return 分类展示对象列表
     */
    List<CategoryVO> listAll();

    /**
     * 分页查询分类列表。
     *
     * @param queryDTO 分页查询参数
     * @return 分类分页结果
     */
    PageResult<CategoryVO> listPage(PageQueryDTO queryDTO);

    /**
     * 根据分类主键查询分类详情。
     *
     * @param categoryId 分类主键
     * @return 分类展示对象
     */
    CategoryVO getById(Long categoryId);

    /**
     * 新增分类。
     *
     * @param categoryDTO 分类新增参数
     */
    void create(CategoryDTO categoryDTO);

    /**
     * 修改指定分类。
     *
     * @param categoryId 分类主键
     * @param categoryDTO 分类更新参数
     */
    void update(Long categoryId, CategoryDTO categoryDTO);

    /**
     * 删除指定分类。
     *
     * @param categoryId 分类主键
     */
    void delete(Long categoryId);
}
