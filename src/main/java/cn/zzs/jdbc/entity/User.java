/**
 * 
 */
package cn.zzs.jdbc.entity;

import java.util.Date;

/**
 * @ClassName: User
 * @Description: 用户实体类
 * @author: zzs
 * @date: 2019年11月03日 下午10:09:08
 */
public class User {

	/**
	 * 用户id
	 */
	private Long id;

	/**
	 * 用户名
	 */
	private String name;

	/**
	 * 用户年龄
	 */
	private Integer age;

	/**
	 * 是否删除
	 */
	private Boolean deleted;

	/**
	 * 记录创建时间
	 */
	private Date gmt_create;

	/**
	 * 记录最近更新时间
	 */
	private Date gmt_modified;

	public User( String name, Integer age, Date gmt_create, Date gmt_modified ) {
		super();
		this.name = name;
		this.age = age;
		this.gmt_create = gmt_create;
		this.gmt_modified = gmt_modified;
	}

	public User() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId( Long id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge( Integer age ) {
		this.age = age;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted( Boolean deleted ) {
		this.deleted = deleted;
	}

	public Date getGmt_create() {
		return gmt_create;
	}

	public void setGmt_create( Date gmt_create ) {
		this.gmt_create = gmt_create;
	}

	public Date getGmt_modified() {
		return gmt_modified;
	}

	public void setGmt_modified( Date gmt_modified ) {
		this.gmt_modified = gmt_modified;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", age=" + age + ", deleted=" + deleted + ", gmt_create=" + gmt_create + ", gmt_modified=" + gmt_modified + "]";
	}

}
