package io.github.yikaicao;

import io.github.yikaicao.entity.PostBO;
import io.github.yikaicao.service.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UniquenessTest {

    @Autowired
    PostRepository postRepository;

    @Test
    public void givenUniqueIndex_whenInsertingDupe_thenExceptionIsThrown() {
        PostBO aPost = new PostBO();
        aPost.setStoryId("123");
        PostBO bPost = new PostBO();
        bPost.setStoryId("123");
        PostBO cPost = new PostBO();
        cPost.setStoryId("456");
        List<PostBO> posts = List.of(aPost, bPost, cPost);

        postRepository.insert(aPost);
        assertThrows(DuplicateKeyException.class, () -> {
            postRepository.insert(bPost);
        });
        postRepository.insert(cPost);

        for (PostBO p : posts) {
            if (p._id != null) {
                postRepository.deleteById(p.get_id());
            }
        }
    }
}
