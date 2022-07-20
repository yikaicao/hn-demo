package io.github.yikaicao;

import io.github.yikaicao.schedule.ScheduledTasks;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScheduledTaskTest {
    @Autowired
    ScheduledTasks scheduledTasks;

    @Test
    public void givenUrl_whenInvokingHackerNews_thenNoExceptionIsThrown() {
        scheduledTasks.pullHackerNews();
    }
}
