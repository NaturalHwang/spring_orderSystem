package beyond.orderSystem.product.service;

import beyond.orderSystem.product.domain.Product;
import beyond.orderSystem.product.dto.ProductResDto;
import beyond.orderSystem.product.dto.ProductSaveReqDto;
import beyond.orderSystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public Product productCreate(ProductSaveReqDto dto){
        MultipartFile image = dto.getProductImage();
        Product product = null;
        try {
            product = productRepository.save(dto.toEntity());
            byte[] bytes = image.getBytes();
//            Path path = Paths.get("C:/Users/jungh/OneDrive/Desktop/class/tmp/",
//                    UUID.randomUUID() + "_" + image.getOriginalFilename()); // 난수값이라 파일 이름이 복잡
            Path path = Paths.get("C:/Users/jungh/OneDrive/Desktop/class/tmp/",
                    product.getId() + "_" + image.getOriginalFilename());
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            product.updateImagePath(path.toString());
        } catch (IOException e) {
            throw new RuntimeException("이미지를 불러올 수 없습니다.");
        }
        return product;
    }

    public Page<ProductResDto> productList(Pageable pageable){
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(Product::fromEntity);
    }

    public Product productAwsCreate(ProductSaveReqDto dto){
        MultipartFile image = dto.getProductImage();
        Product product = null;
        try {
            product = productRepository.save(dto.toEntity());
            byte[] bytes = image.getBytes();
            Path path = Paths.get("C:/Users/jungh/OneDrive/Desktop/class/tmp/",
                    product.getId() + "_" + image.getOriginalFilename());
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            product.updateImagePath(path.toString());
        } catch (IOException e) {
            throw new RuntimeException("이미지를 불러올 수 없습니다.");
        }
        return product;
    }
}
