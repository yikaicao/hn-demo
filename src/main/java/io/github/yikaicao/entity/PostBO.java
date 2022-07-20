package io.github.yikaicao.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document(collection = "post")
@Data
public class PostBO {

    @Id
    public String _id;

    @JsonAlias("story_id")
    @Indexed(unique = true)
    public String storyId;
    @JsonAlias("created_at")
    @JsonDeserialize(converter = StringToTimeConverter.class)
    public LocalDateTime createdAt;
    public String title;
    public String url;
    @JsonAlias("story_url")
    public String storyUrl;
}

class StringToTimeConverter extends StdConverter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String s) {
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
    }
}

