/**
 * 
 */
package cn.zzs.jdbc.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import cn.zzs.jdbc.dao.UserDao;
import cn.zzs.jdbc.entity.User;
import cn.zzs.jdbc.util.JDBCUtil;

/**
 * @ClassName: UserDao
 * @Description: 用户持久层操作类
 * @author: zzs
 * @date: 2019年11月03日 下午10:09:08
 */
public class UserDaoImpl implements UserDao {

	@Override
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

	@Override
	public void update( User user ) throws Exception {
		String sql = "update demo_user set name = ?,age = ?,gmt_create = ?,gmt_modified = ? where id = ?";
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement( sql );
		prepareStatement.setString( 1, user.getName() );
		prepareStatement.setInt( 2, user.getAge() );
		prepareStatement.setDate( 3, new java.sql.Date( user.getGmt_create().getTime() ) );
		prepareStatement.setDate( 4, new java.sql.Date( user.getGmt_modified().getTime() ) );
		prepareStatement.setLong( 5, user.getId() );
		prepareStatement.executeUpdate();
		JDBCUtil.release( prepareStatement, null );
	}

	@Override
	public void delete( Long id ) throws Exception {
		String sql = "update demo_user set deleted = ? where id = ?";
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement( sql );
		prepareStatement.setInt( 1, 1 );
		prepareStatement.setLong( 2, id );
		prepareStatement.executeUpdate();
		JDBCUtil.release( prepareStatement, null );
	}

	@Override
	public User selectById( Long id ) throws Exception {
		User user = null;
		String sql = "select * from demo_user where id = ?";
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement( sql );
		prepareStatement.setLong( 1, id );
		ResultSet resultSet = prepareStatement.executeQuery();
		while( resultSet.next() ) {
			user = new User();
			user.setId( resultSet.getLong( "id" ) );
			user.setName( resultSet.getString( "name" ) );
			user.setAge( resultSet.getInt( "age" ) );
			user.setDeleted( resultSet.getInt( "deleted" ) == 1 ? true : false );
			user.setGmt_create( resultSet.getDate( "gmt_create" ) );
			user.setGmt_modified( resultSet.getDate( "gmt_modified" ) );
		}
		JDBCUtil.release( prepareStatement, resultSet );
		return user;
	}

	@Override
	public User selectByName( String name ) throws Exception {
		User user = null;
		String sql = "select * from demo_user where name = ?";
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement( sql );
		prepareStatement.setString( 1, name );
		ResultSet resultSet = prepareStatement.executeQuery();
		while( resultSet.next() ) {
			user = new User();
			user.setId( resultSet.getLong( "id" ) );
			user.setName( resultSet.getString( "name" ) );
			user.setAge( resultSet.getInt( "age" ) );
			user.setDeleted( resultSet.getInt( "deleted" ) == 1 ? true : false );
			user.setGmt_create( resultSet.getDate( "gmt_create" ) );
			user.setGmt_modified( resultSet.getDate( "gmt_modified" ) );
		}
		JDBCUtil.release( prepareStatement, resultSet );
		return user;
	}

	@Override
	public List<User> selectAll() throws Exception {
		List<User> list = new ArrayList<User>();
		String sql = "select * from demo_user where deleted = 0";
		Connection connection = JDBCUtil.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement( sql );
		ResultSet resultSet = prepareStatement.executeQuery();
		while( resultSet.next() ) {
			User user = new User();
			user = new User();
			user.setId( resultSet.getLong( "id" ) );
			user.setName( resultSet.getString( "name" ) );
			user.setAge( resultSet.getInt( "age" ) );
			user.setDeleted( resultSet.getInt( "deleted" ) == 1 ? true : false );
			user.setGmt_create( resultSet.getDate( "gmt_create" ) );
			user.setGmt_modified( resultSet.getDate( "gmt_modified" ) );
			list.add( user );
		}
		JDBCUtil.release( prepareStatement, resultSet );
		return list;
	}

}
