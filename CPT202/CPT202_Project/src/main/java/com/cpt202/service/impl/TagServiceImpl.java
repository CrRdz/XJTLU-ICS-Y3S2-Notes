package com.cpt202.service.impl;

import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.dto.TagDTO;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.Tag;
import com.cpt202.result.PageResult;
import com.cpt202.repository.TagRepository;
import com.cpt202.service.RedisCacheService;
import com.cpt202.service.TagService;
import com.cpt202.util.VoConverter;
import com.cpt202.validation.CatalogValidationService;
import com.cpt202.vo.TagVO;
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
 * 标签管理服务实现类。
 * 当前仅保留方法定义，后续由仓储层完成真实持久化实现。
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private static final Duration TAG_CACHE_TTL = Duration.ofMinutes(30);

    private final TagRepository tagRepository;
    private final RedisCacheService redisCacheService;
    private final CatalogValidationService catalogValidationService;

    /**
     * 查询全部标签列表。
     *
     * @return 标签展示对象列表
     */
    @Override
    public List<TagVO> listAll() {
        return redisCacheService.get(RedisKeyConstants.TAG_LIST, new TypeReference<List<TagVO>>() { })
                .orElseGet(() -> {
                    List<TagVO> tagVos = VoConverter.toList(
                            tagRepository.findAll(), this::toTagVO);
                    redisCacheService.set(RedisKeyConstants.TAG_LIST, tagVos, TAG_CACHE_TTL);
                    return tagVos;
                });
    }

            @Override
            public PageResult<TagVO> listPage(PageQueryDTO queryDTO) {
            Pageable pageable = PageRequest.of(
                Math.max(0, queryDTO.getPageNum() - 1),
                queryDTO.getPageSize(),
                Sort.by(Sort.Direction.ASC, "tagId"));
            Page<Tag> tagPage = tagRepository.findAll(pageable);
            List<TagVO> tagVos = tagPage.getContent().stream()
                .map(this::toTagVO)
                .toList();
            return PageResult.fromPage(tagPage, tagVos);
            }

    /**
     * 根据标签主键查询标签详情。
     *
     * @param tagId 标签主键
     * @return 标签展示对象
     */
    @Override
    public TagVO getById(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.TAG_NOT_FOUND));
        return toTagVO(tag);
    }

    /**
     * 新增标签。
     *
     * @param tagDTO 标签新增参数
     */
    @Override
    public void create(TagDTO tagDTO) {
        String tagName = tagDTO.getTagName().trim();
        catalogValidationService.checkTagNameUnique(tagName);

        LocalDateTime now = LocalDateTime.now();
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagDTO, tag);
        tag.setTagName(tagName);
        tag.setCreatedAt(now);
        tag.setUpdatedAt(now);
        tagRepository.save(tag);
        redisCacheService.delete(RedisKeyConstants.TAG_LIST);
    }

    /**
     * 修改标签。
     *
     * @param tagId 标签主键
     * @param tagDTO 标签更新参数
     */
    @Override
    public void update(Long tagId, TagDTO tagDTO) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.TAG_TO_UPDATE_NOT_FOUND));

        String tagName = tagDTO.getTagName().trim();
        catalogValidationService.checkTagNameUniqueExcludingId(tagName, tagId);

        tag.setTagName(tagName);
        tag.setDescription(tagDTO.getDescription());
        tag.setUpdatedAt(LocalDateTime.now());

        tagRepository.save(tag);
        redisCacheService.delete(RedisKeyConstants.TAG_LIST);
    }

    /**
     * 删除标签。
     *
     * @param tagId 标签主键
     */
    @Override
    public void delete(Long tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new NotFoundException(MessageConstants.TAG_TO_DELETE_NOT_FOUND);
        }
        tagRepository.deleteById(tagId);
        redisCacheService.delete(RedisKeyConstants.TAG_LIST);
    }

    private TagVO toTagVO(Tag tag) {
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }
}
