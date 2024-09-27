package com.minigit.service;

import com.jcraft.jsch.*;
import com.minigit.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

@Slf4j
@Service
public class UploadService {
    private Session session;
    private ChannelSftp channelSftp;
    @Value("${ftp.address}")
    private String ftpAddress;
    @Value("${ftp.username}")
    private String ftpUserName;
    @Value("${ftp.password}")
    private String ftpPassword;
    @Value("${ftp.port}")
    private int ftpPort;
    @Value("${remote-repo-path}")
    public String REMOTE_REPO_PATH;

    public ChannelSftp getSFTPClient(){
        return channelSftp;
    }

    /**
     * 获取sftp连接
     */
    public ChannelSftp createSFTPClient() {

        //开始时间  用于计时
        long startTime = System.currentTimeMillis();
        JSch jsch = new JSch();// 创建JSch对象
        Channel channel = null;
        try {
            //根据用户名，主机ip，端口获取一个Session对象
            session = jsch.getSession(ftpUserName, ftpAddress, ftpPort);
            jsch.addIdentity("./centos.pem");
            session.setPassword(ftpPassword); // 设置密码
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config); // 为Session对象设置properties
            //session.setTimeout(timeout); // 设置timeout时间
            session.connect(); // 通过Session建立链接
            channel = session.openChannel("sftp"); // 打开SFTP通道
            channel.connect(); // 建立SFTP通道的连接
            long endTime = System.currentTimeMillis();
            log.info("连接sftp成功耗时" + (endTime - startTime) + "毫秒");
            System.out.println(session);
            System.out.println(channel);
            System.out.println("获取连接成功！");
            return (ChannelSftp) channel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @Description //关闭链接资源
     * @return void
     **/
    public void close() {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        log.info("关闭连接资源");
    }

    /**
     * @Description //上传文件
     * @return boolean
     **/
    public boolean uploadFile(String repoPath, String userName, String repoName, String branchName){
        try{
            //建立连接
            if (channelSftp == null || !channelSftp.isConnected()) {
                channelSftp=createSFTPClient();
            }
            String branchPath = REMOTE_REPO_PATH + "/"+ userName + "/" + repoName +
                    "/" + branchName;
            deleteDirectory(branchPath, channelSftp);
            createDir(branchPath);
            String commitHash = FileUtils.getCurrentCommitHash(repoPath);
            String treeHeadHash = FileUtils.getTreeHeadHash(commitHash,repoPath);
            uploadCommitFile(channelSftp, treeHeadHash, repoPath, branchPath);
        }catch(Exception e){
            e.printStackTrace();
            close();
        }
        return false;
    }

    /**
     * 在远程服务器上递归创建目录
     * @param dirPath 目录路径，格式为"dir1/dir2/dir3"
     */
    public void createDir(String dirPath) throws SftpException {
        if (channelSftp == null || !channelSftp.isConnected()) {
            channelSftp=createSFTPClient();
        }
        String[] dirs = dirPath.split("/");
        String currentDir = "";
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            currentDir += "/" + dir;
            try {
                channelSftp.cd(currentDir);
            } catch (SftpException e) {
                channelSftp.mkdir(currentDir);
                channelSftp.cd(currentDir);
            }
        }
    }


    public void uploadCommitFile(ChannelSftp sftp, String hash, String repoPath, String branchPath) throws SftpException, IOException {
        File file = FileUtils.getObjectFile(hash, repoPath);
        String content = FileUtils.readFile(file.getAbsolutePath());
        String[] lines = content.split(System.lineSeparator());
        for (String line : lines) {
            if(line.equals("")) continue;
            String[] s = line.split("\t");
            if(s[0].equals("blob")){
                channelSftp.put(s[1], branchPath + "/" +
                        s[1].replace(repoPath + File.separator, "")
                        .replace(File.separator, "/"));
            }else{
                channelSftp.mkdir(branchPath + "/" +
                        s[1].replace(repoPath + File.separator, "")
                                .replace(File.separator, "/"));
                uploadCommitFile(sftp, s[2], repoPath, branchPath);
            }
        }
    }

    public void checkRemoteDirectory(String remoteDir, ChannelSftp channelSftp) throws SftpException {
        try {
            channelSftp.cd(remoteDir);
        } catch (SftpException e) {
            channelSftp.mkdir(remoteDir);
        }
    }


