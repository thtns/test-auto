package cn.thtns.test.auto.wz;

import cn.thtns.test.auto.entity.SysUser;
import cn.thtns.test.auto.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import org.springframework.web.client.RestTemplate;
import org.testng.annotations.Test;

import java.util.List;


@SpringBootTest
public class UserInfoTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Description("查询用户信息")
    public void testSelect() {

        System.out.println(("----- selectAll method test ------"));
        List<SysUser> userList = userMapper.selectList(null);

        userList.forEach(System.out::println);




    }




}
