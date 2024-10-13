package com.finalproject.stayease.property.controller;

import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.CategoryDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/categories")
@Slf4j
@Data
public class CategoriesController {

  private final PropertyCategoryService propertyCategoryService;
  private final UsersService usersService;

  @GetMapping
  public ResponseEntity<Response<List<CategoryDTO>>> getAllCategories() {
    List<PropertyCategory> categoryList = propertyCategoryService.findAll();
    List<CategoryDTO> categoryDTOList = categoryList.stream()
        .map(CategoryDTO::new)
        .sorted(Comparator.comparing(CategoryDTO::getName))
        .toList();
    return Response.successfulResponse(200, "Listing all categories", categoryDTOList);
  }

  @PostMapping
  public ResponseEntity<Response<CategoryDTO>> addCategory(@RequestBody CreateCategoryRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.CREATED.value(), "Category added!", new CategoryDTO(
        propertyCategoryService.createCategory(tenant, requestDTO)));
  }

  @PutMapping("/categories/{categoryId}")
  public ResponseEntity<Response<CategoryDTO>> updateCategory(@PathVariable Long categoryId,
      @RequestBody UpdateCategoryRequestDTO requestDTO) {
    Users tenant = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.OK.value(), "Category updated!", new CategoryDTO(
        propertyCategoryService.updateCategory(categoryId, tenant, requestDTO)));
  }

  @DeleteMapping("/categories/{categoryId}")
  public ResponseEntity<Response<Object>> deleteCategory(@PathVariable Long categoryId) {
    Users tenant = usersService.getLoggedUser();
    propertyCategoryService.deleteCategory(categoryId, tenant);
    return Response.successfulResponse(HttpStatus.OK.value(), "Category successfully deleted!", null);
  }

}
