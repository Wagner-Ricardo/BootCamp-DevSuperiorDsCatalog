package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.CategoryRepository;
import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.factory.Factory;

// trabalhando com teste de unidade

@ExtendWith(SpringExtension.class)
public class ProductServicesTests {
	
	@InjectMocks
	private ProductService productService;
	
	@Mock
	private ProductRepository productRepository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	private long existingId;
	private long nonExisting;
	private long dependentId;
	private PageImpl<Product>page;
	private Product product;
	private Category category;
	
	@BeforeEach
	void setup() throws Exception{
		existingId = 10L;
		nonExisting = 1000L;
		dependentId = 4L;
		product  = Factory.createProduct();
		category = Factory.createCategory();
		page = new PageImpl<>( List.of(product));
		
		
		Mockito.when(productRepository.findAll((Pageable)any())).thenReturn(page);
		
		Mockito.when(productRepository.save(any())).thenReturn(product);
		
		Mockito.when(productRepository.findById(existingId)).thenReturn(Optional.of(product));
		Mockito.when(productRepository.findById(nonExisting)).thenReturn(Optional.empty());	
		
		Mockito.when(productRepository.find(any(), any(), any())).thenReturn(page);	
		
		Mockito.when(productRepository.getOne(existingId)).thenReturn(product);
		Mockito.when(productRepository.getOne(nonExisting)).thenThrow(EntityNotFoundException.class);
		
		Mockito.when(categoryRepository.getOne(existingId)).thenReturn(category);
		Mockito.when(categoryRepository.getOne(nonExisting)).thenThrow(EntityNotFoundException.class);
		
		Mockito.doNothing().when(productRepository).deleteById(existingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(productRepository).deleteById(nonExisting);
		Mockito.doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId );
	}
	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() {
		
		ProductDTO result = productService.findById(existingId);
		Assertions.assertNotNull(result);
		Mockito.verify(productRepository,Mockito.times(1)).findById(existingId);
	}
	
	@Test
	public void findByIdShouldThroWResourceNotFoundExceptionWhenIdDoesNotExists() {
	
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
		productService.findById(nonExisting);
		});
		
	}
	
	@Test
	public void updateShouldThroWResourceNotFoundExceptionWhenIdDoesNotExists() {
		
		ProductDTO  productDTO = Factory.createProductDTO();
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			productService.update(nonExisting, productDTO);
		});
		
	}
	
	
	@Test
	public void updateShouldReturnProducDTOWhenIdExists() {
		
		ProductDTO  productDTO = Factory.createProductDTO();
		ProductDTO entity = productService.update(existingId, productDTO);
		Assertions.assertNotNull(entity);
	}
	

	
	@Test
	public void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<ProductDTO> result = productService.findAllPaged(0L, "", pageable);
		
		Assertions.assertNotNull(result);
		//Mockito.verify(productRepository, Mockito.times(1)).findAll(pageable);
	}
	
	@Test
	public void deleteShouldDataBaseExceptionWhenDependentId() {
		
		Assertions.assertThrows(DataBaseException.class, () -> {
			productService.delete(dependentId);
		});
		
		Mockito.verify(productRepository, Mockito.times(1)).deleteById(dependentId);
	}
	
	
	@Test
	public void deleteShouldDoNothingWhenIdExist() {
		
		Assertions.assertDoesNotThrow(() -> {
			productService.delete(existingId);
		});
		
		Mockito.verify(productRepository, Mockito.times(1)).deleteById(existingId);
	}
	
	@Test
	public void deleteShouldResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			productService.delete(nonExisting);
		});
		
		Mockito.verify(productRepository, Mockito.times(1)).deleteById(nonExisting);
	}
	
	
}
