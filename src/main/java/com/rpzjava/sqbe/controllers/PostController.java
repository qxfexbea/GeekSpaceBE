package com.rpzjava.sqbe.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rpzjava.sqbe.entities.Post;
import com.rpzjava.sqbe.tools.EditPostType;
import com.rpzjava.sqbe.tools.EntityStatus;
import com.rpzjava.sqbe.tools.ExecuteResult;
import com.rpzjava.sqbe.daos.IPostDao;
import com.rpzjava.sqbe.exceptions.PostDataNotCompleteException;
import com.rpzjava.sqbe.services.EditPostService;
import com.rpzjava.sqbe.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

    private final IPostDao iPostDao;
    private final EditPostService editPostService;

    private static final int PAGE_SIZE = 10;

    public PostController(
            IPostDao iPostDao, EditPostService editPostService) {
        this.iPostDao = iPostDao;
        this.editPostService = editPostService;
    }

    @PutMapping("/")
    public Object updatePost(@RequestBody JSONObject reqBody) {
        try {
            ExecuteResult rewriteResult = editPostService.updateEdit(reqBody, EditPostType.POST);
            if (!rewriteResult.getStatus()) {
                ResultUtils.error("修改帖子失败！" + rewriteResult.getPayload().toString());
            }
        } catch (PostDataNotCompleteException e) {
            return ResultUtils.error("修改帖子失败，提交的数据字段不完整");
        }
        return ResultUtils.success("修改帖子成功！");
    }

    @PostMapping("/")
    public Object savePost(@RequestBody JSONObject reqBody) {

        try {
            ExecuteResult newPostResult = editPostService.newEdit(reqBody, EditPostType.POST);
            if (!newPostResult.getStatus()) {
                ResultUtils.error("发帖失败！" + newPostResult.getPayload().toString());
            }
        } catch (PostDataNotCompleteException e) {
            return ResultUtils.error("发帖失败，提交的数据字段不完整");
        }

        return ResultUtils.success("发帖成功");
    }

    @GetMapping("/latest") //按分页（每页10个）所有帖子数据信息
    public Object getLatestPost(@RequestParam(name = "page") String page) {
        Pageable pageable = PageRequest.of(Integer.parseInt(page), PAGE_SIZE, Sort.by("createTime").descending());
        return ResultUtils.success(
                JSON.toJSON(iPostDao.findAllByStatus(EntityStatus.NORMAL, pageable).getContent()),
                "获取帖子成功！"
        );
    }

    @GetMapping("/byTag/{tagName}")
    public Object getPostWithParams(@PathVariable String tagName) {
        return ResultUtils.success(iPostDao.dragPostsByTag(tagName), "获取 tag: " + tagName + "标签下的帖子成功！");
    }

    @GetMapping("/{postId}")
    public Object getPostById(@PathVariable String postId) {
        Long pid = Long.parseLong(postId);
        Optional<Post> findingPost = iPostDao.findById(pid);
        return findingPost.map(post -> ResultUtils.success(JSON.toJSON(post),
                "获取 id: " + postId + "的帖子成功！")).orElseGet(
                () -> ResultUtils.error("获取 id: " + postId + "的帖子不存在！"));
    }

    @GetMapping("/pagination")
    public Object getPagination() {
        long pagination = iPostDao.count() / PAGE_SIZE;
        if (iPostDao.count() % PAGE_SIZE != 0) {
            pagination += 1;
        }
        JSONObject result = new JSONObject();
        result.put("pagination", pagination);
        return ResultUtils.success(result, "获取页数成功！");
    }

}
