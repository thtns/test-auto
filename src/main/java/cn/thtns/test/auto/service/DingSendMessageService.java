package cn.thtns.test.auto.service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;

@Slf4j
@Service
@AllArgsConstructor
public class DingSendMessageService {

    public static final String CUSTOM_ROBOT_TOKEN = "c03d49530f0133056a973fa51cc3c26d7b5a4a94b3205f2e062e4b2e9566fdaa";


    public static final String SECRET = "SEC60338c1fd446870359b891e753d006664f141dc023138b48acc03a655aadf9ff";

    @SneakyThrows
    public void sendMessage(String content) {

        Long timestamp = System.currentTimeMillis();
        System.out.println(timestamp);
        String secret = SECRET;
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        System.out.println(sign);

        //sign字段和timestamp字段必须拼接到请求URL上，否则会出现 310000 的错误信息
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?sign=" + sign + "&timestamp=" + timestamp);
        OapiRobotSendRequest req = new OapiRobotSendRequest();
        /**
         * 发送文本消息
         */
        //定义文本内容
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent(content);
        //定义 @ 对象
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
//            at.setAtUserIds(Arrays.asList(USER_ID));
        //设置消息类型
        req.setMsgtype("text");
        req.setText(text);
        req.setAt(at);
        OapiRobotSendResponse rsp = client.execute(req, CUSTOM_ROBOT_TOKEN);
        System.out.println(rsp.getBody());


    }

    ;


}
