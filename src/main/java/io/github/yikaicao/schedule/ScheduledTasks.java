package io.github.yikaicao.schedule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.yikaicao.entity.HackerNewsResponseDTO;
import io.github.yikaicao.entity.PostBO;
import io.github.yikaicao.service.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ScheduledTasks {

    private final String URL_HACKER_NEWS = "https://hn.algolia.com/api/v1/search_by_date?query=java";
    private final HttpClient client = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpGet httpGet = new HttpGet(URL_HACKER_NEWS);
    private final PostRepository postRepository;

    public ScheduledTasks(PostRepository postRepository) {
        this.postRepository = postRepository;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
    }

    //@Scheduled(cron = "0 * * * * ?", zone = "GMT+8") // every minute
    @Scheduled(cron = "0 0 * * * ?", zone = "GMT+8") // every hour
    //@Scheduled(fixedRate = 1000) // every second
    public void pullHackerNews() {

        try {
            log.info("checking last post status");

            CompletableFuture<Void> cfHackerNews = CompletableFuture.supplyAsync(() -> {
                // get last post info to optimize query to hacker news
                PostBO lastPost = null;
                Iterable<PostBO> iterable = postRepository.findAll(PageRequest.of(0, 1));
                if (iterable.iterator().hasNext()) {
                    lastPost = iterable.iterator().next();
                }
                String numericFilters = "";
                if (lastPost != null) {
                    numericFilters += "&numericFilters=created_at_i>" + lastPost.getCreatedAt().toEpochSecond(ZoneOffset.UTC);
                }

                try {
                    HttpResponse res = client.execute(httpGet);
                    HttpEntity httpEntity = res.getEntity();
                    if (httpEntity != null) {
                        log.info("received result");
                        String result = EntityUtils.toString(httpEntity, "utf-8");
                        HackerNewsResponseDTO dto = objectMapper.readValue(result, HackerNewsResponseDTO.class);
                        log.info("received {} posts(hits)", dto.getHits().size());
                        int savedCount = 0;
                        for (PostBO p : dto.getHits()) {
                            try {
                                postRepository.insert(p);
                                savedCount++;
                            } catch (DuplicateKeyException e) {
                                log.warn("duplicated storyId from HN");
                            }
                        }
                        log.info("saved count = {}", savedCount);
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                return null;
            });
        } catch (Exception e) {
            log.error("exception when processing hacker news {}", e.getMessage());
        }
    }
}
