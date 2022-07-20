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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

@Component
@Slf4j
public class ScheduledTasks {

    private static final String URL_HACKER_NEWS = "https://hn.algolia.com/api/v1/search_by_date?query=java";
    private static final int WAIT_TIME_HACKER_NEWS = 3;

    private final HttpClient client = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpGet httpGet = new HttpGet(URL_HACKER_NEWS);
    private final PostRepository postRepository;

    @Resource
    private ThreadPoolExecutor executor;

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
            pullWithFutures();
        } catch (Exception e) {
            log.error("exception during scheduled hacker news task, {}", e.getMessage());
        }
    }

    private void pullWithFutures() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return postRepository.findAll(PageRequest.of(0, 1)).iterator().next();
            } catch (NoSuchElementException e) {
                log.warn("no post yet");
                return null;
            }
        }, executor).thenAcceptAsync(lastPost -> {
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
                log.error("error when invoking hacker news, {}", e.getMessage());
            }
        }, executor);
        completableFuture.get(WAIT_TIME_HACKER_NEWS, TimeUnit.SECONDS);
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
