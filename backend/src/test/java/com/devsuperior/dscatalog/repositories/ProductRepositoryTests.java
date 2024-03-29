package com.devsuperior.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.tests.factory.Factory;

@DataJpaTest
public class ProductRepositoryTests {
	
	@Autowired
	private ProductRepository productRepository;
	
	private long exintingId;
	private long nonExistingId;
	private long countTotalProducts;
	
	@BeforeEach
	void setup()throws Exception{
		exintingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
	}
	@Test
	public void saveShouldPersistWithAutoincrementWhenIdIsNull() {
		Product product = Factory.createProduct();
		product.setId(null);
		
		product =  productRepository.save(product);
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());
	}
	
	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {
		
		productRepository.deleteById( exintingId );
		
		Optional<Product> result = productRepository.findById(exintingId);
		Assertions.assertFalse(result.isPresent());
	}
	
	@Test
	public void deleteShouldThroWEmptyResultDataAcessExceptionWhenIdDoesNotExists() {

		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			productRepository.deleteById(nonExistingId);
		});
	}
	@Test
	public void findByIdShouldReturnNotEmptyOptionalWhenIdIdExists() {
		
		Optional<Product> result = productRepository.findById(exintingId);
		Assertions.assertTrue(result.isPresent());
	}
	
	@Test
	public void findByIdShouldReturnEmptyOptionalWhenIdIdExists() {
		
		Optional<Product> result = productRepository.findById(nonExistingId);
		Assertions.assertFalse(result.isPresent());
	}
	
}
