package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.finance.dto.CategoryRequest;
import com.mbclab.lablink.features.finance.dto.CategoryResponse;
import com.mbclab.lablink.shared.exception.BusinessValidationException;
import com.mbclab.lablink.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceCategoryService {

    private final FinanceCategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessValidationException("Kategori dengan nama '" + request.getName() + "' sudah ada");
        }
        
        FinanceCategory category = new FinanceCategory();
        category.setName(request.getName());
        category.setType(request.getType() != null ? request.getType() : "BOTH");
        category.setDescription(request.getDescription());
        
        FinanceCategory saved = categoryRepository.save(category);
        
        eventPublisher.publishEvent(AuditEvent.create(
                "FINANCE_CATEGORY", saved.getId(), saved.getName(),
                "Created finance category: " + saved.getName()));
        
        return toCategoryResponse(saved);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getCategoriesByType(String type) {
        return categoryRepository.findByTypeAndIsActiveTrue(type).stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        FinanceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori tidak ditemukan"));
        
        if (request.getName() != null) category.setName(request.getName());
        if (request.getType() != null) category.setType(request.getType());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        
        FinanceCategory saved = categoryRepository.save(category);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "FINANCE_CATEGORY", saved.getId(), saved.getName(),
                "Updated finance category"));
        
        return toCategoryResponse(saved);
    }

    @Transactional
    public void deleteCategory(String id) {
        FinanceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori tidak ditemukan"));
        
        category.setActive(false);
        categoryRepository.save(category);
        
        eventPublisher.publishEvent(AuditEvent.delete(
                "FINANCE_CATEGORY", id, category.getName(),
                "Deactivated finance category"));
    }

    // ==================== HELPER ====================

    private CategoryResponse toCategoryResponse(FinanceCategory c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType())
                .description(c.getDescription())
                .isActive(c.isActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
