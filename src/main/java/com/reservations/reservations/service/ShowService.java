package com.reservations.reservations.service;

import com.reservations.reservations.model.Show;
import com.reservations.reservations.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShowService {
    @Autowired
    private ShowRepository repository;

    public List<Show> getAllShows() {
        return (List<Show>) repository.findAll();
    }

    public Optional<Show> getShowById(Long id) {
        return repository.findById(id);
    }

    public void addShow(Show show) {
        repository.save(show);
    }

    public void updateShow(Long id, Show updatedShow) {
        repository.save(updatedShow);
    }

    public void deleteShow(Long id) {
        repository.deleteById(id);
    }

    public List<Show> searchByTagKeyword(String keyword) {
        return repository.findByTagKeyword(keyword);
    }

    public List<Show> excludeByTag(String tag) {
        return repository.findByTagNot(tag);
    }

    public Page<Show> getFilteredShows(String titleKeyword, String tagKeyword, String sortField, String sortDir, int page, int size) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        if (titleKeyword != null && !titleKeyword.isEmpty() && tagKeyword != null && !tagKeyword.isEmpty()) {
            return repository.findByTitleContainingIgnoreCaseAndTagsTagContainingIgnoreCase(titleKeyword, tagKeyword, pageable);
        } else if (tagKeyword != null && !tagKeyword.isEmpty()) {
            return repository.findByTagsTagContainingIgnoreCase(tagKeyword, pageable);
        } else if (titleKeyword != null && !titleKeyword.isEmpty()) {
            return repository.findByTitleContainingIgnoreCase(titleKeyword, pageable);
        } else {
            return repository.findAll(pageable);
        }
    }


}