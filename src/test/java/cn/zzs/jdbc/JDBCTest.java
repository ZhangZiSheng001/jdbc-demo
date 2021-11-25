package cn.zzs.jdbc;

import cn.zzs.jdbc.entity.User;
import cn.zzs.jdbc.util.CollectionUtils;
import cn.zzs.jdbc.util.IdUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 测试JDBC
 * @author zzs
 * @date 2021/5/2
 * @version 1.0.0
 */
public class JDBCTest {

    private Connection connection;

    private static final String EXIST_USER_ID = "11111111111111111111111111111111";

    @Before
    public void setup() throws IOException, SQLException {
        // 加载配置文件
        Properties pro = new Properties();
        InputStream in = JDBCTest.class.getClassLoader().getResourceAsStream("jdbc.properties");
        pro.load(in);
        // 获取配置文件的信息
        String url = pro.getProperty("url");
        String username = pro.getProperty("username");
        String password = pro.getProperty("password");
        // 注册驱动，JDK6后不需要再手动注册，DirverManager的静态代码块会帮我们注册
        // Class.forName(driver);
        // 获得连接
        connection = DriverManager.getConnection(url, username, password);
    }

    @After
    public void tearDown() throws SQLException {
        connection.close();
    }

    /**
     * 根据id获取用户
     */
    @Test
    public void getById() throws SQLException {
        User user = getById(EXIST_USER_ID);
        System.err.println(user);
    }

