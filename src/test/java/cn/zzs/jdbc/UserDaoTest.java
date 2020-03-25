package cn.zzs.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import cn.zzs.jdbc.dao.UserDao;
import cn.zzs.jdbc.dao.impl.UserDaoImpl;
import cn.zzs.jdbc.entity.User;
import cn.zzs.jdbc.util.JDBCUtil;

/**
 * <p>测试JDBC</p>
 * @author: zzs
 * @date: 2019年8月31日 下午9:39:54
 */
public class UserDaoTest {

	/**
	 * <p>测试添加用户</p>
	 * @throws Exception 
	 */
	@Test
	public void save() throws Exception {
		UserDao userDao = new UserDaoImpl();
		// 创建用户
		User user = new User("zzf002", 18, new Date(), new Date());
		try (Connection connection = JDBCUtil.getConnection()) {
			// 开启事务
			connection.setAutoCommit(false);
			// 保存用户
			userDao.insert(user);
			// 提交事务
			connection.commit();
		}
	}

	/**
	 * <p>测试更新用户</p>
	 */
	@Test
	public void update() throws Exception {
		UserDao userDao = new UserDaoImpl();
		// 根据name获取用户并修改
		User user = userDao.selectByName("zzf002");
		if(user == null) {
			System.out.println("该用户不存在");
			return;
		}
		user.setAge(17);
		user.setGmt_modified(new Date());
		try (Connection connection = JDBCUtil.getConnection()) {
			// 开启事务
			connection.setAutoCommit(false);
			// 更新用户
			userDao.update(user);
			// 提交事务
			connection.commit();
		}
	}

	/**
	 * <p>测试查找所有用户</p>
	 */
	@Test
	public void find() throws Exception {
		UserDao userDao = new UserDaoImpl();
		try (Connection connection = JDBCUtil.getConnection()) {
			// 查询所有用户并遍历
			List<User> list = userDao.selectAll();
			if(list != null && list.size() != 0) {
				for(User user : list) {
					System.out.println(user);
				}
			}
		}
	}

	/**
	 * <p>测试删除用户</p>
	 */
	@Test
	public void delete() throws Exception {
		UserDao userDao = new UserDaoImpl();
		try (Connection connection = JDBCUtil.getConnection()) {
			// 开启事务
			connection.setAutoCommit(false);
			// 更新用户
			userDao.delete(1L);
			// 提交事务
			connection.commit();
		}
	}

	/**
	 * 
	 * <p>测试通过改变结果集来操作数据库，操作前预备了三条数据</p>
	 */
	@Test
	public void testResultSet() throws Exception {
		Connection conn = null;
		Statement statement = null;
		String sql = "select * from demo_user where id <= 15";
		// 使用工具类获得连接
		try (Connection connection = JDBCUtil.getConnection()) {

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
			// 第一个参数设置是否可以滚动，第二个参数设置是否可更新
			statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			// 执行查询并返回结果集
			ResultSet rs = statement.executeQuery(sql);
			/**
			可滚动的几个方法
			rs.previous();
			rs.next();
			rs.getRow();
			rs.absolute(0);
			 */
			// 测试插入数据
			rs.moveToInsertRow();// 把游标移动到插入行，默认在最后一行。
			rs.updateString(2, "李四");
			rs.updateInt(3, 26);
			rs.updateDate(4, new java.sql.Date(System.currentTimeMillis()));
			rs.updateDate(5, new java.sql.Date(System.currentTimeMillis()));
			rs.insertRow();// 插入数据
			rs.moveToCurrentRow();// 把游标移动最后一个位置
			// 测试删除数据
			rs.absolute(2);// 移动游标到第二行
			rs.deleteRow();// 删除第二行数据
			// 遍历所有数据
			while(rs.next()) {
				// 测试更新数据
				rs.updateDate(5, new java.sql.Date(System.currentTimeMillis()));
				rs.updateRow();
			}
		}
	}

}
