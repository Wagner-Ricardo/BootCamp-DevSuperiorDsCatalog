package com.devsuperior.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.factory.Factory;
import com.devsuperior.dscatalog.tests.factory.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

//@WebMvcTest(ProductResource.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductResourcesTests {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ProductService productService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private TokenUtil tokenUtil;
	
	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	private ProductDTO productDTO;
	private PageImpl<ProductDTO> page;
	
	private String username;
	private String password;
	
	// simulando o comportamento das camadas da aplicação web
	@BeforeEach
	void setup() throws Exception{
		
		username = "maria@gmail.com";
		password = "123456";
		
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		
		productDTO = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productDTO));
		
		when(productService.findAllPaged(any(), any(), any())).thenReturn(page);
		
		when(productService.findById(existingId)).thenReturn(productDTO);
		when(productService.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		when(productService.insert(any())).thenReturn(productDTO);
		
		when(productService.update(eq(existingId),any())).thenReturn(productDTO);
		when(productService.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);
		
		doNothing().when(productService).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(productService).delete(nonExistingId);
		doThrow(DataBaseException.class).when(productService).delete(dependentId);
		
		
	}
	
	@Test
	public void findAllShouldReturnPage() throws Exception{
		
		ResultActions result = mockMvc.perform(get("/products")
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
	}
	
	@Test
	public void findByIdShouldReturnProductWhenIdExists()throws Exception {
		
		ResultActions result = mockMvc.perform(get("/products/{id}",existingId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenDoesNotIdExists() throws Exception {
		ResultActions result = mockMvc.perform(get("/products/{id}",nonExistingId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNotFound());
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception{
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
				.header("Authorization", "Bearer" + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenDoesNotExists() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result =  mockMvc.perform(put("/products/{id}",nonExistingId)
				.header("Authorization", "Bearer" + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNotFound());
		
	}
	@Test
	public void insertShouldReturnProductDTOCreated() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = mockMvc.perform(post("/products")
				.header("Authorization", "Bearer" + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
		
	}
	
	@Test
	public void deleteShouldReturnNoContentWhenIdExists() throws Exception{
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
		
		ResultActions result = mockMvc.perform(delete("/products/{id}", existingId)
				.header("Authorization", "Bearer" + accessToken)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNoContent());
	}
	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExists() throws Exception{
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
		
		ResultActions result = mockMvc.perform(delete("/products/{id}", nonExistingId )
				.header("Authorization", "Bearer" + accessToken)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNotFound());
	}
	
}
