package com.nowcoder.community.actuator;

import com.nowcoder.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Date: create in 10:31 2023/4/2
 * @describe:
 */

@Component
@Endpoint(id="database")
@Slf4j
public class DatabaseEndpoint {

    @Autowired
    private DataSource dataSource;

    //这个断点只能GET请求访问
    @ReadOperation
    public String checkConnection(){
        try (
                Connection connection = dataSource.getConnection();
        ){
            return CommunityUtil.getJSONString(0,"连接成功");
        } catch (SQLException e) {
            log.error("获取连接失败");
            return CommunityUtil.getJSONString(0,"获取连接失败");
        }

    }

}











