package com.collo.dreadlocks.crm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientNoteRepository extends JpaRepository<ClientNote, Integer> {

    List<ClientNote> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);
}
