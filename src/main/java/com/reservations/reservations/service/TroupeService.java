package com.reservations.reservations.service;


import com.reservations.reservations.model.Troupe;
import com.reservations.reservations.repository.TroupeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TroupeService {
    @Autowired
    private TroupeRepository troupeRepository;

    public List<Troupe> getAllTroupes() {
        return new ArrayList<>(troupeRepository.findAll());
    }

    public Troupe getTroupe(long id) {
        return troupeRepository.findById(id).orElse(null);
    }

    public void addTroupe(Troupe troupe) {
        troupeRepository.save(troupe);
    }

    public void updateTroupe(long id, Troupe troupe) {
        troupeRepository.save(troupe);
    }

    public void deleteTroupe(long id) {
        troupeRepository.deleteById(id);
    }
}
