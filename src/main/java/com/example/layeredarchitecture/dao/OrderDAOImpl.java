package com.example.layeredarchitecture.dao;

import com.example.layeredarchitecture.db.DBConnection;
import com.example.layeredarchitecture.model.ItemDTO;
import com.example.layeredarchitecture.model.OrderDTO;
import com.example.layeredarchitecture.model.OrderDetailDTO;

import java.sql.*;
import java.util.List;

public class OrderDAOImpl implements OrderDAO{
    ItemDAO itemDAO = new ItemDAOImpl();
    OrderDetailsDAO orderDetailsDAO = new OrderDetailsDAOImpl();

    @Override
    public String getCurrentOrderId() throws SQLException, ClassNotFoundException {
        Connection connection = DBConnection.getDbConnection().getConnection();
        Statement stm = connection.createStatement();
        ResultSet rst = stm.executeQuery("SELECT oid FROM `Orders` ORDER BY oid DESC LIMIT 1;");
        if (rst.next()) {
            return rst.getString(1);
        }
        return null;
    }

    @Override
    public boolean checkIfOrderExists(String orderId) throws SQLException, ClassNotFoundException {
        Connection connection = DBConnection.getDbConnection().getConnection();
        PreparedStatement stm = connection.prepareStatement("SELECT oid FROM `Orders` WHERE oid=?");
        stm.setString(1, orderId);
        return stm.executeQuery().next();
    }

    @Override
    public boolean placeOrder(OrderDTO orderDTO) throws SQLException, ClassNotFoundException {
        Connection connection = DBConnection.getDbConnection().getConnection();
        PreparedStatement stm = connection.prepareStatement("INSERT INTO `Orders` (oid, date, customerID) VALUES (?,?,?)");
        stm.setString(1, orderDTO.getOrderId());
        stm.setDate(2, Date.valueOf(orderDTO.getOrderDate()));
        stm.setString(3, orderDTO.getCustomerId());
        return stm.executeUpdate() != 1;
    }

    @Override
    public boolean saveOrder(OrderDTO orderDTO, List<OrderDetailDTO> orderDetails) {
        try {
            Connection connection = DBConnection.getDbConnection().getConnection();
            /*if order id already exist*/
            if (checkIfOrderExists(orderDTO.getOrderId())) {

            }

            connection.setAutoCommit(false);

            if (placeOrder(orderDTO)) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }

            for (OrderDetailDTO detail : orderDetails) {
                OrderDetailDTO orderDetailDTO = new OrderDetailDTO(detail.getItemCode(), detail.getQty(), detail.getUnitPrice());
                if (orderDetailsDAO.saveOrderDetail(orderDTO.getOrderId(),orderDetailDTO)) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }

//                //Search & Update Item
                ItemDTO item = itemDAO.findItem(detail.getItemCode());
                item.setQtyOnHand(item.getQtyOnHand() - detail.getQty());

                ItemDTO itemDTO = new ItemDTO(item.getCode(), item.getDescription(), item.getUnitPrice(), item.getQtyOnHand());
                if (!(itemDAO.updateQty(itemDTO))) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
            return true;

        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}
