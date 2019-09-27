package hello.controller;

import hello.constant.FileType;
import hello.solr.bean.FileIndex;
import hello.solr.service.FileIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.List;

/**
 * @author tobywang
 * @date 9/27/2019
 */

@RestController
public class SolrControllder {
    
    @Autowired
    private FileIndexService fileIndexService;
    
    @RequestMapping("/indexFile")
    private String indexFile(@PathParam("path") String path, @PathParam("type") String type) {
        fileIndexService.indexFromPath(path, FileType.valueOf(type));
        return "ok";
    }
    
    @RequestMapping("/searchFile")
    private List<FileIndex> searchFile(@PathParam("path") String path, @PathParam("type") String type, @PathParam("name") String name) {
        return fileIndexService.searchFile(path, type != null ? FileType.valueOf(type) : null, name);
    }
    
}
