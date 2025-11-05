package vn.cineshow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.cineshow.dto.request.order.OrderCreatedAtSearchRequest;
import vn.cineshow.dto.request.order.OrderListRequest;
import vn.cineshow.model.Order;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.OrderQueryService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderQueryService orderQueryService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== GET /api/orders ====================
    @Test
    @DisplayName("GET /api/orders should return orders list successfully")
    void listAll_shouldReturnOrdersList() throws Exception {
        List<Order> orders = new ArrayList<>();
        Order order = new Order();
        order.setId(1L);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(100000.0);
        orders.add(order);

        Page<Order> page = new PageImpl<>(orders, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

        when(orderRepository.findAllBy(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(orderRepository, times(1)).findAllBy(any(org.springframework.data.domain.Pageable.class));
    }

    // ==================== POST /api/orders/search ====================
    @Test
    @DisplayName("POST /api/orders/search should search orders successfully")
    void search_shouldSearchOrdersSuccessfully() throws Exception {
        OrderListRequest request = new OrderListRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSort(List.of("desc"));

        List<Order> orders = new ArrayList<>();
        Order order = new Order();
        order.setId(1L);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(100000.0);
        orders.add(order);

        Page<Order> page = new PageImpl<>(orders, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

        when(orderRepository.findAllBy(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(post("/api/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(orderRepository, times(1)).findAllBy(any(org.springframework.data.domain.Pageable.class));
    }

    // ==================== POST /api/orders/search-by-date ====================
    @Test
    @DisplayName("POST /api/orders/search-by-date should search orders by date successfully")
    void searchByCreated_shouldSearchByDateSuccessfully() throws Exception {
        OrderCreatedAtSearchRequest request = new OrderCreatedAtSearchRequest();
        request.setDate(LocalDate.of(2025, 1, 1));
        request.setPage(0);
        request.setSize(10);
        request.setSort(List.of("desc"));

        List<Order> orders = new ArrayList<>();
        Order order = new Order();
        order.setId(1L);
        order.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        order.setTotalPrice(100000.0);
        orders.add(order);

        Page<Order> page = new PageImpl<>(orders, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(post("/api/orders/search-by-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0));

        verify(orderRepository, times(1)).findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("POST /api/orders/search-by-date should return 400 when date is missing")
    void searchByCreated_missingDate_shouldReturnBadRequest() throws Exception {
        OrderCreatedAtSearchRequest request = new OrderCreatedAtSearchRequest();
        request.setDate(null);
        request.setPage(0);
        request.setSize(10);

        mockMvc.perform(post("/api/orders/search-by-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderRepository, never()).findByCreatedAtBetween(any(), any(), any());
    }
}

