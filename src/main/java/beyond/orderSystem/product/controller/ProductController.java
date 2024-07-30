package beyond.orderSystem.product.controller;

import beyond.orderSystem.common.dto.CommonResDto;
import beyond.orderSystem.product.domain.Product;
import beyond.orderSystem.product.dto.ProductResDto;
import beyond.orderSystem.product.dto.ProductSaveReqDto;
import beyond.orderSystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/product/create")
//    public ResponseEntity<?> createProduct(@RequestBody ProductSaveReqDto dto){
    public ResponseEntity<?> createProduct(ProductSaveReqDto dto){
        Product product = productService.productAwsCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "products are Successly created", product);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("/product/list")
    public ResponseEntity<?> productList(Pageable pageable){
        Page<ProductResDto> dtos = productService.productList(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "product list", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
