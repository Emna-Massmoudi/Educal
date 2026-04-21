package com.elearning.elearning_api.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class LeconResponse {
    private Long id;
    private Integer ordre;
    private String titre;
    private String description;
    private String contenuHtml;
    private Long coursId;
    private String coursTitre;
    private String pdfUrl;
    private List<LeconRessourceResponse> ressources;
}
