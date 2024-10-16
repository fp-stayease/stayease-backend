package com.finalproject.stayease.property.service.helpers;

import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.CategoryNotFoundException;
import com.finalproject.stayease.exceptions.properties.DuplicateCategoryException;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.repository.PropertyCategoryRepository;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
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
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
public class PropertyCategoryHelper {

  private final PropertyCategoryRepository propertyCategoryRepository;
  private final LevenshteinDistance levenshteinDistance;
  private final JaroWinklerSimilarity jaroWinklerSimilarity;
  private final Map<String, Set<String>> synonyms;

  public PropertyCategoryHelper(PropertyCategoryRepository propertyCategoryRepository) {
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

  public void isTenant(Users tenant) {
    if (tenant.getUserType() != UserType.TENANT) {
      throw new UnauthorizedOperationsException("Only Tenants can create properties");
    }
  }

  public String normalizeCategoryName(String name) {
    return name.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
  }

  public void checkMatch(String requestedName) {
    // * 1 exact match
    Optional<PropertyCategory> categoryOptional = propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull(requestedName);
    if (categoryOptional.isPresent()) {
      throw new DuplicateCategoryException("Category already exist.");
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
      throw new DuplicateCategoryException("Similar categories exist: " + String.join(", ", similarCategories));
    }
  }

  public boolean isSimilarName(String name1, String name2) {
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

  public boolean areSynonyms(String word1, String word2) {
    for (Set<String> synSet : synonyms.values()) {
      if (synSet.contains(word1) && synSet.contains(word2)) {
        return true;
      }
    }
    return false;
  }

  public PropertyCategory toPropertyCategoryEntity(Users tenant, CreateCategoryRequestDTO requestDTO) {
    PropertyCategory category = new PropertyCategory();
    category.setName(capitalizeWords(requestDTO.getName()));
    category.setAddedBy(tenant);
    propertyCategoryRepository.save(category);
    return category;
  }

  public String capitalizeWords(String str) {
    String[] words = str.split("\\s+");
    StringBuilder capitalized = new StringBuilder();
    for (String word : words) {
      if (!word.isEmpty()) {
        capitalized.append(Character.toUpperCase(word.charAt(0)))
            .append(word.substring(1).toLowerCase())
            .append(" ");
      }
    }
    return capitalized.toString().trim();
  }

  public PropertyCategory checkIfValid(Users tenant, Long categoryId) {
    PropertyCategory existingCategory = propertyCategoryRepository.findByIdAndDeletedAtIsNull(categoryId).orElseThrow(
        () -> new CategoryNotFoundException("Category with this ID does not exist")
    );
    isTenant(tenant);
    Users categoryAuthor = existingCategory.getAddedBy();
    if (tenant != categoryAuthor) {
      throw new UnauthorizedOperationsException("You are not the creator of this category");
    }
    return existingCategory;
  }
}
