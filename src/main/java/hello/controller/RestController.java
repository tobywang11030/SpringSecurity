package hello.controller;

import hello.model.User;
import hello.repository.UserRepository;
import hello.services.SmsService;
import hello.services.VisitedService;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    
    @Autowired
    private HttpServletResponse response;
    
    @Autowired
    private VisitedService visitedService;
    
    
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
    
    @RequestMapping("/updateVisit")
    public String updateVisit(@PathParam("path") String path) {
        int count = visitedService.getVisitedCount(path) + 1;
        visitedService.putVisitedCount(path, count);
        response.setHeader("Access-Control-Allow-Origin", "*");
        return count + "";
    }
    
    @RequestMapping("/getVisit")
    public String getVisit() {
        response.setHeader("Access-Control-Allow-Origin", "*");
        return visitedService.getMaxKey();
    }
    
}
