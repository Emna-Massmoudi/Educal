package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.CategorieRequest;
import com.elearning.elearning_api.dto.response.CategorieResponse;
import com.elearning.elearning_api.entity.Categorie;
import com.elearning.elearning_api.exception.AlreadyExistsException;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;

    public CategorieResponse create(CategorieRequest request) {
        if (categorieRepository.existsByNom(request.getNom()))
            throw new AlreadyExistsException("Categorie already exists: " + request.getNom());

        Categorie categorie = new Categorie();
        categorie.setNom(request.getNom());
        categorie.setDescription(request.getDescription());

        return toResponse(categorieRepository.save(categorie));
    }

    public CategorieResponse update(Long id, CategorieRequest request) {
        Categorie existing = categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie not found: " + id));

        existing.setNom(request.getNom());
        existing.setDescription(request.getDescription());

        return toResponse(categorieRepository.save(existing));
    }

    public void delete(Long id) {
        if (!categorieRepository.existsById(id))
            throw new ResourceNotFoundException("Categorie not found: " + id);
        categorieRepository.deleteById(id);
    }

    public CategorieResponse getById(Long id) {
        return categorieRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie not found: " + id));
    }

    public List<CategorieResponse> getAll() {
        return categorieRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    public List<CategorieResponse> getPublicCategories() {
        return getAll();
    }

    private CategorieResponse toResponse(Categorie categorie) {
        CategorieResponse response = new CategorieResponse();
        response.setId(categorie.getId());
        response.setNom(categorie.getNom());
        response.setDescription(categorie.getDescription());
        response.setSousCategorieCount(categorie.getSousCategories() == null ? 0 : categorie.getSousCategories().size());
        return response;
    }
}
