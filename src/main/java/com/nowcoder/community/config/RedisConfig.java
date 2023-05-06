package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @Date: create in 8:59 2023/3/15
 * @describe: redis配置类,
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        /**
        * @Description:  设置适当的序列化方式可以避免因为序列化格式不同而引起的数据错误，
         *               提高Redis的查询效率和响应速度。  设置适当的序列化方式可以提高数据的存储效率、减少存储空间和保证数据的正确性。
        * @Date: 2023/3/15 9:48
        */

        //设置Key的序列化方式
        //相比于使用JSON等其他类型序列化方式，序列化为String类型虽然占用的存储空间可能会更大，但是适合存储字符串类型的数据
        template.setKeySerializer(RedisSerializer.string());

        //JSON格式的字符串可以更加直观地展示该Java对象，占用的存储空间也比序列化后的二进制数据更小。
        //设置普通的value 的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        //使上面的配置生效
        template.afterPropertiesSet();
        return template;
    }
}



