# Table of Contents

* [简介](#简介)
  * [什么是JDBC](#什么是jdbc)
  * [几个重要的类](#几个重要的类)
  * [使用中的注意事项](#使用中的注意事项)
* [使用例子](#使用例子)
  * [需求](#需求)
  * [工程环境](#工程环境)
  * [主要步骤](#主要步骤)
  * [创建表](#创建表)
  * [创建项目](#创建项目)
  * [引入依赖](#引入依赖)
  * [编写jdbc.prperties](#编写jdbcprperties)
  * [获得Connection对象](#获得connection对象)
  * [使用Connection对象完成保存操作](#使用connection对象完成保存操作)
* [源码分析](#源码分析)
  * [驱动注册](#驱动注册)
    * [DriverManager](#drivermanager)
    * [为什么Class.forName(com.mysql.cj.jdbc.Driver) 可以注册驱动？](#为什么classfornamecommysqlcjjdbcdriver-可以注册驱动)
    * [为什么JDK6后不需要Class.forName也能注册驱动？](#为什么jdk6后不需要classforname也能注册驱动)
  * [获得连接对象](#获得连接对象)
    * [DriverManager.getConnection](#drivermanagergetconnection)
    * [com.mysql.cj.jdbc.Driver.connection](#commysqlcjjdbcdriverconnection)
    * [ConnectionImpl.getInstance](#connectionimplgetinstance)
    * [NativeSession.connect](#nativesessionconnect)


# 简介   
## 什么是JDBC  

JDBC是一套连接和操作数据库的标准、规范。通过提供`DriverManager`、`Connection`、`Statement`、`ResultSet`等接口将开发人员与数据库提供商隔离，开发人员只需要面对JDBC接口，无需关心怎么跟数据库交互。  

## 几个重要的类
类名 | 作用  
-|-
`DriverManager` | 驱动管理器，用于注册驱动，是获取 `Connection`对象的入口
`Driver` | 数据库驱动，用于获取`Connection`对象
`Connection` | 数据库连接，用于获取`Statement`对象、管理事务
`Statement` | sql执行器，用于执行sql
`ResultSet` | 结果集，用于封装和操作查询结果
`prepareCall` | 用于调用存储过程

## 使用中的注意事项  
1. 记得释放资源。另外，`ResultSet`和`Statement`的关闭都不会导致`Connection`的关闭。  

2. maven要引入oracle的驱动包，要把jar包安装在本地仓库或私服才行。  

3. 使用`PreparedStatement`而不是`Statement`。可以避免SQL注入，并且利用预编译的特点可以提高效率。  

# 使用例子

## 需求
使用JDBC对mysql数据库的用户表进行增删改查。  

## 工程环境
JDK：1.8  
 
maven：3.6.1  

IDE：sts4  
  
mysql driver：8.0.15  

mysql：5.7  


## 主要步骤
一个完整的JDBC保存操作主要包括以下步骤：  

1. 注册驱动（JDK6后会自动注册，可忽略该步骤）;

2. 通过`DriverManager`获得`Connection`对象;

3. 开启事务;

4. 通过`Connection`获得`PreparedStatement`对象;

5. 设置`PreparedStatement`的参数;

6. 执行保存操作;

7. 保存成功提交事务，保存失败回滚事务;

8. 释放资源，包括`Connection`、`PreparedStatement`。


## 创建表
```sql
CREATE TABLE `demo_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `name` varchar(16) COLLATE utf8_unicode_ci NOT NULL COMMENT '用户名',
  `age` int(3) unsigned DEFAULT NULL COMMENT '用户年龄',
  `gmt_create` datetime DEFAULT NULL COMMENT '记录创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '记录最近修改时间',
  `deleted` bit(1) DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `index_age` (`age`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci
```

## 创建项目
项目类型Maven Project，打包方式jar  

## 引入依赖
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
注意：由于oracle商业版权问题，maven并不提供`Oracle JDBC driver`，需要将驱动包手动添加到本地仓库或私服。  

## 编写jdbc.prperties
下面的url拼接了好几个参数，主要为了避免乱码和时区报错的异常。  

路径：resources目录下  
```properties
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://localhost:3306/github_demo?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=true
#这里指定了字符编码和解码格式，时区，是否加密传输
username=root
password=root
#注意，xml配置是&采用&amp;替代
```
如果是oracle数据库，配置如下：  

```properties
driver=oracle.jdbc.driver.OracleDriver
url=jdbc:oracle:thin:@//localhost:1521/xe
username=system
password=root
```
## 获得Connection对象

```java
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
```

## 使用Connection对象完成保存操作
这里简单地模拟实际业务层调用持久层，并开启事务。另外，获取连接、开启事务、提交回滚、释放资源都通过自定义的工具类 `JDBCUtil` 来实现，具体见源码。  

```java
	@Test
	public void save() {
		UserDao userDao = new UserDaoImpl();
		// 创建用户
		User user = new User( "zzf002", 18, new Date(), new Date() );
		try {
			// 开启事务
			JDBCUtil.startTrasaction();
			// 保存用户
			userDao.insert( user );
			// 提交事务
			JDBCUtil.commit();
		} catch( Exception e ) {
			// 回滚事务
			JDBCUtil.rollback();
			e.printStackTrace();
		} finally {
			// 释放资源
			JDBCUtil.release();
		}
	}
```
接下来看看具体的保存操作，即DAO层方法。  
```java
	public void insert( User user ) throws Exception {
		String sql = "insert into demo_user (name,age,gmt_create,gmt_modified) values(?,?,?,?)";
		Connection connection = JDBCUtil.getConnection();
		//获取PreparedStatement对象
		PreparedStatement prepareStatement = connection.prepareStatement( sql );
		//设置参数
		prepareStatement.setString( 1, user.getName() );
		prepareStatement.setInt( 2, user.getAge() );
		prepareStatement.setDate( 3, new java.sql.Date( user.getGmt_create().getTime() ) );
		prepareStatement.setDate( 4, new java.sql.Date( user.getGmt_modified().getTime() ) );
		//执行保存
		prepareStatement.executeUpdate();
		//释放资源
		JDBCUtil.release( prepareStatement, null );
	}
```

# 源码分析

## 驱动注册

### DriverManager

`DriverManager`用于管理数据库驱动。`registeredDrivers`是`DriverManager`的成员属性，是一个集合，存放的是不同的数据库驱动对象。 

```java
    private final static CopyOnWriteArrayList<DriverInfo> registeredDrivers = new CopyOnWriteArrayList<>(); 
```
接下来是`DriverManager`的两个重要方法。  

1. `registerDriver`方法可以将驱动放入这个集合。  

2. `getConnection`方法从这个集合中找到合适的驱动来获得连接对象。  

### 为什么Class.forName(com.mysql.cj.jdbc.Driver) 可以注册驱动？ 
当加载`com.mysql.cj.jdbc.Driver`这个类时，静态代码块中会执行注册驱动的方法。  

```java
    static {
        try {
            java.sql.DriverManager.registerDriver(new Driver());
        } catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }
```

### 为什么JDK6后不需要Class.forName也能注册驱动？

因为从JDK6开始，`DriverManager`增加了以下静态代码块，当类被加载时会执行static代码块的方法。  

而这个方法会去扫描classpath下实现`java.sql.Driver`接口的类，加载后注册。  

```java
    static {
        loadInitialDrivers();
    }
```

## 获得连接对象
### DriverManager.getConnection
获取连接对象的入口是`DriverManager.getConnection`，调用时需要传入url、username和password。  

获取连接对象需要调用`java.sql.Driver`实现类（即数据库驱动）的方法，而具体调用哪个实现类呢？  

正如前面讲到的，注册的数据库驱动被存放在`registeredDrivers`中，所以只有从这个集合中获取就可以了。  

注意：考虑篇幅，以下代码经过修改，仅保留所需部分。  

```java
    public static Connection getConnection(String url,
        String user, String password) throws SQLException {
        java.util.Properties info = new java.util.Properties();

        if (user != null) {
            info.put("user", user);
        }
        if (password != null) {
            info.put("password", password);
        }

        return (getConnection(url, info, Reflection.getCallerClass()));
    }
    private static Connection getConnection(
        String url, java.util.Properties info, Class<?> caller) throws SQLException {
        ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
        //遍历所有注册的数据库驱动
        for(DriverInfo aDriver : registeredDrivers) {
            //先检查这个驱动是否当前类加载器注册的，如果是才进入
            if(isDriverAllowed(aDriver.driver, callerCL)) {
                //这一步是关键，会去调用Driver的connect方法
                Connection con = aDriver.driver.connect(url, info);
                if (con != null) {
                    return con;
                }
            } else {
                println("    skipping: " + aDriver.getClass().getName());
            }
        }
    }
```
### com.mysql.cj.jdbc.Driver.connection
由于使用的是mysql的数据驱动，这里实际调用的是`com.mysql.cj.jdbc.Driver`的方法。  

从以下代码可以看出，mysql支持支持多节点部署的策略，本文仅对单机版进行扩展。  

注意：考虑篇幅，以下代码经过修改，仅保留所需部分。  
```java
    public java.sql.Connection connect(String url, Properties info) throws SQLException {
         //mysql支持多节点部署的策略，根据架构不同，url也有所区别。
         private static final String REPLICATION_URL_PREFIX = "jdbc:mysql:replication://";
         private static final String URL_PREFIX = "jdbc:mysql://";
         private static final String MXJ_URL_PREFIX = "jdbc:mysql:mxj://";
         public static final String LOADBALANCE_URL_PREFIX = "jdbc:mysql:loadbalance://";
        //根据url的类型来返回不同的连接对象，这里仅考虑单机版
        ConnectionUrl conStr = ConnectionUrl.getConnectionUrlInstance(url, info);
        switch (conStr.getType()) {
            case SINGLE_CONNECTION:
                //调用ConnectionImpl.getInstance获取连接对象
                return com.mysql.cj.jdbc.ConnectionImpl.getInstance(conStr.getMainHost());

            case LOADBALANCE_CONNECTION:
                return LoadBalancedConnectionProxy.createProxyInstance((LoadbalanceConnectionUrl) conStr);

            case FAILOVER_CONNECTION:
                return FailoverConnectionProxy.createProxyInstance(conStr);

            case REPLICATION_CONNECTION:
                return ReplicationConnectionProxy.createProxyInstance((ReplicationConnectionUrl) conStr);

            default:
                return null;
        }
    }
```
### ConnectionImpl.getInstance
这个类有个比较重要的字段`session`，可以把它看成一个会话，和我们平时浏览器访问服务器的会话差不多，后续我们进行数据库操作就是基于这个会话来实现的。  

注意：考虑篇幅，以下代码经过修改，仅保留所需部分。 
```java
    private NativeSession session = null;
    public static JdbcConnection getInstance(HostInfo hostInfo) throws SQLException {
        return new ConnectionImpl(hostInfo);
    }
    public ConnectionImpl(HostInfo hostInfo) throws SQLException {
        //根据hostInfo初始化成员属性，包括数据库主机名、端口、用户名、密码、数据库及其他参数设置，这里不放入，其实核心的代码如下： 
        createNewIO(false);
    }
    public void createNewIO(boolean isForReconnect) {
        if (!this.autoReconnect.getValue()) {
            //这里只看不重试的方法
            connectOneTryOnly(isForReconnect);
            return;
        }

        connectWithRetries(isForReconnect);
    }
    private void connectOneTryOnly(boolean isForReconnect) throws SQLException {

        JdbcConnection c = getProxy();
        //调用NativeSession对象的connect方法连接数据库
        this.session.connect(this.origHostInfo, this.user, this.password, this.database, DriverManager.getLoginTimeout() * 1000, c);
        return;
    }

```
### NativeSession.connect
接下来的代码主要是建立会话的过程，首先时建立物理连接，然后根据协议建立会话。  

注意：考虑篇幅，以下代码经过修改，仅保留所需部分。 

```java
    public void connect(HostInfo hi, String user, String password, String database, int loginTimeout, TransactionEventHandler transactionManager)
            throws IOException {
        //采用TCP/IP协议获得物理连接
        SocketConnection socketConnection = new NativeSocketConnection();
        socketConnection.connect(this.hostInfo.getHost(), this.hostInfo.getPort(), this.propertySet, getExceptionInterceptor(), this.log, loginTimeout);

        // 根据物理连接创建与数据库的通信协议
        if (this.protocol == null) {
            this.protocol = NativeProtocol.getInstance(this, socketConnection, this.propertySet, this.log, transactionManager);
        } else {
            this.protocol.init(this, socketConnection, this.propertySet, transactionManager);
        }

        // 创建会话
        this.protocol.connect(user, password, database);
    }
```
> 学习使我快乐！！
