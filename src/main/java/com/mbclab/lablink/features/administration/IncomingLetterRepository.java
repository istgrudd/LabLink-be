package com.mbclab.lablink.features.administration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomingLetterRepository extends JpaRepository<IncomingLetter, String> {
    List<IncomingLetter> findBySenderContainingIgnoreCase(String sender);
}
