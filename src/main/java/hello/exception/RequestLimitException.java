package hello.exception;

/**
 * @author tobywang
 * @date 9/16/2019
 * 自定义Exception，用于在超出访问频率后抛出异常并捕获后跳转到自定义异常页面
 */
public class RequestLimitException  extends RuntimeException{
    private Integer code;
    public RequestLimitException(Integer code, String message){
        super(message);
        this.code = code;
    }
}
