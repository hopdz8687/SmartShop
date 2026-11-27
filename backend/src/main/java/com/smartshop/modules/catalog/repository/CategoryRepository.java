package com.smartshop.modules.catalog.repository;

import com.smartshop.modules.catalog.entity.Category;
import com.smartshop.modules.catalog.entity.CategoryStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @EntityGraph(attributePaths = {"parent"})
    List<Category> findByStatusOrderByNameAsc(CategoryStatus status);

    @EntityGraph(attributePaths = {"parent"})
    List<Category> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"parent"})
    Optional<Category> findWithParentById(Long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
