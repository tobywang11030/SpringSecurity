package hello.solr.bean;

import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;

/**
 * @author tobywang
 * @date 9/27/2019
 */
public class FileIndex implements Serializable {
    
    private static final long serialVersionUID = 8352940146876879705L;
    //@Field("id") 因为ID是自增的，所以这里会报错，还未解决
    private String id;
    
    @Field("name")
    private String name;
    
    @Field("full_path")
    private String path;
    
    @Field("type")
    private String type;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
