package com.cpt202.unit.validation.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.repository.TagRepository;
import com.cpt202.validation.impl.CatalogValidationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Unit tests for catalog-domain validation rules. */
@ExtendWith(MockitoExtension.class)
class CatalogValidationServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private CatalogValidationServiceImpl catalogValidationService;

    /* ---- checkCategoryNameUnique ---- */

    @Test
    void checkCategoryNameUniqueShouldPassWhenNotExists() {
        when(categoryRepository.existsByCategoryNameIgnoreCase("AI")).thenReturn(false);
        assertDoesNotThrow(() -> catalogValidationService.checkCategoryNameUnique("AI"));
    }

    @Test
    void checkCategoryNameUniqueShouldRejectWhenExists() {
        when(categoryRepository.existsByCategoryNameIgnoreCase("AI")).thenReturn(true);
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> catalogValidationService.checkCategoryNameUnique("AI"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.CATEGORY_NAME_EXISTS);
    }

    /* ---- checkCategoryNameUniqueExcludingId ---- */

    @Test
    void checkCategoryNameUniqueExcludingIdShouldPassWhenNotExists() {
        when(categoryRepository.existsByCategoryNameIgnoreCaseAndCategoryIdNot("AI", 5L)).thenReturn(false);
        assertDoesNotThrow(() -> catalogValidationService.checkCategoryNameUniqueExcludingId("AI", 5L));
    }

    @Test
    void checkCategoryNameUniqueExcludingIdShouldRejectWhenExists() {
        when(categoryRepository.existsByCategoryNameIgnoreCaseAndCategoryIdNot("AI", 5L)).thenReturn(true);
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> catalogValidationService.checkCategoryNameUniqueExcludingId("AI", 5L));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.CATEGORY_NAME_EXISTS);
    }

    /* ---- checkTagNameUnique ---- */

    @Test
    void checkTagNameUniqueShouldPassWhenNotExists() {
        when(tagRepository.existsByTagNameIgnoreCase("Java")).thenReturn(false);
        assertDoesNotThrow(() -> catalogValidationService.checkTagNameUnique("Java"));
    }

    @Test
    void checkTagNameUniqueShouldRejectWhenExists() {
        when(tagRepository.existsByTagNameIgnoreCase("Java")).thenReturn(true);
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> catalogValidationService.checkTagNameUnique("Java"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.TAG_NAME_EXISTS);
    }

    /* ---- checkTagNameUniqueExcludingId ---- */

    @Test
    void checkTagNameUniqueExcludingIdShouldPassWhenNotExists() {
        when(tagRepository.existsByTagNameIgnoreCaseAndTagIdNot("Java", 10L)).thenReturn(false);
        assertDoesNotThrow(() -> catalogValidationService.checkTagNameUniqueExcludingId("Java", 10L));
    }

    @Test
    void checkTagNameUniqueExcludingIdShouldRejectWhenExists() {
        when(tagRepository.existsByTagNameIgnoreCaseAndTagIdNot("Java", 10L)).thenReturn(true);
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> catalogValidationService.checkTagNameUniqueExcludingId("Java", 10L));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.TAG_NAME_EXISTS);
    }

    /** Verifies that self-exclusion works: same-name-same-id passes. */
    @Test
    void checkCategoryNameUniqueExcludingIdShouldPassForSelf() {
        when(categoryRepository.existsByCategoryNameIgnoreCaseAndCategoryIdNot("AI", 5L)).thenReturn(false);
        assertDoesNotThrow(() -> catalogValidationService.checkCategoryNameUniqueExcludingId("AI", 5L));
    }
}
