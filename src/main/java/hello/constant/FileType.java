package hello.constant;

/**
 * @author tobywang
 * @date 9/27/2019
 */
public enum FileType {
    FILE("file"), FOLDER("folder");
    
    private String type;
    
    private FileType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
