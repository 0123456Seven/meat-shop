package ru.xaero.meat.core.db.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.xaero.meat.core.db.model.Product;
import ru.xaero.meat.core.db.repository.ProductRepository;
import ru.xaero.meat.dto.ProductCreateDTO;
import ru.xaero.meat.dto.ProductResponseDTO;
import ru.xaero.meat.dto.ProductUpdateDTO;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Преобразование Entity в DTO
    private ProductResponseDTO convertToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setArticle(product.getArticle());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setIsOnSale(product.getIsOnSale());
        dto.setQuantity(product.getQuantity());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setWeight(product.getWeight());
        return dto;
    }

    // Преобразование DTO в Entity
    private Product convertToEntity(ProductCreateDTO dto) {
        Product product = new Product();
        product.setArticle(dto.getArticle());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setIsOnSale(dto.getIsOnSale() != null ? dto.getIsOnSale() : false);
        product.setQuantity(dto.getQuantity());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setWeight(dto.getWeight());
        return product;
    }

    // 1. Получить все продукты (без пагинации)
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAllByIsDeletedFalse()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 2. Получить все продукты с пагинацией
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAllByIsDeletedFalse(pageable)
                .map(this::convertToDTO);
    }

    // 3. Получить продукт по ID
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Продукт с ID " + id + " не найден"));

        if (product.getIsDeleted()) {
            throw new NoSuchElementException("Продукт с ID " + id + " был удален");
        }

        return convertToDTO(product);
    }

    // 4. Получить продукт по артикулу
    public ProductResponseDTO getProductByArticle(String article) {
        Product product = productRepository.findByArticleAndIsDeletedFalse(article)
                .orElseThrow(() -> new NoSuchElementException("Продукт с артикулом " + article + " не найден"));

        return convertToDTO(product);
    }

    // 5. Создать новый продукт
    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO dto) {
        // Проверка уникальности артикула
        if (productRepository.existsByArticleAndIsDeletedFalse(dto.getArticle())) {
            throw new IllegalArgumentException("Продукт с артикулом " + dto.getArticle() + " уже существует");
        }

        Product product = convertToEntity(dto);
        Product savedProduct = productRepository.save(product);

        return convertToDTO(savedProduct);
    }

    // 6. Обновить продукт
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Продукт с ID " + id + " не найден"));

        if (product.getIsDeleted()) {
            throw new NoSuchElementException("Продукт с ID " + id + " был удален");
        }

        // Обновляем только те поля, которые пришли в DTO
        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getSalePrice() != null) {
            product.setSalePrice(dto.getSalePrice());
        }
        if (dto.getIsOnSale() != null) {
            product.setIsOnSale(dto.getIsOnSale());
        }
        if (dto.getQuantity() != null) {
            product.setQuantity(dto.getQuantity());
        }
        if (dto.getCategory() != null) {
            product.setCategory(dto.getCategory());
        }
        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }
        if (dto.getWeight() != null) {
            product.setWeight(dto.getWeight());
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    // 7. Удалить продукт (мягкое удаление)
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("Продукт с ID " + id + " не найден");
        }

        productRepository.softDelete(id);
    }

    // 8. Получить продукты по категории (простая фильтрация)
    public List<ProductResponseDTO> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndIsDeletedFalse(category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
