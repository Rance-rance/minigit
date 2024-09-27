package com.minigit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jcraft.jsch.SftpException;
import com.minigit.entity.Branch;
import com.minigit.entity.Commit;
import com.minigit.entity.Repo;
import com.minigit.entityService.BranchService;
import com.minigit.entityService.RepoService;
import com.minigit.util.FileUtils;
import com.minigit.util.Sha1Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minigit.util.Sha1Utils.calculateFileSha1;

@Service
public class GitService {
    @Autowired
    private RepoService repoService;
    @Autowired
    private CommitUtilService commitUtilService;
    @Autowired
    private BackService backService;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private BranchService branchService;

    public Repo init(String path, Long authorId, Repo repo) {
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getPath,path);
        Repo repo1 = repoService.getOne(queryWrapper);
        if(repo1 != null){
            System.out.println("仓库已经存在！");
            return repo1;
        }
        File gitDir = new File(path, ".minigit");
        if(!gitDir.exists()){
            gitDir.mkdirs();
        }
        File objectsDir = new File(gitDir, "objects");
        if(!objectsDir.exists()){
            objectsDir.mkdirs();
        }
        File refsDir = new File(gitDir, "refs");
        if(!refsDir.exists()){
            refsDir.mkdirs();
        }
        File headsDir = new File(refsDir, "heads");
        if(!headsDir.exists()){
            headsDir.mkdirs();
        }
        try {
            String headPath = gitDir + File.separator + "HEAD";
            String headBranch = "main";
            FileUtils.writeFileNoAppend(headPath, headBranch);
        } catch (IOException e) {
            e.printStackTrace();
        }
        repo.setAuthorId(authorId);
        System.out.println("初始化仓库成功！");
        return repo;
    }

    public void add(List<File> files, String repoPath) {
        for (File file : files) {
            String hash = calculateFileSha1(file);
            try {
                FileUtils.writeFile(repoPath + File.separator + ".minigit" + File.separator + "INDEX",
                        file.getAbsolutePath() + "\t" + hash);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Commit commit(String message, String committer,String repoPath) throws NoSuchAlgorithmException {
        Commit commit = new Commit();
        String oldCommitHash = FileUtils.getCurrentCommitHash(repoPath);
        String oldTreeHeadHash = FileUtils.getTreeHeadHash(oldCommitHash,repoPath);
        Map<String, String> fileMap = new HashMap<>();
        Map<String, String> indexMap = new HashMap<>();
        Map<String, String> commitTreeMap = new HashMap<>();
        commitUtilService.createIndexTree(indexMap,repoPath);
        commitUtilService.createOldCommitTree(oldTreeHeadHash, commitTreeMap,repoPath);
        commitUtilService.createFileTree(fileMap,new File(repoPath));
        commitUtilService.getNewCommitTree(commitTreeMap, fileMap, indexMap);
        String newTreeHeadHash = commitUtilService.writeTree(new File(repoPath), commitTreeMap, repoPath);
        // 将新的提交写入objects文件，并清空index
        StringBuilder sb = new StringBuilder();
        // 这里应该再有一个提交时间
        String data = sb.append(newTreeHeadHash + System.lineSeparator())
                .append(committer + "\t" + "2020-4-18 00:00:00" + System.lineSeparator())
                .append(oldCommitHash + System.lineSeparator())
                .append(message).toString();
        String commitHash = commitUtilService.calculateCommitHash(data);
        commit.setParentHash(oldCommitHash);
        commit.setMessage(message);
        commit.setHash(commitHash);
        commit.setCommitter(committer);
        File file = FileUtils.createObjectFile(commitHash,repoPath);
        try {
            FileUtils.writeFile(file.getAbsolutePath(), data);
            // 将refs/head/main中的commitHash替换为最新的hash
            String headPath = repoPath + File.separator + ".minigit" + File.separator + "HEAD";
            FileUtils.writeFileNoAppend(repoPath + File.separator + ".minigit" + File.separator + "refs" +
                    File.separator + "heads" + File.separator + FileUtils.readLine(headPath), commitHash);
            // 删除缓冲区的内容
            FileUtils.deleteFileOrDirectory(repoPath + File.separator + ".minigit" + File.separator + "INDEX");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return commit;
    }

    public void back(String oldCommitHash,String repoPath){
        Map<String, String> currentCommitTreeMap = backService.getCurrentCommitTree(repoPath);
        Map<String, String> oldCommitTreeMap = backService.getOldCommitTree(oldCommitHash,repoPath);
        Map<String, String> fileMap = backService.getFileMap(repoPath);
        Map<String, String> deleteMap = backService.getDeleteMap(currentCommitTreeMap, oldCommitTreeMap);
        Map<String, String> createMap = backService.getCreateMap(currentCommitTreeMap, oldCommitTreeMap);
        for (String path : deleteMap.keySet()) {
            if(fileMap.containsKey(path)){
                new File(path).delete();
            }
        }
        for (String path: createMap.keySet()){
            if(!fileMap.containsKey(path)){
                try {
                    FileUtils.createFile(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (String path : oldCommitTreeMap.keySet()) {
            String currentHash = Sha1Utils.calculateFileSha1(new File(path));
            String oldhash = oldCommitTreeMap.get(path);
            if(oldhash != currentHash){
                File objectFile = FileUtils.getObjectFile(oldhash,repoPath);
                try {
                    String content = FileUtils.readFile(objectFile.getAbsolutePath());
                    FileUtils.writeFileNoAppend(path, content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // 将refs/head/main中的commitHash替换为最新的hash
        try {
            String headPath = repoPath + File.separator + ".minigit" + File.separator + "HEAD";
                    FileUtils.writeFileNoAppend(repoPath + File.separator + ".minigit" + File.separator + "refs"
                    + File.separator + "heads" +File.separator + FileUtils.readLine(headPath), oldCommitHash);
            // 删除缓冲区的内容
            FileUtils.deleteFileOrDirectory(repoPath + File.separator + ".minigit" + File.separator + "INDEX");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void push(String repoPath,  String userName, String repoName, String branchName){
        uploadService.uploadFile(repoPath, userName, repoName, branchName);
    }

    public void pull(String path, String repoPath) throws SftpException {
        uploadService.downloadDirectory(path, repoPath);
    }

    /**
     * Integer: 0:未追踪  1:已缓存  2:未修改  3:已修改  4:已删除
     * @param repoPath
     * @param oldCommitHash
     * @return
     */
    public Map<String, Integer> getFileStatus(String repoPath, String oldCommitHash){

        String oldTreeHeadHash = FileUtils.getTreeHeadHash(oldCommitHash,repoPath);
        Map<String, String> fileMap = new HashMap<>();
        Map<String, String> indexMap = new HashMap<>();
        Map<String, String> commitTreeMap = new HashMap<>();
        commitUtilService.createIndexTree(indexMap,repoPath);
        commitUtilService.createOldCommitTree(oldTreeHeadHash, commitTreeMap,repoPath);
        commitUtilService.createFileTree(fileMap,new File(repoPath));
        Map<String, Integer> resultMap = new HashMap<>();

        // 如果commitTreeMap为null，说明没有提交，先将所有的文件标记为未追踪
        for (String filePath : fileMap.keySet()) {
            resultMap.put(filePath.replace(repoPath + File.separator, ""), 0);
        }

        if(commitTreeMap == null){
            return resultMap;
        }

        // 对每个缓冲区的文件和commitTree中的文件做比较
        for (String filePath : commitTreeMap.keySet()) {
            String oldHash = commitTreeMap.get(filePath);
            File file = new File(filePath);
            if(!file.exists()){
                // 文件已经删除，标记为4
                resultMap.put(filePath.replace(repoPath + File.separator, ""), 4);
                continue;
            }
            String newHash = calculateFileSha1(file);
            if(oldHash.equals(newHash)){
                // 文件未修改，标记为2
                resultMap.put(filePath.replace(repoPath + File.separator, ""), 2);
            }else{
                // 文件已经修改，标记为3
                resultMap.put(filePath.replace(repoPath + File.separator, ""), 3);
            }
        }
        // 然后将index中的文件标记为已缓存即可
        for (String filePath : indexMap.keySet()) {
            resultMap.put(filePath.replace(repoPath + File.separator, ""), 1);
        }
        return resultMap;
    }
}
