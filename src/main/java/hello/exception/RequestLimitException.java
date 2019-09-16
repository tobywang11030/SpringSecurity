package hello.exception;

/**
 * @author tobywang
 * @date 9/16/2019
 */
public class RequestLimitException  extends RuntimeException{
    private Integer code;
    public RequestLimitException(Integer code, String message){
        super(message);
        this.code = code;
    }
}
