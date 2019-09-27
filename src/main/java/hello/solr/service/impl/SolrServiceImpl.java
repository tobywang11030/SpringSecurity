package hello.solr.service.impl;

import hello.constant.FileType;
import hello.solr.bean.FileIndex;
import hello.solr.service.SolrService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


/**
 * @author tobywang
 * @date 9/27/2019
 */

@Service
public class SolrServiceImpl implements SolrService {
    
    private static final Logger logger = LoggerFactory.getLogger(SolrServiceImpl.class);
    
    @Autowired
    private SolrClient solrClient;
    
    @Override
    public void add(FileIndex fileIndex) {
        try {
            solrClient.addBean(fileIndex);
            solrClient.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    
    @Override
    public void addAll(List<FileIndex> fileIndexList) {
        try {
            solrClient.addBeans(fileIndexList);
            solrClient.commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        
    }
    
    @Override
    public List<FileIndex> searchFile(String path, FileType fileType, String name) {
        SolrQuery solrQuery = new SolrQuery();
        StringBuilder params = new StringBuilder();
        if (StringUtils.isEmpty(path)) {
            params.append("full_path:*");
        } else {
            params.append("full_path:" + path);
        }
        if (StringUtils.isEmpty(name)) {
            params.append(" AND name:*");
        } else {
            params.append(" AND name:" + name);
        }
        if (fileType != null) {
            params.append(" AND type:" + fileType.getType());
        } else {
            params.append(" AND type:*");
        }
        solrQuery.setQuery(params.toString());
        
        QueryResponse queryResponse = null;
        try {
            queryResponse = solrClient.query(solrQuery);
            return queryResponse.getBeans(FileIndex.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}
