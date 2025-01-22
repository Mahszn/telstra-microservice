package au.com.telstra.simcardactivator;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import au.com.telstra.simcardactivator.records.ActivationRecord;

import java.util.Optional;

@Repository
public interface ActivationRecordRepository extends JpaRepository<ActivationRecord, Long> {
    Optional<ActivationRecord> findByIccid(String iccid);
}
