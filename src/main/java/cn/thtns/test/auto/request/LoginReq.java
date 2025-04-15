package cn.thtns.test.auto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginReq {

    private String username;
    private String company;

}
