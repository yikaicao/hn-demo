package io.github.yikaicao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HackerNewsHttpConfig {

    @Bean(name = "DEFAULT_PAGE_SIZE")
    public int defaultPageSize() {
        return 10;
    }

}
