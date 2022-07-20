package io.github.yikaicao.service;

import io.github.yikaicao.entity.PostBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PostRepository extends MongoRepository<PostBO, String> {

    Page<PostBO> findAll(Pageable pageable);

    @Query(value = "{'_id':?0}", delete = true)
    void deleteById(String id);
}

