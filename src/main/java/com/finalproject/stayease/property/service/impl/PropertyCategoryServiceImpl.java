package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.property.repository.PropertyCategoryRepository;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
@Slf4j
public class PropertyCategoryServiceImpl implements PropertyCategoryService {

  private final PropertyCategoryRepository propertyCategoryRepository;
  private final LevenshteinDistance levenshteinDistance;
  private final JaroWinklerSimilarity jaroWinklerSimilarity;
  private final Map<String, Set<String>> synonyms;

  public PropertyCategoryServiceImpl(PropertyCategoryRepository propertyCategoryRepository) {
    this.propertyCategoryRepository = propertyCategoryRepository;
    this.levenshteinDistance = new LevenshteinDistance();
    this.jaroWinklerSimilarity = new JaroWinklerSimilarity();
    this.synonyms = initializeSynonyms();
  }

  private Map<String, Set<String>> initializeSynonyms() {
    Map<String, Set<String>> synonymMap = new HashMap<>();
    synonymMap.put("apartment", new HashSet<>(Arrays.asList("apartment", "flat", "suite", "unit", "condo",
        "condominium")));
    synonymMap.put("house", new HashSet<>(Arrays.asList("house", "home", "residence", "dwelling")));
    synonymMap.put("villa", new HashSet<>(Arrays.asList("villa", "chateau", "mansion", "estate")));
    synonymMap.put("cottage", new HashSet<>(Arrays.asList("cottage", "cabin", "bungalow", "chalet")));
    synonymMap.put("bed and breakfast", new HashSet<>(Arrays.asList("bed and breakfast", "b&b", "bnb", "guesthouse")));
    synonymMap.put("hostel", new HashSet<>(Arrays.asList("hostel", "dormitory", "dorm")));
    synonymMap.put("motel", new HashSet<>(Arrays.asList("motel", "motor hotel", "motor inn")));
    synonymMap.put("resort", new HashSet<>(Arrays.asList("resort", "retreat", "spa")));
    synonymMap.put("penthouse", new HashSet<>(Arrays.asList("penthouse", "luxury apartment", "top-floor suite")));
    synonymMap.put("studio", new HashSet<>(Arrays.asList("studio", "efficiency apartment", "bachelor apartment")));
    synonymMap.put("loft", new HashSet<>(Arrays.asList("loft", "attic apartment", "converted warehouse")));
    synonymMap.put("vacation rental", new HashSet<>(Arrays.asList("vacation rental", "holiday home", "vacation home",
        "short-term rental")));
    synonymMap.put("farmhouse", new HashSet<>(Arrays.asList("farmhouse", "ranch house", "country house")));
    synonymMap.put("townhouse", new HashSet<>(Arrays.asList("townhouse", "row house", "terraced house")));
    return synonymMap;
  }

  @Override
  public PropertyCategory createCategory(Users tenant, CreateCategoryRequestDTO requestDTO) {
    // * validity checks
    isTenant(tenant);
    checkMatch(requestDTO.getName());

    // * if pass
    return toPropertyCategoryEntity(tenant, requestDTO);
  }

  @Override
  public PropertyCategory updateCategory(Long categoryId, Users tenant, UpdateCategoryRequestDTO requestDTO) {
    PropertyCategory existingCategory = checkIfValid(tenant, categoryId);

    // TODO : if name is updateable, do checkMatch on the name

    // update category
    Optional.ofNullable(requestDTO.getDescription()).ifPresent(existingCategory::setDescription);
    propertyCategoryRepository.save(existingCategory);
    return existingCategory;
  }

  @Override
  public void deleteCategory(Long categoryId, Users tenant) {
    PropertyCategory existingCategory = checkIfValid(tenant, categoryId);
    existingCategory.setDeletedAt(Instant.now());
    propertyCategoryRepository.save(existingCategory);
  }

  @Override
  public Optional<PropertyCategory> findCategoryByIdAndNotDeleted(Long id) {
    return propertyCategoryRepository.findByIdAndDeletedAtIsNull(id);
  }

  private void isTenant(Users tenant) {
    if (tenant.getUserType() != UserType.TENANT) {
      throw new InvalidRequestException("Only Tenants can create properties");
    }
  }

  private String normalizeCategoryName(String name) {
    return name.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
  }

  private void checkMatch(String requestedName) {
    // * 1 exact match
    Optional<PropertyCategory> categoryOptional = propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull(requestedName);
    if (categoryOptional.isPresent()) {
      // TODO: make DuplicateCategoryException
      throw new DuplicateEntryException("Category already exist.");
    }

    // * similar names
    List<PropertyCategory> allCategories = propertyCategoryRepository.findAll();
    List<String> similarCategories = new ArrayList<>();
    for (PropertyCategory category : allCategories) {
      if (isSimilarName(requestedName, category.getName())) {
        similarCategories.add(category.getName());
      }
    }
    if (!similarCategories.isEmpty()) {
      // TODO: make SimilarCategoryException
      throw new DuplicateEntryException("Similar categories exist: " + String.join(", ", similarCategories));
    }
  }

  private boolean isSimilarName(String name1, String name2) {
    String normalized1 = normalizeCategoryName(name1);
    String normalized2 = normalizeCategoryName(name2);

    // check for synonyms
    if (areSynonyms(normalized1, normalized2)) {
      return  true;
    }

    // Levenshtein distance check
    int distance = levenshteinDistance.apply(normalized1, normalized2);
    int maxLength = Math.max(normalized1.length(), normalized2.length());
    if (distance < (maxLength * 0.2)) {
      return true;
    }

    // Jaro-Winkler similarity check
    double similarity = jaroWinklerSimilarity.apply(normalized1, normalized2);
    return similarity > 0.9;
  }

  // TODO : consider if need
  private boolean areSynonyms(String word1, String word2) {
    for (Set<String> synSet : synonyms.values()) {
      if (synSet.contains(word1) && synSet.contains(word2)) {
        return true;
      }
    }
    return false;
  }

  private PropertyCategory toPropertyCategoryEntity(Users tenant, CreateCategoryRequestDTO requestDTO) {
    PropertyCategory category = new PropertyCategory();
    category.setName(requestDTO.getName().toLowerCase());
    category.setDescription(requestDTO.getDescription());
    category.setAddedBy(tenant);
    propertyCategoryRepository.save(category);
    return category;
  }

  private PropertyCategory checkIfValid(Users tenant, Long categoryId) {
    // TODO : make ex CategoryNotFoundException
    PropertyCategory existingCategory = propertyCategoryRepository.findByIdAndDeletedAtIsNull(categoryId).orElseThrow(
        () -> new InvalidRequestException("Category with this ID does not exist")
    );
    isTenant(tenant);
    Users categoryAuthor = existingCategory.getAddedBy();
    if (tenant != categoryAuthor) {
      throw new BadCredentialsException("You are not the creator of this category");
    }
    return existingCategory;
  }
}
