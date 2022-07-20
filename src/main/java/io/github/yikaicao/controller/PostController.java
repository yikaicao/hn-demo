package io.github.yikaicao.controller;

import io.github.yikaicao.entity.PostBO;
import io.github.yikaicao.service.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {
    @Autowired
    private PostRepository postRepository;

    @GetMapping("/posts")
    public Page<PostBO> getRecentPostsPageable() {
        return postRepository.findAll(PageRequest.of(0, 10));
    }

    @DeleteMapping("/post/{id}")
    public void deletePost(@PathVariable String id) {
        postRepository.deleteById(id);
    }

}
