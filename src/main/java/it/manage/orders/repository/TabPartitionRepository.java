package it.manage.orders.repository;

import it.manage.orders.entity.TabPartitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TabPartitionRepository extends JpaRepository<TabPartitions, String> {
}
