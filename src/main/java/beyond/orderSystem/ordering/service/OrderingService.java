package beyond.orderSystem.ordering.service;

import beyond.orderSystem.member.domain.Member;
import beyond.orderSystem.member.repository.MemberRepository;
import beyond.orderSystem.ordering.domain.OrderDetail;
import beyond.orderSystem.ordering.domain.OrderStatus;
import beyond.orderSystem.ordering.domain.Ordering;
import beyond.orderSystem.ordering.dto.OrderListResDto;
import beyond.orderSystem.ordering.dto.OrderSaveReqDto;
import beyond.orderSystem.ordering.repository.OrderDetailRepository;
import beyond.orderSystem.ordering.repository.OrderingRepository;
import beyond.orderSystem.product.domain.Product;
import beyond.orderSystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;


    @Autowired
    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository,
                           ProductRepository productRepository, OrderDetailRepository orderDetailRepository){
        this.productRepository = productRepository;
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.orderDetailRepository = orderDetailRepository;
    }


//    방법1. JPA에 최적화된 방식
    public Ordering orderCreate(List<OrderSaveReqDto> dto){
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(
//                ()-> new EntityNotFoundException("member is not found"));
        Member member = memberRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(
                        ()-> new EntityNotFoundException("member is not found"));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

//        for(OrderSaveReqDto.OrderDetailDto orderDetailDto : dto.getOrderDtos()){
        for(OrderSaveReqDto orders : dto){
//            Product product = productRepository.findById(orderDetailDto.getProductId()).orElseThrow(()-> new EntityNotFoundException("product is not found"));
//            int quantity = orderDetailDto.getProductCount();
            Product product = productRepository.findById(orders.getProductId()).orElseThrow(
                    ()-> new EntityNotFoundException("product is not found"));
            int quantity = orders.getProductCount();
            if(quantity > product.getStockQuantity()){
                throw new IllegalArgumentException(product.getName() + "의 재고가 부족합니다. 현재 재고: " + product.getStockQuantity());
            }
            product.updateQuantity(quantity); // 변경감지(dirty checking)로 인해 별도의 save 불필요

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .ordering(ordering)
                    .quantity(quantity)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }

        Ordering savedOrdering = orderingRepository.save(ordering);
        return savedOrdering;
    }

//    public Ordering orderCreate(OrderSaveReqDto dto){
////        방법2: 쉬운 방식 : 레포가 따로 필요함
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원"));
//        Ordering ordering = orderingRepository.save(dto.toEntity(member));
//
////        OrderDetail 생성: order_id, product_id, quantity
//        for(OrderSaveReqDto.OrderDetailDto orderDto : dto.getOrderDtos()){
//            Product product = productRepository.findById(orderDto.getProductId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 상품"));
//            int quantity = orderDto.getProductCount();
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            orderDetailRepository.save(orderDetail);
//        }
//        return ordering;
//    }
//    public Page<OrderResDto> orderList(Pageable pageable) {
//        Page<Ordering> orders = orderingRepository.findAll(pageable);
//        return orders.map(Ordering::fromEntity);
//    }

    public List<OrderListResDto> orderList(){
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering ordering : orderings){
            orderListResDtos.add(ordering.fromEntity());
        }
        return orderListResDtos;
    }

    public List<OrderListResDto> myOrderList(){
        Member member = memberRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(
                        ()-> new EntityNotFoundException("존재하지 않는 유저"));
//        List<Ordering> orderings = orderingRepository.findAll();
        List<Ordering> orderings = orderingRepository.findAllByMember(member);
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
//        for(Ordering ordering : orderings){
//            if(ordering.getMember().getEmail().equals(member.getEmail())){
//                orderListResDtos.add(ordering.fromEntity());
//            }
//        }
        for(Ordering ordering : orderings){
            orderListResDtos.add(ordering.fromEntity());
        }
        return orderListResDtos;
    }

    public void orderCancel(Long orderId){
        Member member = memberRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(
                ()-> new EntityNotFoundException("존재하지 않는 유저"));
        Ordering ordering = orderingRepository.findById(orderId).orElseThrow(
                ()-> new EntityNotFoundException("존재하지 않는 주문"));
        if(!ordering.getMember().equals(member)) throw new SecurityException("접근 권한이 없습니다");
        else if(ordering.getOrderStatus().equals(OrderStatus.CANCELED)) throw new IllegalArgumentException ("이미 취소된 주문입니다");
        else ordering.orderCancel();
    }

    public void orderCancelByAdmin(Long orderId){
        Ordering ordering = orderingRepository.findById(orderId).orElseThrow(
                ()-> new EntityNotFoundException("존재하지 않는 주문"));
        if(ordering.getOrderStatus().equals(OrderStatus.CANCELED)) throw new IllegalArgumentException ("이미 취소된 주문입니다");
        ordering.orderCancel();
    }
}
