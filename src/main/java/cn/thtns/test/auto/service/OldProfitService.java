package cn.thtns.test.auto.service;

import cn.hutool.core.util.StrUtil;
import cn.thtns.test.auto.response.OldProfitRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class OldProfitService {

    //总收益
    private static final String OLD_PROFIT_API = "https://manage-web.wanzhuangkj.com/api/admin/getAllProfit?company_id={}&u_id={}&u_type=2&profit_type=dealer";

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public OldProfitRes oldProfitRes(String token, String phone,Integer uid,Integer companyId) {
        return objectMapper.readValue(getApiResponse(StrUtil.format(OLD_PROFIT_API,companyId,uid), token), OldProfitRes.class);
    }


    private String getApiResponse(String apiUrl, String BEARER_TOKEN) {
        return restClient.get()
                .uri(apiUrl)
                .header("authorization", BEARER_TOKEN)
                .retrieve()
                .body(String.class);
    }
}
