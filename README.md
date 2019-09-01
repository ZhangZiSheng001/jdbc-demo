
# JDBC

## 简介   
JDBC用于连接和操作数据库，可以看成是一套标准。通过提供Driver、Connection、Statement、PreparedStatement、PrepareCall、ResultSet等接口将开发人员与数据库提供商隔离，开发人员只需要面对jdbc接口，无需关心怎么跟数据库交互。  

jdbc底层与数据库的交互是通过socket的IO流方式，所以这方面存在一定的消耗，因此在程序设计中应当考虑缓存等方式来减少数据库连接。  

在mysql的对应实现类中，Connection、Statement和ResultSet里都包含了对soket的io流对象的引用，所以都可以和数据库通讯。其中Connection主要可以管理事务，Statement用于指定具体的DML、DDL和DQL语句、prepareCall(String)用于调用存储函数、ResultSet用于存储和操作结果集。  

注意，记得关闭io流，避免造成资源浪费。ResultSet和Statement的关闭都不会导致Connection或者Socket的IO流关闭。另外，maven要引入oracle的驱动包，要把jar包安装在本地仓库或私服才行。
## 使用例子
### 需求
使用JDBC对mysql的用户表进行增删改查。

### 工程环境
JDK：1.8.0_201  
maven：3.6.1  
IDE：Spring Tool Suites4 for Eclipse  
mysql驱动：8.0.15
mysql：5.7


### 主要步骤
1. 注册驱动（JDK6后会自动注册，所以该步骤可以取消）;
2. 通过`DriverManager`获得与数据库的连接`Connection`对象;
3. 通过`Connection`获得`PreparedStatement`对象，可以看成一个语句对象;
4. 设置`PreparedStatement`的参数，并执行增删改查;
5. 查询的话还得获得结果集`ResultSet`，获得结果集的内容;
6. 释放资源，包括`Connection`、`PreparedStatement`、`ResultSet`。


### 创建表
```sql
CREATE DATABASE `demo`CHARACTER SET utf8 COLLATE utf8_bin;
User `demo`;
CREATE TABLE `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '用户名',
  `age` int(10) unsigned DEFAULT NULL COMMENT '用户年龄',
  `gmt_create` datetime DEFAULT NULL COMMENT '记录创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '记录最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
```

### 创建项目
项目类型Maven Project，打包方式jar

### 引入依赖
```xml
<!-- junit -->
<dependency>
	<groupId>junit</groupId>
	<artifactId>junit</artifactId>
	<version>4.12</version>
	<scope>test</scope>
</dependency>
<!-- mysql驱动的jar包 -->
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>8.0.15</version>
</dependency>
<!-- oracle驱动的jar包 -->
<!-- <dependency>
	<groupId>com.oracle</groupId>
	<artifactId>ojdbc6</artifactId>
	<version>11.2.0.2.0</version>
</dependency> -->
```
*注意：由于oracle商业版权问题，maven并不提供`Oracle JDBC driver`，需要将驱动包手动添加到本地仓库。不懂请留言*

### 编写jdbc.prperties
路径：resources目录下  
```properties
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=true
#这里指定了字符编码和解码格式，时区，是否加密传输
username=root
password=root
#注意，xml配置的话，&采用&amp;替代
```
如果是oracle数据库，配置如下：
```properties
driver=oracle.jdbc.driver.OracleDriver
url=jdbc:oracle:thin:@//localhost:1521/xe
username=system
password=root
```
### 编写JDBCUtil用于获得连接对象
这里设置工具类的目的是避免多个线程使用同一个连接对象，并提供了释放资源的方法（注意，考虑到重用性，这里并不会关闭连接）。  
路径：`cn.zzs.jdbc`
```java
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
```
### 编写测试类
路径：test目录下的`cn.zzs.jdbc`

#### 添加用户
注意：这里引入了事务
```java
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
```
#### 更新用户
```java
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
```
#### 查询用户
```java
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
```
#### 删除用户
```java
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
```
#### 补充ResultSet的测试
```java
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
```
> 学习使我快乐！！
