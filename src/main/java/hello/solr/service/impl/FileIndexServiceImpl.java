package hello.solr.service.impl;

import hello.constant.FileType;
import hello.solr.bean.FileIndex;
import hello.solr.service.FileIndexService;
import hello.solr.service.SolrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tobywang
 * @date 9/27/2019
 */

@Service
public class FileIndexServiceImpl implements FileIndexService {
    
    @Autowired
    private SolrService solrService;
    
    @Override
    public void indexFromPath(String path, FileType fileType) {
        List<FileIndex> fileIndexList = new ArrayList<>();
        File rootFile = new File(path);
        fileIndexList = fetch(rootFile, fileType, fileIndexList);
        if (!CollectionUtils.isEmpty(fileIndexList)) {
            solrService.addAll(fileIndexList);
        }
    }
    
    @Override
    public List<FileIndex> searchFile(String path, FileType fileType, String name) {
        return solrService.searchFile(path, fileType, name);
    }
    
    
    private List<FileIndex> fetch(File rootFile, FileType fileType, List<FileIndex> fileIndexList) {
        if (rootFile.exists()) {
            if (rootFile.isFile() && fileType.equals(FileType.FILE)) {
                FileIndex fileIndex = new FileIndex();
                fileIndex.setName(rootFile.getName());
                fileIndex.setPath(rootFile.getPath());
                fileIndex.setType(FileType.FILE.getType());
                fileIndexList.add(fileIndex);
            } else if (rootFile.isDirectory()) {
                if (fileType.equals(FileType.FOLDER)) {
                    FileIndex folderIndex = new FileIndex();
                    folderIndex.setName(rootFile.getName());
                    folderIndex.setPath(rootFile.getPath());
                    folderIndex.setType(FileType.FOLDER.getType());
                    fileIndexList.add(folderIndex);
                }
                File[] files = rootFile.listFiles();
                if (files != null) {
                    for (File file : files) {
                        fetch(file, fileType, fileIndexList);
                    }
                }
            }
        }
        
        return fileIndexList;
    }
}