    private User getById(String id) throws SQLException {
        // 创建PreparedStatement对象
        String sql = "select * from demo_user where id = ?";
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        // 入参映射
        prepareStatement.setString(1, id);
        // 执行sql
        ResultSet resultSet = prepareStatement.executeQuery();
        // 出参映射
        List<User> list = mapResult(resultSet);
        // 释放资源
        resultSet.close();
        prepareStatement.close();
        return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * 出参映射
     */
    private List<User> mapResult(ResultSet resultSet) throws SQLException {
        List<User> list = new ArrayList<User>();
        while (resultSet.next()) {
            User user = new User();
            user = new User();
            user.setId(resultSet.getString(1));
            user.setName(resultSet.getString(2));
            user.setGender(resultSet.getInt(3));
            user.setAge(resultSet.getInt(4));
            user.setGmt_create(resultSet.getDate(5));
            user.setGmt_modified(resultSet.getDate(6));
            user.setDeleted(resultSet.getInt(7));
            user.setPhone(resultSet.getString(8));
            list.add(user);
        }
        return list;
    }

    /**
     * 测试添加用户
     */
    @Test
    public void save() throws SQLException {
        // 创建用户实体
        User user = new User(
                IdUtils.randomUUID(),
                "zzf001",
                0,
                18,
                new Date(),
                new Date(),
                "188******26"
        );
        // 持久化
        save(user);
    }

    private void save(User user) throws SQLException {
        // 开启事务
        connection.setAutoCommit(false);
        // 创建PreparedStatement对象
        String sql = "insert into demo_user (id,name,gender,age,gmt_create,gmt_modified,deleted,phone) values(?,?,?,?,?,?,?,?)";
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        // 入参的映射
        prepareStatement.setString(1, user.getId());
        prepareStatement.setString(2, user.getName());
        prepareStatement.setInt(3, user.getGender());
        prepareStatement.setInt(4, user.getAge());
        prepareStatement.setTimestamp(5, Timestamp.from(Instant.now()));
        prepareStatement.setTimestamp(6, Timestamp.from(Instant.now()));
        prepareStatement.setInt(7, 0);
        prepareStatement.setString(8, user.getPhone());
        // 执行sql
        int result = prepareStatement.executeUpdate();
        System.err.println(result);
        // 提交事务
        connection.commit();
        // 释放资源
        prepareStatement.close();
    }

    /**
     * 测试更新用户
     */
    @Test
    public void update() throws Exception {
        // 获取并更改用户实体
        User user = getById(EXIST_USER_ID);
        user.setAge(17);
        user.setGmt_modified(new Date());
        // 持久化
        update(user);
    }

    private void update(User user) throws SQLException {
        // 开启事务
        connection.setAutoCommit(false);
        // 创建PreparedStatement对象
        String sql = "update demo_user set name = ?,gender = ?,age = ?,gmt_modified = ?,phone = ? where id = ?";
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        // 入参的映射
        prepareStatement.setString(1, user.getName());
        prepareStatement.setInt(2, user.getGender());
        prepareStatement.setInt(3, user.getAge());
        prepareStatement.setTimestamp(4, Timestamp.from(Instant.now()));
        prepareStatement.setString(5, user.getPhone());
        prepareStatement.setString(6, user.getId());
        // 执行sql
        int result = prepareStatement.executeUpdate();
        System.err.println(result);
        // 提交事务
        connection.commit();
        // 释放资源
        prepareStatement.close();
    }

    /**
     * 测试查找所有用户
     */
    @Test
    public void find() throws SQLException {
        List<User> list = findAll();
        list.stream().forEach(System.err::println);
    }


    private List<User> findAll() throws SQLException {
        // 创建PreparedStatement对象
        String sql = "select * from demo_user where deleted = 0";
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        // 执行sql
        ResultSet resultSet = prepareStatement.executeQuery();
        // 出参的映射
        List<User> list = mapResult(resultSet);
        // 释放资源
        resultSet.close();
        prepareStatement.close();
        return list;
    }

    /**
     * 测试删除用户
     */
    @Test
    public void delete() throws SQLException {
        //持久化
        delete(EXIST_USER_ID);
    }


    private void delete(String id) throws SQLException {
        // 开启事务
        connection.setAutoCommit(false);
        // 创建PreparedStatement对象
        String sql = "delete from demo_user where id = ?";
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        // 入参的映射
        prepareStatement.setString(1, id);
        // 执行sql
        prepareStatement.executeUpdate();
        // 提交事务
        // connection.commit();
        // 回滚事务
        connection.rollback();
        // 释放资源
        prepareStatement.close();
    }

    /**
     * 测试通过结果集来操作数据库
     */
    // @Test
    public void testResultSet() throws Exception {
        String sql = "select * from demo_user where deleted = 0 order by name desc";
        /**
         ResultSet的几个属性
         结果集不能滚动(默认值)
         int TYPE_FORWARD_ONLY = 1003;
         结果集可以滚动，但对数据库变化不敏感
         int TYPE_SCROLL_INSENSITIVE = 1004;
         结果集可以滚动，且对数据库变化敏感
         int TYPE_SCROLL_SENSITIVE = 1005;
         结果集不能用于更新数据库(默认值)
         int CONCUR_READ_ONLY = 1007;
         结果集可以用于更新数据库
         int CONCUR_UPDATABLE = 1008;
         */
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        // 执行查询并返回结果集
        ResultSet rs = statement.executeQuery(sql);
        // 测试插入数据
        rs.moveToInsertRow();// 把游标移动到插入行，默认在最后一行。
        rs.updateString(1, IdUtils.randomUUID());
        rs.updateString(2, "李四");
        rs.updateInt(3, 0);
        rs.updateInt(4, 26);
        rs.updateTimestamp(5, Timestamp.from(Instant.now()));
        rs.updateTimestamp(6, Timestamp.from(Instant.now()));
        rs.updateInt(7, 0);
        rs.updateString(8, "188******49");
        rs.insertRow();// 插入数据
        rs.moveToCurrentRow();// 把游标移动到新插入数据所在行
        // 测试删除数据
        // rs.absolute(2);// 移动游标到第二行
        // rs.deleteRow();// 删除第二行数据
        // 遍历所有数据
        while (rs.next()) {
            // 测试更新数据
            rs.updateTimestamp(6, Timestamp.from(Instant.now()));
            rs.updateRow();
        }
        statement.close();
    }
    
    @Test
    public void getMetaData() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables("github_demo", "%", "%", new String[] { "TABLE" });
        
        while (rs.next()) {
            System.err.println(rs.getString("TABLE_NAME"));
            System.err.println(rs.getString("REMARKS"));
        }
    }

}
