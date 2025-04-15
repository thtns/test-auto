package cn.thtns.test.auto.service;

import cn.thtns.test.auto.response.NewProfitRes;
import cn.thtns.test.auto.response.OldProfitRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@AllArgsConstructor
public class NewProfitService {

    //总收益
    private static final String NEW_PROFIT_API = "https://agentv2.wanzhuangkj.com/api/operator/income/profitReport";

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public NewProfitRes newProfitRes(String token) {
        return objectMapper.readValue(getApiResponse(NEW_PROFIT_API , token), NewProfitRes.class);
    }


    private String getApiResponse(String apiUrl, String BEARER_TOKEN) {
        return restClient.get()
                .uri(apiUrl)
                .header("authorization", BEARER_TOKEN)
                .retrieve()
                .body(String.class);
    }
}
