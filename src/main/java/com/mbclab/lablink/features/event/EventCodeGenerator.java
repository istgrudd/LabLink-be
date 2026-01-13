package com.mbclab.lablink.features.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Generate Event Code dengan format: EVT-0001
 */
@Service
@RequiredArgsConstructor
public class EventCodeGenerator {

    private final EventRepository eventRepository;

    public String generate() {
        long count = eventRepository.count() + 1;
        return String.format("EVT-%04d", count);
    }
}
