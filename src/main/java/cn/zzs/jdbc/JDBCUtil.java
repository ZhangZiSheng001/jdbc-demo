package cn.zzs.jdbc;

import java.io.IOException;
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
	private static Object obj = new Object();
	/**
	 * 
	 * @Title: getConnection
	 * @Description: 获取数据库连接对象的方法，线程安全
	 * @author: zzs
	 * @date: 2019年8月31日 下午9:22:29
	 * @return: Connection
	 */
	public static Connection getConnection(){
		//从当前线程中获取连接对象
		Connection connection = tl.get();
		//判断为空的话，创建连接并绑定到当前线程
		if(connection == null) {
			synchronized (obj) {
				if(tl.get() == null) {
					connection = createConnection();
					tl.set(connection);
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
	 * @param conn
	 * @param statement
	 * @return: void
	 */
	public static void release(Connection conn,Statement statement,ResultSet resultSet) {
		if(resultSet!=null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				System.err.println("关闭ResultSet对象异常");
				e.printStackTrace();
			}
		}
		if(statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				System.err.println("关闭Statement对象异常");
				e.printStackTrace();
			}
		}
		//注意：这里不关闭连接
		if(conn!=null) {
			try {
				//如果连接失效的话，从当前线程的绑定中删除
				if(!conn.isValid(3)) {
					tl.remove();
				}
			} catch (SQLException e) {
				System.err.println("校验连接有效性");
				e.printStackTrace();
			}
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
	private static Connection createConnection(){ 
		//导入配置文件
		Properties pro = new Properties();
		InputStream in = JDBCUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
		//获得连接
		Connection conn = null;
		try {
			pro.load(in);
			//获取配置文件的信息
			String driver=pro.getProperty("driver");
			String url=pro.getProperty("url");
			String username=pro.getProperty("username");
			String password=pro.getProperty("password");
			//注册驱动,JDK6后不需要再手动注册，DirverManager的静态代码块会帮我们注册
			//Class.forName(driver);
			conn = DriverManager.getConnection(url,username,password);
		} catch (IOException e) {
			System.err.println("获取配置数据异常");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("创建数据库连接异常");
			e.printStackTrace();
		}
		return conn;
	}
	
}
