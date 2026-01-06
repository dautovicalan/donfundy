package hr.algebra.donfundy.repository;

import hr.algebra.donfundy.domain.Campaign;
import hr.algebra.donfundy.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatus(Status status);
}
