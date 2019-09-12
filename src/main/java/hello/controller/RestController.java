package hello.controller;

import hello.model.User;
import hello.repository.UserRepository;
import hello.services.SmsService;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    UserRepository userRepository;
    
    @Autowired
    SmsService smsService;
    
    @Autowired
    private HttpServletRequest request;
    
    
    @RequestMapping("/sendSmsCode")
    public String sendSmsCode(@PathParam("number") String number) throws DocumentException {
        int mobile_code = (int) ((Math.random() * 9 + 1) * 100000);
        Boolean isSendSuccess = smsService.sendCodeBySMS(number, String.valueOf(mobile_code));
        if (isSendSuccess) {
            //将验证码存储在Session当中供filter验证
            HttpSession session = request.getSession();
            session.setAttribute(SMS_CODE, mobile_code);
            return "success";
        }
        return "error";
    }
    
    //@Secured(ROLE_ADMIN)
    @RequestMapping("/search")
    public User search(@PathParam("email") String email) {
        return userRepository.findByEmail(email);
    }
    
}
