package edu.cit.galo.wellcheck.features.slots;

import edu.cit.galo.wellcheck.domain.entities.Slot;
import edu.cit.galo.wellcheck.domain.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByCounselorId(Long counselorId);
    List<Slot> findByCounselorIdAndStatus(Long counselorId, SlotStatus status);
}