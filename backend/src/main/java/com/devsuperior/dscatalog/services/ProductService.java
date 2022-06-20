package com.devsuperior.dscatalog.services;


import java.util.Optional;


import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repository.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;


@Service
public class ProductService {

	@Autowired
	private ProductRepository categoryRepository;

	@Transactional (readOnly = true)
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest){
		Page<Product>listPaged = categoryRepository.findAll(pageRequest);
		return listPaged.map(x -> new ProductDTO(x));
		
	}

	@Transactional (readOnly = true)
	public ProductDTO findById(Long id) {
		Optional <Product> obj = categoryRepository.findById(id);
		Product entity = obj.orElseThrow(() -> new EntityNotFoundException("Entity not found"));
		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		//entity.setName(dto.getName());
		entity = categoryRepository.save(entity);
		return new ProductDTO(entity);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity =  categoryRepository.getOne(id);
			//entity.setName(dto.getName());
			entity = categoryRepository.save(entity);
			return new ProductDTO(entity);
		}

		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
	}

	public void delete(Long id) {
		
		try {
			categoryRepository.deleteById(id);
		} 
		catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found" +id);
		}
		
		catch(DataIntegrityViolationException e) {
			throw new DataBaseException("Integrity violation");
			
		}
		
		
	}
}
