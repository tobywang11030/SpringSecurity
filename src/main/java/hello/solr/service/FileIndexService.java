package hello.solr.service;

import hello.constant.FileType;
import hello.solr.bean.FileIndex;

import java.util.List;

/**
 * @author tobywang
 * @date 9/27/2019
 */
public interface FileIndexService {
    void indexFromPath(String path, FileType fileType);
    
    List<FileIndex> searchFile(String path, FileType fileType, String name);
}
