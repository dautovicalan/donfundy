package hr.algebra.donfundy.repository;

import hr.algebra.donfundy.domain.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByCampaignId(Long campaignId);
    List<Donation> findByDonorId(Long donorId);
}
