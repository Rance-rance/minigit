package com.minigit.util;


import java.io.*;

public  class FileUtils {
    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException
     */
    public static void createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if(!file.exists()){
            file.getParentFile().mkdirs(); // 创建父目录
            file.createNewFile(); // 创建文件
        }
    }

    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException
     */
    public static String readFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !line.equals("")) {
            content.append(line);
            content.append(System.lineSeparator());
        }
        reader.close();
        return content.toString();
    }

    /**
     * 写入文件内容（追加）,如果文件不存在会自动创建文件，但是不能自动创建目录
     * @param filePath 文件路径
     * @param content 内容字符串
     * @throws IOException
     */
    public static void writeFile(String filePath, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,true));
        writer.write(content + System.lineSeparator());
        writer.close();
    }

    /**
     * 写入文件内容(覆盖）
     * @param filePath 文件路径
     * @param content 内容字符串
     * @throws IOException
     */
    public static void writeFileNoAppend(String filePath, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,false));
        writer.write(content);
        writer.close();
    }
    /**
     * 读取文件的一行并去除换行符
     */
    public static String readLine(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();
        reader.close();
        if (line != null) {
            line = line.replace(File.separator, "");
        }
        return line;
    }


    /**
     * 复制文件
     * @param srcFile 源文件路径
     * @param destFile 目标文件路径
     * @throws IOException
     */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        try (InputStream in = new FileInputStream(srcFile);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    /**
     * 删除文件或目录
     * @param path 文件或目录路径
     * @throws IOException
     */
    public static void deleteFileOrDirectory(String path) throws IOException {
        File file = new File(path);
        if(!file.exists()) return;
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                deleteFileOrDirectory(subFile.getPath());
            }
        }
        file.delete();
    }

    /**
     * 写入object文件
     */
    public static void writeObject(String path, String hash, String repoPath) {
        String folder = hash.substring(0, 2);
        String filename = hash.substring(2);
        String folderPath = repoPath + File.separator + ".minigit" + File.separator + "objects"
                + File.separator + folder;
        String filePath = folderPath + File.separator + filename;
        File objectFile = new File(filePath);
        if (!objectFile.exists()) {
            File folderFile = new File(folderPath);
            folderFile.mkdirs();
            try {
                objectFile.createNewFile();
                File file = new File(path);
                if (file.isDirectory()) {

                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static File createObjectFile(String hash, String repoPath) {
        String folder = hash.substring(0, 2);
        String filename = hash.substring(2);
        String folderPath = repoPath + File.separator + ".minigit" + File.separator + "objects"
                + File.separator + folder;
        String filePath = folderPath + File.separator + filename;
        File objectFile = new File(filePath);
        if (!objectFile.exists()) {
            File folderFile = new File(folderPath);
            folderFile.mkdirs();
            try {
                objectFile.createNewFile();
            } catch (IOException e) {
                System.out.println("创建文件失败！");
                throw new RuntimeException(e);
            }
        }else{
            return null;
        }
        return objectFile;
    }

    public static File getObjectFile(String hash, String repoPath) {
        String folder = hash.substring(0, 2);
        String filename = hash.substring(2);
        String folderPath = repoPath + File.separator + ".minigit" + File.separator + "objects"
                + File.separator + folder;
        String filePath = folderPath + File.separator + filename;
        File objectFile = new File(filePath);
        if (!objectFile.exists()) {
            System.out.println("目标文件不存在！");
            return null;
        }
        return objectFile;
    }

    /**
     * 获取当前的commitHash
     * @return
     */
    public static String getCurrentCommitHash(String repoPath){

        try {
            // 到head文件中找到当前分支的路径
            String branchName = readLine(repoPath + File.separator + ".minigit" + File.separator + "HEAD");
            // 向分支中找到parentHash
            File file = new File(repoPath + File.separator + ".minigit" + File.separator + "refs"
                     + File.separator + "heads" +  File.separator +  branchName);
            if(!file.exists()){
                System.out.println("当前分支还没有commit！");
                return null;
            }
            String parentCommitHash = readLine(file.getAbsolutePath());
            return parentCommitHash;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前commit的treeHeadHash
     * @param currentCommitHash
     * @return
     */
    public static String getTreeHeadHash(String currentCommitHash, String repoPath){
        try {
            if (currentCommitHash == null) {
                System.out.println("currentCommitHash不存在！");
                return null;
            }
            // 通过parentCommitHash找到commit的objects文件
            File treeHeadFile = getObjectFile(currentCommitHash, repoPath);
            // commit的objects文件的第一行保存着treeHeadHash
            if(!treeHeadFile.exists() || treeHeadFile == null){
                System.out.println("treeHeadFile不存在！");
                return null;
            }
            String treeHeadHash = readLine(treeHeadFile.getAbsolutePath());
            return treeHeadHash;
        } catch (IOException e) {
            System.out.println("HEAD文件中保存着当前分支name，不应该抛出异常！");
            System.out.println(e);
        }
        System.out.println("commitHash或者treeHeadHash出错！");
        return null;
    }


}

