package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.dto.CategoryDTO;
import com.cpt202.exception.NotFoundException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Category;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.service.RedisCacheService;
import com.cpt202.service.impl.CategoryServiceImpl;
import com.cpt202.validation.CatalogValidationService;
import com.cpt202.vo.CategoryVO;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for category caching, lookup, validation, and deletion rules. */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private CatalogValidationService catalogValidationService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    /** Returns cached category values without querying the repository. */
    @Test
    void listAllShouldReturnCachedValuesWithoutRepositoryLookup() {
        CategoryVO cached = new CategoryVO();
        cached.setCategoryId(1L);
        cached.setCategoryName("AI");
        cached.setDescription("Artificial Intelligence");

        when(redisCacheService.<List<CategoryVO>>get(eq(RedisKeyConstants.CATEGORY_LIST), anyCategoryVoListTypeReference()))
                .thenReturn(Optional.of(List.of(cached)));

        List<CategoryVO> result = categoryService.listAll();

        assertThat(result).containsExactly(cached);
        verify(categoryRepository, never()).findAll();
    }

    /** Loads categories from the repository and populates the cache on a miss. */
    @Test
    void listAllShouldLoadRepositoryValuesAndPopulateCache() {
        Category ai = category(2L, "AI");
        ai.setDescription("Artificial Intelligence");
        Category ds = category(3L, "Distributed Systems");
        ds.setDescription("Distributed Systems");

        when(redisCacheService.<List<CategoryVO>>get(eq(RedisKeyConstants.CATEGORY_LIST), anyCategoryVoListTypeReference()))
                .thenReturn(Optional.empty());
        when(categoryRepository.findAll()).thenReturn(List.of(ai, ds));

        List<CategoryVO> result = categoryService.listAll();

        assertThat(result).extracting(CategoryVO::getCategoryName).containsExactly("AI", "Distributed Systems");
        verify(redisCacheService).set(eq(RedisKeyConstants.CATEGORY_LIST), any(List.class), eq(Duration.ofMinutes(30)));
    }

    /** Maps a persisted category entity to the public view object. */
    @Test
    void getByIdShouldMapCategoryVo() {
        Category category = category(4L, "Cloud Computing");
        category.setDescription("Cloud description");

        when(categoryRepository.findById(4L)).thenReturn(Optional.of(category));

        CategoryVO result = categoryService.getById(4L);

        assertThat(result.getCategoryId()).isEqualTo(4L);
        assertThat(result.getCategoryName()).isEqualTo("Cloud Computing");
        assertThat(result.getDescription()).isEqualTo("Cloud description");
    }

    /** Rejects lookup when the category id does not exist. */
    @Test
    void getByIdShouldRejectMissingCategory() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.getById(5L));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.CATEGORY_NOT_FOUND);
    }

    /** Trims the name, persists the category, and evicts the list cache. */
    @Test
    void createShouldTrimNamePersistAndEvictCache() {
        CategoryDTO dto = categoryDTO("  AI  ", "Artificial Intelligence");

        categoryService.create(dto);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        verify(redisCacheService).delete(RedisKeyConstants.CATEGORY_LIST);

        Category saved = categoryCaptor.getValue();
        assertThat(saved.getCategoryName()).isEqualTo("AI");
        assertThat(saved.getDescription()).isEqualTo("Artificial Intelligence");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    /** Rejects category updates that would create a duplicate trimmed name. */
    @Test
    void updateShouldRejectDuplicateCategoryName() {
        Category existing = category(3L, "Distributed Systems");
        CategoryDTO dto = categoryDTO("  AI  ", "dup");

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(existing));
        doThrow(new RuleViolationException(MessageConstants.CATEGORY_NAME_EXISTS))
                .when(catalogValidationService).checkCategoryNameUniqueExcludingId("AI", 3L);

        RuleViolationException exception = assertThrows(RuleViolationException.class,
                () -> categoryService.update(3L, dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.CATEGORY_NAME_EXISTS);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    /** Persists trimmed category updates and evicts the list cache. */
    @Test
    void updateShouldTrimNamePersistAndEvictCache() {
        Category existing = category(6L, "Legacy");
        CategoryDTO dto = categoryDTO("  Cloud  ", "Cloud native");

        when(categoryRepository.findById(6L)).thenReturn(Optional.of(existing));

        categoryService.update(6L, dto);

        verify(categoryRepository).save(existing);
        verify(redisCacheService).delete(RedisKeyConstants.CATEGORY_LIST);
        assertThat(existing.getCategoryName()).isEqualTo("Cloud");
        assertThat(existing.getDescription()).isEqualTo("Cloud native");
        assertThat(existing.getUpdatedAt()).isNotNull();
    }

    /** Rejects delete requests for categories that do not exist. */
    @Test
    void deleteShouldRejectMissingCategory() {
        when(categoryRepository.existsById(9L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.delete(9L));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.CATEGORY_TO_DELETE_NOT_FOUND);
        verify(redisCacheService, never()).delete(RedisKeyConstants.CATEGORY_LIST);
    }

    /** Deletes an existing category and evicts the cached list. */
    @Test
    void deleteShouldDeleteExistingCategoryAndEvictCache() {
        when(categoryRepository.existsById(10L)).thenReturn(true);

        categoryService.delete(10L);

        verify(categoryRepository).deleteById(10L);
        verify(redisCacheService).delete(RedisKeyConstants.CATEGORY_LIST);
    }

    private CategoryDTO categoryDTO(String categoryName, String description) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryName(categoryName);
        dto.setDescription(description);
        return dto;
    }

    private Category category(Long categoryId, String categoryName) {
        Category category = new Category();
        ReflectionTestUtils.setField(category, "categoryId", categoryId);
        category.setCategoryName(categoryName);
        return category;
    }

    private TypeReference<List<CategoryVO>> anyCategoryVoListTypeReference() {
        return any();
    }
}