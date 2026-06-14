package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.dto.TagDTO;
import com.cpt202.exception.NotFoundException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Tag;
import com.cpt202.repository.TagRepository;
import com.cpt202.service.RedisCacheService;
import com.cpt202.service.impl.TagServiceImpl;
import com.cpt202.validation.CatalogValidationService;
import com.cpt202.vo.TagVO;
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

/** Unit tests for tag caching, lookup, validation, and deletion rules. */
@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private CatalogValidationService catalogValidationService;

    @InjectMocks
    private TagServiceImpl tagService;

    /** Loads tag values from the repository and populates the cache on a miss. */
    @Test
    void listAllShouldLoadRepositoryValuesAndPopulateCache() {
        Tag java = tag(1L, "Java");
        Tag spring = tag(2L, "Spring");

        when(redisCacheService.<List<TagVO>>get(eq(RedisKeyConstants.TAG_LIST), anyTagVoListTypeReference()))
                .thenReturn(Optional.empty());
        when(tagRepository.findAll()).thenReturn(List.of(java, spring));

        List<TagVO> result = tagService.listAll();

        assertThat(result).extracting(TagVO::getTagName).containsExactly("Java", "Spring");
        verify(redisCacheService).set(eq(RedisKeyConstants.TAG_LIST), any(List.class), eq(Duration.ofMinutes(30)));
    }

    /** Returns cached tags without querying the repository. */
    @Test
    void listAllShouldReturnCachedValuesWithoutRepositoryLookup() {
        TagVO cached = new TagVO(8L, "AI", "Artificial Intelligence");

        when(redisCacheService.<List<TagVO>>get(eq(RedisKeyConstants.TAG_LIST), anyTagVoListTypeReference()))
                .thenReturn(Optional.of(List.of(cached)));

        List<TagVO> result = tagService.listAll();

        assertThat(result).containsExactly(cached);
        verify(tagRepository, never()).findAll();
    }

    /** Maps a persisted tag entity to the public view object. */
    @Test
    void getByIdShouldMapTagVo() {
        Tag tag = tag(9L, "Security");
        tag.setDescription("Security description");

        when(tagRepository.findById(9L)).thenReturn(Optional.of(tag));

        TagVO result = tagService.getById(9L);

        assertThat(result.getTagId()).isEqualTo(9L);
        assertThat(result.getTagName()).isEqualTo("Security");
        assertThat(result.getDescription()).isEqualTo("Security description");
    }

    /** Rejects lookup when the tag id does not exist. */
    @Test
    void getByIdShouldRejectMissingTag() {
        when(tagRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.getById(10L));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.TAG_NOT_FOUND);
    }

    /** Rejects tag creation when the trimmed name already exists. */
    @Test
    void createShouldRejectDuplicateTagName() {
        TagDTO dto = tagDTO("  Java  ", "dup");

        doThrow(new RuleViolationException(MessageConstants.TAG_NAME_EXISTS))
                .when(catalogValidationService).checkTagNameUnique("Java");

        RuleViolationException exception = assertThrows(RuleViolationException.class,
                () -> tagService.create(dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.TAG_NAME_EXISTS);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    /** Trims the name, persists the tag, and evicts the list cache. */
    @Test
    void createShouldTrimNamePersistAndEvictCache() {
        TagDTO dto = tagDTO("  Cloud  ", "Cloud description");

        tagService.create(dto);

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        verify(redisCacheService).delete(RedisKeyConstants.TAG_LIST);

        Tag saved = tagCaptor.getValue();
        assertThat(saved.getTagName()).isEqualTo("Cloud");
        assertThat(saved.getDescription()).isEqualTo("Cloud description");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    /** Persists trimmed tag updates and evicts the list cache. */
    @Test
    void updateShouldTrimNamePersistAndEvictCache() {
        Tag existing = tag(4L, "Legacy");
        TagDTO dto = tagDTO("  Cloud  ", "Cloud native");

        when(tagRepository.findById(4L)).thenReturn(Optional.of(existing));

        tagService.update(4L, dto);

        verify(tagRepository).save(existing);
        verify(redisCacheService).delete(RedisKeyConstants.TAG_LIST);
        assertThat(existing.getTagName()).isEqualTo("Cloud");
        assertThat(existing.getDescription()).isEqualTo("Cloud native");
        assertThat(existing.getUpdatedAt()).isNotNull();
    }

    /** Rejects delete requests for tags that do not exist. */
    @Test
    void deleteShouldRejectMissingTag() {
        when(tagRepository.existsById(12L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.delete(12L));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.TAG_TO_DELETE_NOT_FOUND);
        verify(redisCacheService, never()).delete(RedisKeyConstants.TAG_LIST);
    }

    /** Deletes an existing tag and evicts the cached list. */
    @Test
    void deleteShouldDeleteExistingTagAndEvictCache() {
        when(tagRepository.existsById(13L)).thenReturn(true);

        tagService.delete(13L);

        verify(tagRepository).deleteById(13L);
        verify(redisCacheService).delete(RedisKeyConstants.TAG_LIST);
    }

    private TagDTO tagDTO(String tagName, String description) {
        TagDTO dto = new TagDTO();
        dto.setTagName(tagName);
        dto.setDescription(description);
        return dto;
    }

    private Tag tag(Long tagId, String tagName) {
        Tag tag = new Tag();
        ReflectionTestUtils.setField(tag, "tagId", tagId);
        tag.setTagName(tagName);
        return tag;
    }

    private TypeReference<List<TagVO>> anyTagVoListTypeReference() {
        return any();
    }
}