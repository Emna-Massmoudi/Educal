package com.elearning.elearning_api.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class QuestionResponse {
    private Long   id;
    private String enonce;
    private Integer point;
    private Long   evaluationId;
    private String evaluationTitre;
    private List<ChoixResponse> choix;  
}