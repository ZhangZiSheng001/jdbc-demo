package cn.zzs.jdbc;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.mysql.cj.jdbc.MysqlXADataSource;
import com.mysql.cj.jdbc.MysqlXid;

/**
 * <p>测试XA事务</p>
 * @author: zzs
 * @date: 2019年12月7日 下午12:47:57
 */
public class XATest {

    public static MysqlXADataSource getDataSource(String url, String username, String password) {
        MysqlXADataSource ds = new MysqlXADataSource();
        ds.setUrl(url);
        ds.setUser(username);
        ds.setPassword(password);
        return ds;
    }

    public static void main(String[] arg) throws Exception {
        String url1 = "jdbc:mysql://localhost:3306/github_demo?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=true";
        String url2 = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=true";

        // 从不同数据库获取数据库数据源
        MysqlXADataSource ds1 = getDataSource(url1, "root", "root");
        MysqlXADataSource ds2 = getDataSource(url2, "zzf", "zzf");

        // 数据库1获取连接
        XAConnection xaConnection1 = ds1.getXAConnection();
        XAResource xaResource1 = xaConnection1.getXAResource();
        Connection connection1 = xaConnection1.getConnection();
        Statement statement1 = connection1.createStatement();

        // 数据库2获取连接
        XAConnection xaConnection2 = ds2.getXAConnection();
        XAResource xaResource2 = xaConnection2.getXAResource();
        Connection connection2 = xaConnection2.getConnection();
        Statement statement2 = connection2.createStatement();

        // 创建事务分支的xid
        Xid xid1 = new MysqlXid(new byte[] { 0x01 }, new byte[] { 0x02 }, 100);
        Xid xid2 = new MysqlXid(new byte[] { 0x011 }, new byte[] { 0x012 }, 100);

        try {
            // 事务分支1关联分支事务sql语句
            xaResource1.start(xid1, XAResource.TMNOFLAGS);
            int update1Result = statement1.executeUpdate("update github_demo.demo_user set deleted = 1 where id = '1'");
            xaResource1.end(xid1, XAResource.TMSUCCESS);

            // 事务分支2关联分支事务sql语句
            xaResource2.start(xid2, XAResource.TMNOFLAGS);
            int update2Result = statement2.executeUpdate("update test.demo_user set deleted = 1 where id = '1'");
            xaResource2.end(xid2, XAResource.TMSUCCESS);

            // 两阶段提交协议第一阶段
            int result1 = xaResource1.prepare(xid1);
            int result2 = xaResource2.prepare(xid2);

            // 两阶段提交协议第二阶段
            if(XAResource.XA_OK == result1 && XAResource.XA_OK == result2) {
                xaResource1.commit(xid1, false);
                xaResource2.commit(xid2, false);
                System.out.println("reslut1:" + update1Result + ", result2:" + update2Result);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            xaConnection1.close();
            connection1.close();
            statement1.close();
            xaConnection2.close();
            connection2.close();
            statement2.close();
        }
    }
}
