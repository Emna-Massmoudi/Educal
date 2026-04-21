package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.LeconRequest;
import com.elearning.elearning_api.dto.request.LeconRessourceRequest;
import com.elearning.elearning_api.dto.response.LeconRessourceResponse;
import com.elearning.elearning_api.dto.response.LeconPreviewResponse;
import com.elearning.elearning_api.dto.response.LeconResponse;
import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.entity.Lecon;
import com.elearning.elearning_api.entity.Ressources;
import com.elearning.elearning_api.enums.EtatCours;
import com.elearning.elearning_api.enums.TypeRessources;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.CoursRepository;
import com.elearning.elearning_api.repository.LeconRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeconService {

    private final LeconRepository leconRepository;
    private final CoursRepository coursRepository;
    private final QuizAccessService quizAccessService;

    @Transactional
    public LeconResponse create(LeconRequest request) {
        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours not found: " + request.getCoursId()));
        quizAccessService.assertCanManageCourse(cours);

        Lecon lecon = new Lecon();
        lecon.setTitre(request.getTitre());
        lecon.setDescription(request.getDescription());
        lecon.setContenuHtml(request.getContenuHtml());
        lecon.setOrdre(request.getOrdre());
        lecon.setCours(cours);
        syncResources(lecon, request);

        return toResponse(leconRepository.save(lecon));
    }

    @Transactional
    public LeconResponse update(Long id, LeconRequest request) {
        Lecon existing = leconRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecon not found: " + id));
        quizAccessService.assertCanManageCourse(existing.getCours());

        existing.setTitre(request.getTitre());
        existing.setDescription(request.getDescription());
        existing.setContenuHtml(request.getContenuHtml());
        existing.setOrdre(request.getOrdre());
        syncResources(existing, request);

        return toResponse(leconRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Lecon lecon = leconRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecon not found: " + id));
        quizAccessService.assertCanManageCourse(lecon.getCours());
        leconRepository.delete(lecon);
    }

    @Transactional(readOnly = true)
    public LeconResponse getById(Long id) {
        Lecon lecon = leconRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecon not found: " + id));
        quizAccessService.assertCanReadCourseContent(lecon.getCours());
        return toResponse(lecon);
    }

    @Transactional(readOnly = true)
    public List<LeconResponse> getByCours(Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours not found: " + coursId));
        quizAccessService.assertCanReadCourseContent(cours);

        return leconRepository.findByCoursIdOrderByOrdreAsc(coursId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeconPreviewResponse> getPublicPreviewByCours(Long coursId) {
        coursRepository.findByIdAndEtatPublication(coursId, EtatCours.PUBLIE)
                .orElseThrow(() -> new ResourceNotFoundException("Published course not found: " + coursId));

        return leconRepository.findByCoursIdOrderByOrdreAsc(coursId)
                .stream()
                .map(this::toPreviewResponse)
                .toList();
    }

    private LeconResponse toResponse(Lecon lecon) {
        LeconResponse response = new LeconResponse();
        response.setId(lecon.getId());
        response.setTitre(lecon.getTitre());
        response.setDescription(lecon.getDescription());
        response.setContenuHtml(lecon.getContenuHtml());
        response.setOrdre(lecon.getOrdre());
        response.setCoursId(lecon.getCours().getId());
        response.setCoursTitre(lecon.getCours().getTitre());
        response.setRessources(mapResources(lecon.getRessources()));
        response.setPdfUrl(response.getRessources().stream()
                .filter(resource -> resource.getType() == TypeRessources.DOCUMENT)
                .map(LeconRessourceResponse::getUrl)
                .findFirst()
                .orElse(null));
        return response;
    }

    private LeconPreviewResponse toPreviewResponse(Lecon lecon) {
        LeconPreviewResponse response = new LeconPreviewResponse();
        response.setId(lecon.getId());
        response.setOrdre(lecon.getOrdre());
        response.setTitre(lecon.getTitre());
        response.setDescription(lecon.getDescription());
        response.setCoursId(lecon.getCours().getId());
        return response;
    }

    private void syncResources(Lecon lecon, LeconRequest request) {
        List<Ressources> resources = new ArrayList<>();

        if (request.getRessources() != null) {
            request.getRessources().stream()
                    .filter(item -> item != null && item.getUrl() != null && !item.getUrl().isBlank())
                    .forEach(item -> resources.add(toEntity(item, lecon)));
        }

        if ((request.getRessources() == null || request.getRessources().isEmpty())
                && request.getPdfUrl() != null
                && !request.getPdfUrl().isBlank()) {
            LeconRessourceRequest pdfResource = new LeconRessourceRequest();
            pdfResource.setNom("Support de lecon");
            pdfResource.setType(TypeRessources.DOCUMENT);
            pdfResource.setUrl(request.getPdfUrl());
            resources.add(toEntity(pdfResource, lecon));
        }

        lecon.setRessources(resources);
    }

    private Ressources toEntity(LeconRessourceRequest request, Lecon lecon) {
        Ressources resource = new Ressources();
        resource.setNom(request.getNom().trim());
        resource.setType(request.getType());
        resource.setUrl(request.getUrl().trim());
        resource.setLecon(lecon);
        return resource;
    }

    private List<LeconRessourceResponse> mapResources(List<Ressources> resources) {
        if (resources == null) {
            return List.of();
        }

        return resources.stream()
                .map(resource -> {
                    LeconRessourceResponse response = new LeconRessourceResponse();
                    response.setId(resource.getId());
                    response.setNom(resource.getNom());
                    response.setType(resource.getType());
                    response.setUrl(resource.getUrl());
                    return response;
                })
                .toList();
    }
}
