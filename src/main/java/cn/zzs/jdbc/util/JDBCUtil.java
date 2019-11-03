package cn.zzs.jdbc.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @ClassName: JDBCUtil
 * @Description: 用于获取数据库连接对象的工具类
 * @author: zzs
 * @date: 2019年8月31日 下午9:05:08
 */
public class JDBCUtil {

	private static ThreadLocal<Connection> tl = new ThreadLocal<>();

	private static Object lock = new Object();

	/**
	 * 
	 * @Title: getConnection
	 * @Description: 获取数据库连接对象的方法，线程安全
	 * @author: zzs
	 * @date: 2019年8月31日 下午9:22:29
	 * @return: Connection
	 */
	public static Connection getConnection() throws Exception {
		// 从当前线程中获取连接对象
		Connection connection = tl.get();
		// 判断为空的话，创建连接并绑定到当前线程
		if( connection == null ) {
			synchronized( lock ) {
				if( tl.get() == null ) {
					connection = createConnection();
					tl.set( connection );
				}
			}
		}
		return connection;
	}

	/**
	 * 
	 * @Title: release
	 * @Description: 释放资源
	 * @author: zzs
	 * @date: 2019年8月31日 下午9:39:24
	 * @param statement
	 * @param resultSet
	 * @return: void
	 */
	public static void release( Statement statement, ResultSet resultSet ) {
		if( resultSet != null ) {
			try {
				resultSet.close();
			} catch( SQLException e ) {
				e.printStackTrace();
			}
		}
		if( statement != null ) {
			try {
				statement.close();
			} catch( SQLException e ) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @Title: release
	 * @Description: 释放资源
	 * @author: zzs
	 * @date: 2019年11月3日 上午11:26:26
	 * @return: void
	 */
	public static void release() {
		try {
			getConnection().close();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Title: startTrasaction
	 * @Description: 开启事务
	 * @author: zzs
	 * @date: 2019年11月3日 上午11:03:24
	 * @return: void
	 * @throws Exception 
	 * @throws SQLException 
	 */
	public static void startTrasaction() throws SQLException, Exception {
		getConnection().setAutoCommit( false );
	}

	/**
	 * 
	 * @Title: commit
	 * @Description: 提交事务
	 * @author: zzs
	 * @date: 2019年11月3日 上午11:05:54
	 * @return: void
	 */
	public static void commit() {
		Connection connection = tl.get();
		try {
			if(connection != null && !connection.getAutoCommit()) {
				connection.commit();
				connection.setAutoCommit( true );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Title: rollback
	 * @Description: 回滚事务
	 * @author: zzs
	 * @date: 2019年11月3日 上午11:08:12
	 * @return: void
	 */
	public static void rollback() {
		Connection connection = tl.get();
		try {
			if(connection != null && !connection.getAutoCommit()) {
				connection.rollback();
				connection.setAutoCommit( true );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Title: createConnection
	 * @Description: 创建数据库连接
	 * @author: zzs
	 * @date: 2019年8月31日 下午9:27:03
	 * @return: Connection
	 */
	private static Connection createConnection() throws Exception {
		// 导入配置文件
		Properties pro = new Properties();
		InputStream in = JDBCUtil.class.getClassLoader().getResourceAsStream( "jdbc.properties" );
		Connection conn = null;
		pro.load( in );
		// 获取配置文件的信息
		String driver = pro.getProperty( "driver" );
		String url = pro.getProperty( "url" );
		String username = pro.getProperty( "username" );
		String password = pro.getProperty( "password" );
		// 注册驱动,JDK6后不需要再手动注册，DirverManager的静态代码块会帮我们注册
		// Class.forName(driver);
		// 获得连接
		conn = DriverManager.getConnection( url, username, password );
		return conn;
	}

}
