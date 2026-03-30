package it.manage.orders.repository;

import it.manage.orders.entity.ManageOrdersDeadLetter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeadLetterRepository extends JpaRepository<ManageOrdersDeadLetter, Long> {

    List<ManageOrdersDeadLetter> findByProcessedFalseAndRetryCountLessThan(int maxRetries, Pageable pageable);
}
