package com.cpt202.validation.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.repository.TagRepository;
import com.cpt202.validation.CatalogValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 目录管理领域约束验证服务实现。
 */
@Service
@RequiredArgsConstructor
public class CatalogValidationServiceImpl implements CatalogValidationService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Override
    public void checkCategoryNameUnique(String categoryName) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(categoryName)) {
            throw new RuleViolationException(MessageConstants.CATEGORY_NAME_EXISTS);
        }
    }

    @Override
    public void checkCategoryNameUniqueExcludingId(String categoryName, Long excludeId) {
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndCategoryIdNot(categoryName, excludeId)) {
            throw new RuleViolationException(MessageConstants.CATEGORY_NAME_EXISTS);
        }
    }

    @Override
    public void checkTagNameUnique(String tagName) {
        if (tagRepository.existsByTagNameIgnoreCase(tagName)) {
            throw new RuleViolationException(MessageConstants.TAG_NAME_EXISTS);
        }
    }

    @Override
    public void checkTagNameUniqueExcludingId(String tagName, Long excludeId) {
        if (tagRepository.existsByTagNameIgnoreCaseAndTagIdNot(tagName, excludeId)) {
            throw new RuleViolationException(MessageConstants.TAG_NAME_EXISTS);
        }
    }
}
