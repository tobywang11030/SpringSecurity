package hello.controller;

import hello.services.SmsService;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;

import static hello.constant.AuthenticationConstant.SMS_CODE;

/**
 * @author tobywang
 * @date 9/4/2019
 */

@org.springframework.web.bind.annotation.RestController
public class RestController {
    
    @Autowired
    SmsService smsService;
    
    @Autowired
    private HttpServletRequest request;
    
    
    @RequestMapping("/sendSmsCode")
    @ResponseBody
    public String sendSmsCode(@PathParam("number") String number) throws DocumentException {
        int mobile_code = (int) ((Math.random() * 9 + 1) * 100000);
        Boolean isSendSuccess = smsService.sendCodeBySMS(number, String.valueOf(mobile_code));
        if (isSendSuccess) {
            HttpSession session = request.getSession();
            session.setAttribute(SMS_CODE, mobile_code);
            return "success";
        }
        return "error";
    }
}
