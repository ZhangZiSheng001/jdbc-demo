package cn.zzs.jdbc.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * 用于获取数据库连接对象的工具类
 * @author: zzs
 * @date: 2019年8月31日 下午9:05:08
 */
public class JDBCUtil {

    private static ThreadLocal<Connection> tl = new ThreadLocal<>();

    private static Object lock = new Object();

    /**
     * 
     * <p>获取数据库连接对象的方法，线程安全</p>
     * @author: zzs
     * @date: 2019年8月31日 下午9:22:29
     * @return: Connection
     */
    public static Connection getConnection() throws Exception {
        // 从当前线程中获取连接对象
        Connection connection = tl.get();
        // 判断为空的话，创建连接并绑定到当前线程
        if(connection == null) {
            synchronized(lock) {
                if((connection = tl.get()) == null) {
                    connection = createConnection();
                    tl.set(connection);
                }
            }
        }
        return connection;
    }

    /**
     * 
     * <p>释放资源</p>
     * @author: zzs
     * @date: 2019年8月31日 下午9:39:24
     * @param statement
     * @param resultSet
     * @return: void
     */
    public static void release(Statement statement, ResultSet resultSet, Connection connection) {
        if(resultSet != null) {
            try {
                resultSet.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        if(statement != null) {
            try {
                statement.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }

        if(connection != null) {
            try {
                connection.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     *  <p>创建数据库连接</p>
     * @author: zzs
     * @date: 2019年8月31日 下午9:27:03
     * @return: Connection
     */
    private static Connection createConnection() throws Exception {
        // 导入配置文件
        Properties pro = new Properties();
        InputStream in = JDBCUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
        Connection conn = null;
        pro.load(in);
        // 获取配置文件的信息
        String url = pro.getProperty("url");
        String username = pro.getProperty("username");
        String password = pro.getProperty("password");
        // 注册驱动,JDK6后不需要再手动注册，DirverManager的静态代码块会帮我们注册
        // Class.forName(driver);
        // 获得连接
        conn = DriverManager.getConnection(url, username, password);
        return conn;
    }

}
