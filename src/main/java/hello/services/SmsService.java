package hello.services;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author tobywang
 * @date 9/4/2019
 */

@Service
public class SmsService {
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    @Value("${sms.apiid}")
    private String apiId;
    
    @Value("${sms.apikey}")
    private String apiKey;
    
    @Value("${sms.endpoint}")
    private String endPoint;
    
    public Boolean sendCodeBySMS(String phoneNumber, String code) throws DocumentException {
        String template = "您的验证码是：%s。请不要把验证码泄露给其他人。";
        String content = String.format(template, code);
        String result = doPost(phoneNumber, content);
        Document doc = DocumentHelper.parseText(result);
        Element root = doc.getRootElement();
        
        String resultCode = root.elementText("code");
        
        
        if ("2".equals(resultCode)) {
            logger.info("短信提交成功");
            return true;
        }
        return false;
    }
    
    private String doPost(String phone, String smsContent) {
        
        String content = null;
        
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        
        // 参数
        StringBuffer params = new StringBuffer();
        
        // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
        params.append("method=Submit");
        params.append("&");
        params.append("account=" + apiId);
        params.append("&");
        params.append("password=" + apiKey);
        params.append("&");
        params.append("mobile=" + phone);
        params.append("&");
        params.append("content=" + smsContent);
        
        
        // 创建Post请求
        HttpPost httpPost = new HttpPost(endPoint + "?" + params);
        
        // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
        httpPost.setHeader("Content-Type", "application/json;charset=utf8");
        
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            
            logger.info("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                logger.info("响应内容长度为:" + responseEntity.getContentLength());
                content = EntityUtils.toString(responseEntity);
                logger.info("响应内容为:" + content);
            }
        } catch (ParseException | IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return content;
    }
}
