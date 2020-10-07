package machineRental.MR.price.hour;

import java.time.LocalDate;
import java.util.List;
import machineRental.MR.exception.NotFoundException;
import machineRental.MR.price.PriceChecker;
import machineRental.MR.price.PriceType;
import machineRental.MR.price.distance.service.DateChecker;
import machineRental.MR.price.hour.exception.OverlappingDatesException;
import machineRental.MR.price.hour.model.HourPrice;
import machineRental.MR.repository.HourPriceRepository;
import machineRental.MR.repository.WorkReportEntryRepository;
import machineRental.MR.workDocument.model.WorkDocument;
import machineRental.MR.workDocumentEntry.WorkCode;
import machineRental.MR.workDocumentEntry.model.WorkReportEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HourPriceChecker implements PriceChecker {

  @Autowired
  private WorkReportEntryRepository workReportEntryRepository;

  @Autowired
  private HourPriceRepository hourPriceRepository;

  private DateChecker dateChecker = new DateChecker();

  @Override
  public boolean isPriceAlreadyUsed(Long priceId) {
    return workReportEntryRepository.existsByHourPrice_Id(priceId);
  }

  public void checkEditability(Long id, HourPrice currentHourPrice, HourPrice editedHourPrice) {
    List<WorkReportEntry> workReportEntires = workReportEntryRepository.findAllByHourPrice_Id(id);

//    workReportEntries found by HourPrice id all concern the same machine
    String machineInternalId = "";
    if (!workReportEntires.isEmpty()) {
      WorkDocument workDocument = workReportEntires.iterator().next().getWorkDocument();
      machineInternalId = workDocument.getMachine().getInternalId();
    }
    checkHourPriceUniquness(currentHourPrice, editedHourPrice, machineInternalId);

    checkHourPriceMatch(editedHourPrice, workReportEntires);
//    no exception thrown upto now so edited price can be updated
  }

  private void checkHourPriceUniquness(HourPrice currentHourPrice, HourPrice editedHourPrice, String machineInternalId) {
    //    uniqueness of editedHourPrice needs to be checked ony against existing hour prices for a given machine. All prices for different machines will be unique by definition.
    List<HourPrice> hourPricesByMachine = hourPriceRepository.findByMachineInternalIdEquals(machineInternalId);

    for (HourPrice hourPriceByMachine : hourPricesByMachine) {
//      useless to check if current price to be edited is unique against itself
      if (currentHourPrice == hourPriceByMachine) {
        continue;
      }

      if (!isPriceUnique(editedHourPrice, hourPriceByMachine)) {
        throw new OverlappingDatesException(
            String.format("Hour price for a given work code (%s), machine number (%s), price type (%s) cannot overlap in time with the same entry.",
                editedHourPrice.getWorkCode(), editedHourPrice.getMachineInternalId(), editedHourPrice.getPriceType().toString()));
      }
    }
  }

  public boolean isPriceUnique(HourPrice newPrice, HourPrice price) {
    return newPrice.getWorkCode() != price.getWorkCode()
        || !newPrice.getMachineInternalId().equals(price.getMachineInternalId())
        || newPrice.getPriceType() != price.getPriceType()
        || !newPrice.getProjectCode().equals(price.getProjectCode())
        || !dateChecker.areDatesOverlapping(newPrice, price);
  }

  private void checkHourPriceMatch(HourPrice editedHourPrice, List<WorkReportEntry> workReportEntires) {
    for (WorkReportEntry workReportEntry : workReportEntires) {

      if (!isPriceMatching(workReportEntry, editedHourPrice)) {
        WorkDocument workDocument = workReportEntry.getWorkDocument();
        String workDocumentNumber = workDocument.getId();
        WorkCode workCode = workReportEntry.getWorkCode();
        PriceType priceType = workReportEntry.getHourPrice().getPriceType();
        String estimateProjectCode = workReportEntry.getEstimatePosition().getCostCode().getProjectCode();
        LocalDate date = workDocument.getDate();

        throw new NotFoundException(String.format("Edited hour price does not match work document %s entry parameters: %s, %s, %s, %s.",
            workDocumentNumber,
            workCode,
            priceType,
            estimateProjectCode,
            date));
      }
    }
  }

  private boolean isPriceMatching(WorkReportEntry workReportEntry, HourPrice editedHourPrice) {

    WorkDocument workDocument = workReportEntry.getWorkDocument();

    LocalDate date = workDocument.getDate();
    String machineInternalId = workDocument.getMachine().getInternalId();

    return workReportEntry.getWorkCode() == editedHourPrice.getWorkCode()
        && machineInternalId.equals(editedHourPrice.getMachineInternalId())
        && workReportEntry.getHourPrice().getPriceType() == editedHourPrice.getPriceType()
        && workReportEntry.getEstimatePosition().getCostCode().getProjectCode().equals(editedHourPrice.getProjectCode())
        && (date.isAfter(editedHourPrice.getStartDate()) || date.isEqual(editedHourPrice.getStartDate()))
        && (date.isBefore(editedHourPrice.getEndDate()) || date.isEqual(editedHourPrice.getEndDate()));
  }
}
