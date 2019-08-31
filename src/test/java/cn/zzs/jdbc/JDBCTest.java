package cn.zzs.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

/**
 * @ClassName: JDBCTest
 * @Description: 测试JDBC
 * @author: zzs
 * @date: 2019年8月31日 下午9:39:54
 */
public class JDBCTest {
	/**
	 * 测试添加用户
	 * @throws SQLException 
	 */
	@Test
	public void saveUser() throws Exception {
		//创建sql
		String sql = "insert into user values(null,?,?,?,?)";
		//获得连接
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement statement = null;
		try {
			//设置非自动提交
			connection.setAutoCommit(false);
			//获得Statement对象
			statement = connection.prepareStatement(sql);
			//设置参数
			statement.setString(1, "张三");
			statement.setInt(2, 18);
			statement.setDate(3, new Date(System.currentTimeMillis()));
			statement.setDate(4, new Date(System.currentTimeMillis()));
			//执行
			statement.executeUpdate();
			//提交事务
			connection.commit();
		} catch (Exception e) {
			System.out.println("异常导致操作回滚");
			connection.rollback();
			e.printStackTrace();
		} finally {
			//释放资源
			JDBCUtil.release(connection, statement,null);
		}
	}

	/**
	 * 测试更新用户
	 */
	@Test
	public void updateUser() throws Exception {
		//创建sql
		String sql = "update user set age = ?,gmt_modified = ? where id = ?";
		//获得连接
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement statement = null;
		try {
			//设置非自动提交
			connection.setAutoCommit(false);
			//获得Statement对象
			statement = connection.prepareStatement(sql);
			//设置参数
			statement.setInt(1, 19);
			statement.setDate(2, new Date(System.currentTimeMillis()));
			statement.setInt(3, 1);
			//执行
			statement.executeUpdate();
			//提交事务
			connection.commit();
		} catch (Exception e) {
			System.out.println("异常导致操作回滚");
			connection.rollback();
			e.printStackTrace();
		} finally {
			//释放资源
			JDBCUtil.release(connection, statement,null);
		}
	}

	/**
	 * 测试查找用户
	 */
	@Test
	public void findUser() throws Exception {
		//创建sql
		String sql = "select * from user where id = ?";
		//获得连接
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			//获得Statement对象
			statement = connection.prepareStatement(sql);
			//设置参数
			statement.setInt(1, 1);
			//执行
			resultSet = statement.executeQuery();
			//遍历结果集
			while (resultSet.next()) {
				String name = resultSet.getString(2);
				int age = resultSet.getInt(3);
				System.out.println("用户名：" + name + ",年龄：" + age);
			}
		} finally {
			//释放资源
			JDBCUtil.release(connection, statement,resultSet);
		}
	}

	/**
	 * 测试删除用户
	 */
	@Test
	public void deleteUser() throws Exception {
		//创建sql
		String sql = "delete from user where id = ?";
		//获得连接
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement statement = null;
		try {
			//设置非自动提交
			connection.setAutoCommit(false);
			//获得Statement对象
			statement = connection.prepareStatement(sql);
			//设置参数
			statement.setInt(1, 1);
			//执行
			statement.executeUpdate();
			//提交事务
			connection.commit();
		} catch (Exception e) {
			System.out.println("异常导致操作回滚");
			connection.rollback();
			e.printStackTrace();
		} finally {
			//释放资源
			JDBCUtil.release(connection, statement,null);
		}
	}
	/**
	 * 
	 * @Title: testResultSet
	 * @Description: 测试通过改变结果集来操作数据库，操作前预备了三条数据
	 * @author: zzs
	 * @date: 2019年8月31日 下午10:10:53
	 * @throws Exception
	 * @return: void
	 */
	@Test
	public void testResultSet() throws Exception {
		Connection conn = null;
		Statement statement = null;
		//定义查询语句
		String sql = "select * from user where id<=15";
		//使用工具类获得连接
		conn = JDBCUtil.getConnection();
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
		*/
		//第一个参数设置是否可以滚动，第二个参数设置是否可更新
		statement=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		//执行查询并返回结果集
		ResultSet rs = statement.executeQuery(sql);
		/**
		可滚动的几个方法
	    rs.previous();
	    rs.next();
	    rs.getRow();
	    rs.absolute(0);
		 */
		//测试插入数据
		rs.moveToInsertRow();//把游标移动到插入行，默认在最后一行。
		rs.updateString(2, "李四");
		rs.updateInt(3, 26);
		rs.updateDate(4, new Date(System.currentTimeMillis()));
		rs.updateDate(5, new Date(System.currentTimeMillis()));
		rs.insertRow();//插入数据
		rs.moveToCurrentRow();//把游标移动最后一个位置
		//测试删除数据
		rs.absolute(2);//移动游标到第二行
		rs.deleteRow();//删除第二行数据
		//遍历所有数据
		while (rs.next()) {
			//测试更新数据
			rs.updateDate(5, new Date(System.currentTimeMillis()));
			rs.updateRow();
		}
		//释放资源
		JDBCUtil.release(conn, statement, rs);
	}

}
