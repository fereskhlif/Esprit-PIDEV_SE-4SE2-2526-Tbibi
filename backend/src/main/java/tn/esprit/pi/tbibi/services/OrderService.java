package tn.esprit.pi.tbibi.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.tbibi.DTO.order.OrderRequest;
import tn.esprit.pi.tbibi.DTO.order.OrderResponse;
import tn.esprit.pi.tbibi.DTO.orderline.OrderLineRequest;
import tn.esprit.pi.tbibi.DTO.orderline.OrderLineResponse;
import tn.esprit.pi.tbibi.mappers.OrderLineMapper;
import tn.esprit.pi.tbibi.mappers.OrderMapper;
import tn.esprit.pi.tbibi.entities.*;
import tn.esprit.pi.tbibi.repositories.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService implements IOrderService {

    OrderRepository orderRepo;
    PharmacyRepository pharmacyRepo;
    UserRepo userRepo;
    OrderLineRepository orderLineRepo;
    OrderMapper orderMapper;
    MedicineRepository medicineRepo;
    OrderLineMapper orderLineMapper;

    // ─── Helper: maps an Order and loads its lines with medicine name ──────────
    private OrderResponse mapWithLines(Order order) {
        OrderResponse dto = orderMapper.toDto(order);
        List<OrderLineResponse> lines = orderLineRepo
                .findByOrderIdWithMedicine(order.getOrderId())
                .stream()
                .map(orderLineMapper::toDto)
                .toList();
        dto.setOrderLines(lines);
        return dto;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Pharmacy pharmacy = pharmacyRepo.findById(request.getPharmacyId()).orElseThrow();
        User user = userRepo.findById(request.getUserId()).orElseThrow();

        Order order = new Order();
        order.setPharmacy(pharmacy);
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setOrderStatus(Status.PENDING);

        List<OrderLine> orderLines = new ArrayList<>();
        float totalAmount = 0;

        for (OrderLineRequest lineRequest : request.getOrderLines()) {
            Medicine medicine = medicineRepo.findById(lineRequest.getMedicineId()).orElseThrow();

            if (medicine.getStock() < lineRequest.getQuantity()) {
                throw new RuntimeException("Not enough stock for: " + medicine.getMedicineName());
            }

            OrderLine orderLine = new OrderLine();
            orderLine.setMedicine(medicine);
            orderLine.setQuantity(lineRequest.getQuantity());
            orderLine.setUnitPrice(medicine.getPrice());
            orderLine.setOrder(order);
            orderLines.add(orderLine);

            totalAmount += medicine.getPrice() * lineRequest.getQuantity();

            medicine.setStock(medicine.getStock() - lineRequest.getQuantity());
            medicineRepo.save(medicine);
        }

        order.setOrderLines(orderLines);
        order.setTotalAmount(totalAmount);
        Order saved = orderRepo.save(order);
        return mapWithLines(saved);
    }

    @Override
    @Transactional
    public OrderResponse getOrderById(Long id) {
        return mapWithLines(orderRepo.findById(id).orElseThrow());
    }

    @Override
    @Transactional
    public List<OrderResponse> getAllOrders() {
        return orderRepo.findAll().stream()
                .map(this::mapWithLines)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepo.findById(id).orElseThrow();
        order.setOrderStatus(Status.valueOf(status));

        if (Status.valueOf(status) == Status.DELIVERED) {
            order.setDeliveryDate(new Date());
        }

        if (Status.valueOf(status) == Status.REJECTED ||
                Status.valueOf(status) == Status.CANCELLED) {
            for (OrderLine line : order.getOrderLines()) {
                Medicine medicine = line.getMedicine();
                medicine.setStock(medicine.getStock() + line.getQuantity());
                medicineRepo.save(medicine);
            }
        }

        return mapWithLines(orderRepo.save(order));
    }

    @Override
    @Transactional
    public List<OrderResponse> getOrdersByUser(Integer userId) {
        return orderRepo.findByUser_UserId(userId)
                .stream()
                .map(this::mapWithLines)
                .toList();
    }

    @Override
    @Transactional
    public List<OrderResponse> getPendingOrders() {
        return orderRepo.findByOrderStatus(Status.PENDING)
                .stream()
                .map(this::mapWithLines)
                .toList();
    }

    @Override
    @Transactional
    public List<OrderResponse> getOrdersByPharmacy(Long pharmacyId) {
        return orderRepo.findByPharmacy_PharmacyId(pharmacyId)
                .stream()
                .map(this::mapWithLines)
                .toList();
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        orderRepo.deleteById(id);
    }
}