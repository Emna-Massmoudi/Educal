package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.SousCategorieRequest;
import com.elearning.elearning_api.dto.response.SousCategorieResponse;
import com.elearning.elearning_api.entity.Categorie;
import com.elearning.elearning_api.entity.SousCategorie;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.CategorieRepository;
import com.elearning.elearning_api.repository.SousCategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SousCategorieService {

    private final SousCategorieRepository sousCategorieRepository;
    private final CategorieRepository categorieRepository;

    public SousCategorieResponse create(SousCategorieRequest request) {
        Categorie categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new ResourceNotFoundException("Categorie not found: " + request.getCategorieId()));

        SousCategorie sousCategorie = new SousCategorie();
        sousCategorie.setNom(request.getNom());
        sousCategorie.setDescription(request.getDescription());
        sousCategorie.setCategorie(categorie);

        return toResponse(sousCategorieRepository.save(sousCategorie));
    }

    public SousCategorieResponse update(Long id, SousCategorieRequest request) {
        SousCategorie existing = sousCategorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SousCategorie not found: " + id));

        existing.setNom(request.getNom());
        existing.setDescription(request.getDescription());

        if (request.getCategorieId() != null) {
            Categorie categorie = categorieRepository.findById(request.getCategorieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categorie not found: " + request.getCategorieId()));
            existing.setCategorie(categorie);
        }

        return toResponse(sousCategorieRepository.save(existing));
    }

    public void delete(Long id) {
        if (!sousCategorieRepository.existsById(id))
            throw new ResourceNotFoundException("SousCategorie not found: " + id);
        sousCategorieRepository.deleteById(id);
    }

    public SousCategorieResponse getById(Long id) {
        return sousCategorieRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("SousCategorie not found: " + id));
    }

    public List<SousCategorieResponse> getAll() {
        return sousCategorieRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    public List<SousCategorieResponse> getByCategorie(Long categorieId) {
        return sousCategorieRepository.findByCategorieId(categorieId)
                .stream().map(this::toResponse).toList();
    }

    private SousCategorieResponse toResponse(SousCategorie sousCategorie) {
        SousCategorieResponse response = new SousCategorieResponse();
        response.setId(sousCategorie.getId());
        response.setNom(sousCategorie.getNom());
        response.setDescription(sousCategorie.getDescription());
        response.setCategorieNom(sousCategorie.getCategorie().getNom());
        return response;
    }
}