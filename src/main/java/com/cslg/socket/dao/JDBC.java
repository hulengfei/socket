package com.cslg.socket.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBC {

    private static final Logger logger = LoggerFactory.getLogger(JDBC.class);

    private static final String URL = "";

    private static final String DRIVER = "com.mysql.jdbc.Driver";

    private static final String USERNAME = "";

    private static final String PASSWORD = "";

    public static Connection getConnect() {
        Connection connection = null;
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            logger.error("获取connection出错", e);
        }
        return connection;
    }

    public static void close(Connection connection) {
        try {
            if(connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.getErrorCode();
            logger.error("关闭connection出现异常", e);
        }
    }
}
