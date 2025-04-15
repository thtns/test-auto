package cn.thtns.test.auto.service;

import cn.hutool.core.util.StrUtil;
import cn.thtns.test.auto.request.LoginReq;
import cn.thtns.test.auto.response.CompanyRes;
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
public class OperatorsOrPartnersService {

    private static final String GET_U_ID = "https://manage-web.wanzhuangkj.com/api/admin/getNewOperatorsOrPartnersPageList?page=1&page_size=10&company_id=2&phone={}&settle_mode=0";


    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Integer getUId(String token, LoginReq loginReq) {
        return objectMapper.readValue(getApiResponse(StrUtil.format(GET_U_ID, loginReq.getUsername()), token), CompanyRes.class).getData().getList().get(0).getId();
    }




    private String getApiResponse(String apiUrl, String BEARER_TOKEN) {
        return restClient.get()
                .uri(apiUrl)
                .header("authorization", BEARER_TOKEN)
                .retrieve()
                .body(String.class);
    }


}
