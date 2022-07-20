package io.github.yikaicao;

import io.github.yikaicao.schedule.ScheduledTasks;
import io.github.yikaicao.service.PostRepository;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class PressureTest {

    @Autowired
    ScheduledTasks scheduledTasks;

    @MockBean
    HttpClient httpClient;

    @MockBean
    PostRepository postRepository;

    @Test
    public void givenHttpClient_whenThrowException_thenExecutorShouldHandle() throws IOException {
        Mockito.when(httpClient.execute(any())).thenThrow(new IOException());
        scheduledTasks.pullHackerNews();
    }

}
