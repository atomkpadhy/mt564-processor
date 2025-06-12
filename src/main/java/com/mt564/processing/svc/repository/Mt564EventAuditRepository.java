package com.mt564.processing.svc.repository;

import com.mt564.processing.svc.model.entity.Mt564EventAudit;
import com.mt564.processing.svc.model.entity.Mt564EventAuditId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface Mt564EventAuditRepository extends JpaRepository<Mt564EventAudit, Mt564EventAuditId> {
    List<Mt564EventAudit> findByUpdatedAtBetween(LocalDateTime from, LocalDateTime to);
}