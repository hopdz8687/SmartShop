package com.smartshop.modules.catalog.service;

import com.smartshop.common.exception.BusinessException;
import com.smartshop.common.exception.DuplicateResourceException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.common.util.SlugUtils;
import com.smartshop.modules.catalog.dto.CategoryRequest;
import com.smartshop.modules.catalog.dto.CategoryResponse;
import com.smartshop.modules.catalog.entity.Category;
import com.smartshop.modules.catalog.entity.CategoryStatus;
import com.smartshop.modules.catalog.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private static final int MAX_SLUG_LENGTH = 160;

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByStatusOrderByNameAsc(CategoryStatus.ACTIVE)
            .stream()
            .map(CategoryResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesForAdmin() {
        return categoryRepository.findAllByOrderByNameAsc()
            .stream()
            .map(CategoryResponse::from)
            .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category parent = resolveParent(request.getParentId(), null);
        String slug = allocateUniqueSlug(request.getName().trim(), null);

        Category category = Category.builder()
            .parent(parent)
            .name(request.getName().trim())
            .slug(slug)
            .description(trimToNull(request.getDescription()))
            .status(request.getStatus() != null ? request.getStatus() : CategoryStatus.ACTIVE)
            .build();

        Category saved = categoryRepository.saveAndFlush(category);
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findWithParentById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND", "Category not found"));

        Category parent = resolveParent(request.getParentId(), id);
        String newName = request.getName().trim();
        if (!newName.equalsIgnoreCase(category.getName())) {
            category.setSlug(allocateUniqueSlug(newName, id));
        }

        category.setParent(parent);
        category.setName(newName);
        category.setDescription(trimToNull(request.getDescription()));
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }

        Category saved = categoryRepository.saveAndFlush(category);
        return CategoryResponse.from(saved);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND", "Category not found"));
        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.saveAndFlush(category);
    }

    private Category resolveParent(Long parentId, Long currentCategoryId) {
        if (parentId == null) {
            return null;
        }
        if (currentCategoryId != null && parentId.equals(currentCategoryId)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARENT_CATEGORY",
                "Category cannot be its own parent"
            );
        }
        Category parent = categoryRepository.findWithParentById(parentId)
            .orElseThrow(() -> new ResourceNotFoundException("PARENT_CATEGORY_NOT_FOUND", "Parent category not found"));

        // Guard against circular parent hierarchy
        Category cursor = parent;
        while (cursor != null) {
            if (currentCategoryId != null && cursor.getId().equals(currentCategoryId)) {
                throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "CIRCULAR_CATEGORY_PARENT",
                    "Circular parent category reference detected"
                );
            }
            cursor = cursor.getParent();
        }
        return parent;
    }

    private String allocateUniqueSlug(String name, Long excludeId) {
        String base = SlugUtils.toSlugBase(name, MAX_SLUG_LENGTH, "category");
        if (!slugExists(base, excludeId)) {
            return base;
        }
        for (int suffix = 2; suffix <= 50; suffix++) {
            String candidate = base + "-" + suffix;
            if (!slugExists(candidate, excludeId)) {
                return candidate;
            }
        }
        String fallback = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        if (slugExists(fallback, excludeId)) {
            throw new DuplicateResourceException("CATEGORY_SLUG_CONFLICT", "Unable to allocate unique category slug");
        }
        return fallback;
    }

    private boolean slugExists(String slug, Long excludeId) {
        return excludeId == null
            ? categoryRepository.existsBySlug(slug)
            : categoryRepository.existsBySlugAndIdNot(slug, excludeId);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
