package cn.thtns.test.auto.verify;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TokenManager {

    @Autowired
    private RestClient restClient;

    private static final ConcurrentHashMap<String, String> tokenMap = new ConcurrentHashMap<>(); // 存储账号和 Token 的映射

    /**
     * 获取指定账号的 Token，如果不存在则自动生成
     * @param username 用户名
     * @param password 密码
     * @return 对应账号的 BEARER_TOKEN
     */
    public synchronized String getToken(String username, String password) {
        return tokenMap.computeIfAbsent(username, key -> {
            log.info("Token 未找到，为账号 {} 生成新 Token...", username);
            String token = login(username, password);
            log.info("成功获取 BEARER_TOKEN for {}: {}", username, token);
            return token;
        });
    }

    /**
     * 登录接口调用以获取 BEARER_TOKEN
     * @param phone 用户名
     * @param password 密码
     * @return 登录成功后的 Token
     */
    public String login(String phone, String password) {

        String body = restClient.get()
                .uri(StrUtil.format("https://agentv2.wanzhuangkj.com/api/auth/agentLogin?password={}&company_id=2&phone={}&type=2", password, phone))
                .retrieve()
                .body(String.class);

        String str = JSONUtil.parseObj(body).getJSONObject("data").getStr("token");

        return StrUtil.format("bearer {}", str);


    }



    /**
     * 清除某个账号的 Token（用于强制重新登录）
     * @param username 用户名
     */
    public static void clearToken(String username) {
        tokenMap.remove(username);
        log.info("已清除账号 {} 的 Token", username);
    }





}
