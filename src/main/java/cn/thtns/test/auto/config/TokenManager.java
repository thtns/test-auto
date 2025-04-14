package cn.thtns.test.auto.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TokenManager {

    @Autowired
    private RestClient restClient;

    private static final ConcurrentHashMap<String, String> tokenMap = new ConcurrentHashMap<>(); // 存储账号和 Token 的映射

    /**
     * 获取指定账号的 Token，如果不存在则自动生成
     * @param phone 用户名
     * @param companyId 公司id
     * @return 对应账号的 BEARER_TOKEN
     */
    public synchronized String getToken(String phone, String companyId) {
        return tokenMap.computeIfAbsent(phone, key -> {
            log.info("Token 未找到，为账号 {} 生成新 Token...", phone);
            String token = login(phone, companyId);
            log.info("成功获取 BEARER_TOKEN for {}: {}", phone, token);
            return token;
        });
    }

    /**
     * 登录接口调用以获取 BEARER_TOKEN
     * @param phone 用户名
     * @param companyId 公司id
     * @return 登录成功后的 Token
     */
    public String login(String phone, String companyId) {

        String body = restClient.get()
                .uri(StrUtil.format("https://agentv2.wanzhuangkj.com/api/auth/adminLogin?phone={}&company_id={}&password=wz020202&operator_phone=18327519799&is_operator=1", phone,companyId))
                .retrieve()
                .body(String.class);

        String str = JSONUtil.parseObj(body).getJSONObject("data").getStr("token");

        return StrUtil.format("bearer {}", str);


    }

    /**
     * 获取指定账号的 Token，如果不存在则自动生成
     * @param phone 手机号
     * @param companyId 公司id
     * @return 对应账号的 BEARER_TOKEN
     */
    public synchronized String getShareToken(String phone, String companyId) {
        return tokenMap.computeIfAbsent(phone, key -> {
            log.info("Token 未找到，为账号 {} 生成新 Token...", phone);
            String token = shareLogin(phone, companyId);
            log.info("成功获取 BEARER_TOKEN for {}: {}", phone, token);
            return token;
        });
    }
    /**
     * 登录接口调用以获取 BEARER_TOKEN
     * @param phone 用户名
     * @param companyId 公司id
     * @return 登录成功后的 Token
     */
    public String shareLogin(String phone, String companyId) {

        String body = restClient.get()
                .uri(StrUtil.format("https://agentv2.wanzhuangkj.com/api/auth/adminLogin?phone={}&company_id={}&password=wz020202&operator_phone=18327519799&is_operator=0", phone,companyId))
                .retrieve()
                .body(String.class);

        String str = JSONUtil.parseObj(body).getJSONObject("data").getStr("token");

        return StrUtil.format("bearer {}", str);


    }

    public String manageLogin(String phone, String password,String type) {

        String json = "{\"phone\":\"18327519799\",\"password\":\"wz123456.\",\"type\":\"manage\"}";

        String body = restClient.post()
                .uri(StrUtil.format("https://manage-web.wanzhuangkj.com/public/login"))
                .body(json)
                .header("Content-Type", "application/json")
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
