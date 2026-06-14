package com.cpt202.service;

import com.cpt202.dto.TagDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.result.PageResult;
import com.cpt202.vo.TagVO;

import java.util.List;

/**
 * 标签管理服务接口。
 * <p>
 * 用于定义标签的查询、创建、修改和删除能力。
 */
public interface TagService {

    /**
     * 查询全部标签列表。
     *
     * @return 标签展示对象列表
     */
    List<TagVO> listAll();

    /**
     * 分页查询标签列表。
     *
     * @param queryDTO 分页查询参数
     * @return 标签分页结果
     */
    PageResult<TagVO> listPage(PageQueryDTO queryDTO);

    /**
     * 根据标签主键查询标签详情。
     *
     * @param tagId 标签主键
     * @return 标签展示对象
     */
    TagVO getById(Long tagId);

    /**
     * 新增标签。
     *
     * @param tagDTO 标签新增参数
     */
    void create(TagDTO tagDTO);

    /**
     * 修改指定标签。
     *
     * @param tagId 标签主键
     * @param tagDTO 标签更新参数
     */
    void update(Long tagId, TagDTO tagDTO);

    /**
     * 删除指定标签。
     *
     * @param tagId 标签主键
     */
    void delete(Long tagId);
}
