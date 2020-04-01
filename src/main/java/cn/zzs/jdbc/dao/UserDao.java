/**
 * 
 */
package cn.zzs.jdbc.dao;

import java.util.List;

import cn.zzs.jdbc.entity.User;

/**
 * @ClassName: UserDao
 * @Description: 用户持久层操作接口
 * @author: zzs
 * @date: 2019年11月03日 下午10:09:08
 */
public interface UserDao {

    /**
     * 新增用户
     * @param user
     */
    void insert(User user) throws Exception;

    /**
     * 更新用户
     * @param user
     */
    void update(User user) throws Exception;

    /**
     * 删除用户
     * @param id
     */
    void delete(Long id) throws Exception;

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User selectById(Long id) throws Exception;

    /**
     * 根据用户名查询用户
     * @param name
     * @return
     */
    User selectByName(String name) throws Exception;

    /**
     * 
     * @Title: selectAll
     * @Description: 查询所有用户
     * @author: zzs
     * @date: 2019年11月3日 上午11:18:59
     * @return: List<User>
     */
    List<User> selectAll() throws Exception;
}