    /**
     * 上传一个文件或者整个目录
     * @param sftp
     * @param localPath
     * @param remotePath
     * @throws Exception
     */
    public void uploadFile(ChannelSftp sftp, String localPath, String remotePath) throws Exception {
        File localFile = new File(localPath);
        if (localFile.isFile()) {
            sftp.put(localPath, remotePath);
        } else {
            File[] files = localFile.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile()) {
                    sftp.put(file.getAbsolutePath(), remotePath + "/" + fileName);
                } else {
                    sftp.mkdir(remotePath + "/" + fileName);
                    uploadFile(sftp, file.getAbsolutePath(), remotePath + "/" + fileName);
                }
            }
        }
    }


    public boolean deleteDirectory(String dirPath, ChannelSftp sftp) throws SftpException {
        try{
            sftp.stat(dirPath);
        }catch (Exception e){
            return false;
        }
        Vector<ChannelSftp.LsEntry> fileList = sftp.ls(dirPath);
        for (ChannelSftp.LsEntry entry : fileList) {
            String fileName = entry.getFilename();
            if (".".equals(fileName) || "..".equals(fileName)) {
                continue;
            }
            String path = dirPath + "/" + fileName;
            if (entry.getAttrs().isDir()) {
                deleteDirectory(path, sftp);
            } else {
                sftp.rm(path);
            }
        }
        sftp.rmdir(dirPath);
        return true;
    }

    public void downloadDirectory(String path, String localPath) throws SftpException {
        //建立连接
        if (channelSftp == null || !channelSftp.isConnected()) {
            channelSftp=createSFTPClient();
        }

        String remotePath = REMOTE_REPO_PATH + "/" + path;
        // 递归下载目录下的所有文件
        Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls(remotePath);
        File localFile = new File(localPath);
        if (!localFile.exists()) {
            localFile.mkdirs();
        }
        for (ChannelSftp.LsEntry entry : fileList) {
            String entryName = entry.getFilename();
            if (!entryName.equals(".") && !entryName.equals("..")) {
                String remoteFilePath = path + "/" + entryName;
                String localFilePath = localPath + "/" + entryName;

                if (entry.getAttrs().isDir()) {
                    downloadDirectory(remoteFilePath, localFilePath);
                } else {
                    channelSftp.get(REMOTE_REPO_PATH +"/" +  remoteFilePath, localFilePath);
                }
            }
        }

    }


    public String readFile(String remoteFilePath) throws SftpException {
        //建立连接
        if (channelSftp == null || !channelSftp.isConnected()) {
            channelSftp=createSFTPClient();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        channelSftp.get(remoteFilePath, outputStream);
        return outputStream.toString();
    }

    public Map<String, String> readDir(String remoteDirPath) throws SftpException {
        //建立连接
        if (channelSftp == null || !channelSftp.isConnected()) {
            channelSftp=createSFTPClient();
        }
        Map<String,String> map = new HashMap<>();
        Vector<ChannelSftp.LsEntry> fileEntries = channelSftp.ls(remoteDirPath);
        for (ChannelSftp.LsEntry fileEntry : fileEntries) {
            String fileName = fileEntry.getFilename();
            if (!fileName.equals(".") && !fileName.equals("..")){
                map.put(fileName, fileEntry.getAttrs().isDir() ? "tree" : "blob");
            }
        }
        return map;
    }

    public void copyDirectory(String repoPath,String sourceDir, String destinationDir) throws Exception {
        //建立连接
        if (channelSftp == null || !channelSftp.isConnected()) {
            channelSftp=createSFTPClient();
        }
        this.createDir(destinationDir);
        channelSftp.cd(sourceDir);
        Vector<ChannelSftp.LsEntry> fileEntries = channelSftp.ls(".");
        // 列出源目录中的所有文件和子目录
        for (ChannelSftp.LsEntry fileEntry : fileEntries) {
            String entryName = fileEntry.getFilename();
            if (entryName.equals(".") || entryName.equals("..")) {
                continue;
            }
            String srcFilePath = sourceDir + "/" + entryName;
            String destFilePath = destinationDir + "/" + entryName;
            if (fileEntry.getAttrs().isDir()) {
                // 如果是子目录，则递归复制目录
                channelSftp.mkdir(destFilePath);
                copyDirectory(repoPath, srcFilePath, destFilePath);
            } else {

                // 构造本地临时目录的文件路径
                String localTempFilePath = repoPath + File.separator + ".minigit" + File.separator + entryName;

                // 下载源文件到本地临时目录
                channelSftp.get(srcFilePath, localTempFilePath);

                // 上传本地临时目录的文件到目标位置
                channelSftp.put(localTempFilePath, destFilePath);

                // 删除本地临时目录的文件
                File localTempFile = new File(localTempFilePath);
                localTempFile.delete();
            }
        }
    }
}
