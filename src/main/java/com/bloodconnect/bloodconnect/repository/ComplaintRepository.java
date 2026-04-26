package com.bloodconnect.bloodconnect.repository;

import com.bloodconnect.bloodconnect.model.Complaint;
import com.bloodconnect.bloodconnect.model.ComplaintCategory;
import com.bloodconnect.bloodconnect.model.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findBySubmittedBy(String email);
    List<Complaint> findByStatus(ComplaintStatus status);
    List<Complaint> findByCategory(ComplaintCategory category);
}
