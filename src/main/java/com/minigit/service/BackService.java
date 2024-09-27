package com.minigit.service;

import com.minigit.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class BackService {
    @Autowired
    private CommitUtilService commitUtilService;
    public Map<String, String> getCurrentCommitTree(String repoPath){
        String currentCommitHash = FileUtils.getCurrentCommitHash(repoPath);
        String oldTreeHeadHash = FileUtils.getTreeHeadHash(currentCommitHash, repoPath);
        Map<String, String> currentCommitTreeMap = new HashMap<>();
        commitUtilService.createOldCommitTree(oldTreeHeadHash,currentCommitTreeMap, repoPath);
        if(currentCommitTreeMap.size() == 0){
            System.out.println("当前分支还没有提交！！");
            return null;
        }
        return currentCommitTreeMap;
    }

    public Map<String, String> getOldCommitTree(String oldCommitHash, String repoPath){
        String oldTreeHeadHash = FileUtils.getTreeHeadHash(oldCommitHash, repoPath);
        Map<String, String> oldCommitTreeMap = new HashMap<>();
        commitUtilService.createOldCommitTree(oldTreeHeadHash,oldCommitTreeMap, repoPath);
        if(oldCommitTreeMap.size() == 0){
            System.out.println("该历史提交不存在！！");
            return null;
        }
        return oldCommitTreeMap;
    }


    public Map<String,String> getDeleteMap(Map<String, String> currentCommitTreeMap,
                                                  Map<String, String> oldCommitTreeMap){
        Map<String, String> deleteMap = new HashMap<>();
        for (String path : currentCommitTreeMap.keySet()) {
            // 如果老版本的commitTree中没有某个文件，则加入deleteMap，之后从实际目录中删除它
            if(!oldCommitTreeMap.containsKey(path)){
                deleteMap.put(path,currentCommitTreeMap.get(path));
            }
        }
        return deleteMap;
    }

    public Map<String,String> getCreateMap(Map<String, String> currentCommitTreeMap,
                                                  Map<String, String> oldCommitTreeMap){
        Map<String, String> createMap = new HashMap<>();
        for (String path : oldCommitTreeMap.keySet()) {
            // 如果当前版本的commitTree中没有某个文件，则加入createMap，之后从实际目录中创建它
            if(!currentCommitTreeMap.containsKey(path)){
                createMap.put(path,currentCommitTreeMap.get(path));
            }
        }
        return createMap;
    }

    public Map<String, String> getFileMap(String repoPath){
        Map<String, String> fileMap = new HashMap<>();
        commitUtilService.createFileTree(fileMap, new File(repoPath));
        return fileMap;
    }
}
