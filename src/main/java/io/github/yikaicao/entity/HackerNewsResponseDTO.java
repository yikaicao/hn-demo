package io.github.yikaicao.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;

@Data
public class HackerNewsResponseDTO {

    public List<PostBO> hits;

    public int nbHits;
    public int page;
    public int nbPages;
    public int hitsPerPage;
    public boolean exhaustiveNbHits;
    public boolean exhaustiveTypo;
    public String query;
    public String param;
    public int processingTimeMS;
}
