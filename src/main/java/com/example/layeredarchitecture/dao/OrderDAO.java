package com.example.layeredarchitecture.dao;

import com.example.layeredarchitecture.model.OrderDTO;
import com.example.layeredarchitecture.model.OrderDetailDTO;

import java.sql.SQLException;
import java.util.List;

public interface OrderDAO {
    String getCurrentOrderId() throws SQLException, ClassNotFoundException;

    boolean checkIfOrderExists(String orderId) throws SQLException, ClassNotFoundException;

    boolean placeOrder(OrderDTO orderDTO) throws SQLException, ClassNotFoundException;

    boolean saveOrder(OrderDTO orderDTO, List<OrderDetailDTO> orderDetails);
}
