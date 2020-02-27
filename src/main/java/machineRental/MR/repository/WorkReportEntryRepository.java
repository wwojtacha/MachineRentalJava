package machineRental.MR.repository;

import java.time.LocalDate;
import java.util.List;
import machineRental.MR.machine.model.Machine;
import machineRental.MR.operator.model.Operator;
import machineRental.MR.workDocumentEntry.model.WorkReportEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkReportEntryRepository extends JpaRepository<WorkReportEntry, Long> {

  List<WorkReportEntry> findAllByWorkDocument_Id(String workDocumentId);

  void deleteByWorkDocument_Id(String workDocumentId);

  List<WorkReportEntry> findByWorkDocument_OperatorAndWorkDocument_DateEquals(Operator operator, LocalDate date);
  List<WorkReportEntry> findByWorkDocument_MachineAndWorkDocument_DateEquals(Machine machine, LocalDate date);


}