package com.cpt202.service.impl;

import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.CategoryDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.Category;
import com.cpt202.result.PageResult;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.service.RedisCacheService;
import com.cpt202.service.CategoryService;
import com.cpt202.util.VoConverter;
import com.cpt202.validation.CatalogValidationService;
import com.cpt202.vo.CategoryVO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

/**
 * 分类管理服务实现类。
 * <p>
 * 当前处于接口骨架阶段，具体持久化逻辑后续补充。
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Duration CATEGORY_CACHE_TTL = Duration.ofMinutes(30);

    private final CategoryRepository categoryRepository;
    private final RedisCacheService redisCacheService;
    private final CatalogValidationService catalogValidationService;

    /**
     * 查询全部分类列表。
     */
    @Override
    public List<CategoryVO> listAll() {
        return redisCacheService.get(RedisKeyConstants.CATEGORY_LIST, new TypeReference<List<CategoryVO>>() { })
                .orElseGet(() -> {
                    List<CategoryVO> categoryVos = VoConverter.toList(
                            categoryRepository.findAll(), this::toCategoryVO);
                    redisCacheService.set(RedisKeyConstants.CATEGORY_LIST, categoryVos, CATEGORY_CACHE_TTL);
                    return categoryVos;
                });
    }

            @Override
            public PageResult<CategoryVO> listPage(PageQueryDTO queryDTO) {
            Pageable pageable = PageRequest.of(
                Math.max(0, queryDTO.getPageNum() - 1),
                queryDTO.getPageSize(),
                Sort.by(Sort.Direction.ASC, "categoryId"));
            Page<Category> categoryPage = categoryRepository.findAll(pageable);
            List<CategoryVO> categoryVos = categoryPage.getContent().stream()
                .map(this::toCategoryVO)
                .toList();
            return PageResult.fromPage(categoryPage, categoryVos);
            }

    /**
     * 根据分类主键查询分类详情。
     */
    @Override
    public CategoryVO getById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.CATEGORY_NOT_FOUND));
        return toCategoryVO(category);
    }

    /**
     * 新增分类。
     *
     * @param categoryDTO 分类新增参数
     */
    @Override
    public void create(CategoryDTO categoryDTO) {
        String categoryName = categoryDTO.getCategoryName().trim();
        catalogValidationService.checkCategoryNameUnique(categoryName);

        LocalDateTime now = LocalDateTime.now();

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setCategoryName(categoryName);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        categoryRepository.save(category);
        redisCacheService.delete(RedisKeyConstants.CATEGORY_LIST);
    }

    /**
     * 修改分类。
     *
     * @param categoryId 分类主键
     * @param categoryDTO 分类更新参数
     */
    @Override
    public void update(Long categoryId, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.CATEGORY_TO_UPDATE_NOT_FOUND));

        String categoryName = categoryDTO.getCategoryName().trim();
        catalogValidationService.checkCategoryNameUniqueExcludingId(categoryName, categoryId);

        category.setCategoryName(categoryName);
        category.setDescription(categoryDTO.getDescription());
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepository.save(category);
        redisCacheService.delete(RedisKeyConstants.CATEGORY_LIST);
    }

    /**
     * 删除分类。
     *
     * @param categoryId 分类主键
     */
    @Override
    public void delete(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException(MessageConstants.CATEGORY_TO_DELETE_NOT_FOUND);
        }

        categoryRepository.deleteById(categoryId);
        redisCacheService.delete(RedisKeyConstants.CATEGORY_LIST);
    }

    private CategoryVO toCategoryVO(Category category) {
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);

        return categoryVO;
    }
}
