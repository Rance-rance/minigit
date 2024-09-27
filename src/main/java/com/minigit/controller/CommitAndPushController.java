package com.minigit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jcraft.jsch.SftpException;
import com.minigit.common.R;
import com.minigit.entity.Branch;
import com.minigit.entity.Commit;
import com.minigit.entity.Repo;
import com.minigit.entity.User;
import com.minigit.entityService.BranchService;
import com.minigit.entityService.CommitService;
import com.minigit.entityService.RepoService;
import com.minigit.entityService.UserService;
import com.minigit.service.BackService;
import com.minigit.service.CommitUtilService;
import com.minigit.service.GitService;
import com.minigit.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/{userName}/{repoName}/do/{branchName}")
public class CommitAndPushController {
    @Autowired
    private CommitService commitService;
    @Autowired
    private UserService userService;
    @Autowired
    private RepoService repoService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private CommitUtilService commitUtilService;
    @Autowired
    private BackService backService;
    @Autowired
    private GitService gitService;

    @GetMapping("/status")
    public R<Map<String, Integer>> status(@PathVariable String repoName, HttpSession session){
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<>();
        Long authorId = (Long) session.getAttribute("user");
        queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getName,repoName);
        Repo repo = repoService.getOne(queryWrapper);
        String oldCommitHash = FileUtils.getCurrentCommitHash(repo.getPath());
        Map<String, Integer> fileStatus = gitService.getFileStatus(repo.getPath(), oldCommitHash);
        return R.success(fileStatus);
    }

    @PostMapping("/add")
    public R<String> add(@PathVariable String repoName,@PathVariable String branchName,
                         @RequestParam List<String> filePaths, HttpSession session){
        List<File> files = new ArrayList<>();
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<>();
        Long authorId = (Long) session.getAttribute("user");
        queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getName,repoName);
        Repo repo = repoService.getOne(queryWrapper);
        for (String filePath : filePaths) {
            files.add(new File(repo.getPath() + File.separator + filePath));
        }
        gitService.add(files, repo.getPath());
        return R.success("add成功！");
    }

    /**
     * commit
     * @return
     */
    @PostMapping("/commit")
    public R<Commit> commit(@PathVariable String repoName,@PathVariable String branchName,
                            @RequestParam String message, HttpSession session) throws NoSuchAlgorithmException {
        Long committerId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, committerId);
        User user = userService.getOne(queryWrapper);
        String committer = user.getAccountName();

        LambdaQueryWrapper<Repo> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Repo::getAuthorId, committerId).eq(Repo::getName, repoName);
        Repo repo = repoService.getOne(queryWrapper1);
        Commit commit = gitService.commit(message,committer,repo.getPath());
        LambdaQueryWrapper<Branch> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Branch::getRepoId,repo.getId()).eq(Branch::getName, branchName);
        Branch branch1 = branchService.getOne(queryWrapper2);

        commit.setBranchId(branch1.getId());
        commitService.save(commit);
        return R.success(commit);
    }

    @PostMapping("/back")
    public R<String> back(@PathVariable String repoName,@PathVariable String branchName,
                            @RequestBody Commit commit, HttpSession session) {
        Long committerId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<Repo> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Repo::getAuthorId, committerId).eq(Repo::getName, repoName);
        Repo repo = repoService.getOne(queryWrapper1);

        gitService.back(commit.getHash(), repo.getPath());
        return R.success("回退成功！");
    }

    @GetMapping("/push")
    public R<String> push(@PathVariable String userName,@PathVariable String repoName,
                          @PathVariable String branchName, HttpSession session){
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<>();
        Long authorId = (Long) session.getAttribute("user");
        queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getName,repoName);
        Repo repo = repoService.getOne(queryWrapper);
        gitService.push(repo.getPath(), userName, repoName, branchName);
        return R.success("推送成功！");
    }

    @PostMapping("/pull")
    public R<String> pull(@PathVariable String userName,@PathVariable String repoName,
                          @PathVariable String branchName, @RequestBody Repo repo, HttpSession session) throws SftpException {
        // 在拉取一个仓库时，必须init一个仓库，默认main分支，暂时不允许选择分支

        gitService.pull(userName + "/" + repoName + "/" + branchName, repo.getPath());
        return R.success("拉取成功！");
    }

}
