package com.minigit.controller;

import com.jcraft.jsch.SftpException;
import com.minigit.common.R;
import com.minigit.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/{userName}/{repoName}")
public class FileController {
    @Autowired
    private UploadService uploadService;

    @GetMapping("/blob/{branchName}/**")
    public R<String> getFilePath(@PathVariable String userName, @PathVariable String repoName, @PathVariable String branchName,
             HttpServletRequest request) throws SftpException {
        String requestURI = request.getRequestURI();
        String filepath = requestURI.replaceFirst("/blob/", "/");
        String content = uploadService.readFile(uploadService.REMOTE_REPO_PATH +  filepath);
        return R.success(content);
    }

    @GetMapping("/tree/{branchName}/**")
    public R<Map<String, String>> getDirPath(@PathVariable String userName, @PathVariable String repoName, @PathVariable String branchName,
                                             HttpServletRequest request) throws SftpException {
        String requestURI = request.getRequestURI();
        String filepath = requestURI.replaceFirst("/tree/", "/");
        System.out.println(filepath);
        Map<String, String> map = uploadService.readDir(uploadService.REMOTE_REPO_PATH + filepath);
        return R.success(map);
    }
}
