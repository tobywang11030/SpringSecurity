package hello.solr.service;

import hello.constant.FileType;
import hello.solr.bean.FileIndex;

import java.util.List;

/**
 * @author tobywang
 * @date 9/27/2019
 */
public interface SolrService {
    void add(FileIndex fileIndex);
    
    void addAll(List<FileIndex> fileIndexList);
    
    List<FileIndex> searchFile(String path, FileType fileType, String name);
}
