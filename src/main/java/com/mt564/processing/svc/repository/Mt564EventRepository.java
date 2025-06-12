package com.mt564.processing.svc.repository;

import com.mt564.processing.svc.model.entity.Mt564Event;
import com.mt564.processing.svc.model.entity.Mt564EventId;
import org.springframework.data.jpa.repository.JpaRepository;
public interface Mt564EventRepository extends JpaRepository<Mt564Event, Mt564EventId> {}
