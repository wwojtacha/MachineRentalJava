package machineRental.MR.reports;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import machineRental.MR.price.hour.exception.IncorrectDateException;
import machineRental.MR.workDocumentEntry.model.WorkReportEntry;
import machineRental.MR.workDocumentEntry.service.WorkReportEntryService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

  @Autowired
  private WorkReportEntryService workReportEntryService;

  public void generateExcelReport(LocalDate startDate, LocalDate endDate) {

    List<WorkReportEntry> workReportEntries = workReportEntryService.getWorkReportEntriesBetweenDates(startDate, endDate);

    String directory = System.getProperty("user.home") + "/Downloads";
    File file = new File(directory, "work-report-entries.xlsx");

    try {

      FileOutputStream fileOutputStream = new FileOutputStream(file);
      XSSFWorkbook worbook = new XSSFWorkbook();
      Sheet sheet = worbook.createSheet("WorkReportEntries");

      writeHeaderLine(sheet);

      writeDataLines(workReportEntries, sheet);

      worbook.write(fileOutputStream);

      System.setProperty("java.awt.headless", "false");
      Desktop desktop = Desktop.getDesktop();
      desktop.open(file);
    } catch (IOException e) {
      e.printStackTrace();
    }


  }

  private void writeHeaderLine(Sheet sheet) {
    Row headerRow = sheet.createRow(0);

    Cell headerCell = headerRow.createCell(0);
    headerCell.setCellValue("Document number");

    headerCell = headerRow.createCell(1);
    headerCell.setCellValue("Document type");

    headerCell = headerRow.createCell(2);
    headerCell.setCellValue("Date");

    headerCell = headerRow.createCell(3);
    headerCell.setCellValue("Machine id");

    headerCell = headerRow.createCell(4);
    headerCell.setCellValue("Operator name");

    headerCell = headerRow.createCell(5);
    headerCell.setCellValue("Delegation");

    headerCell = headerRow.createCell(6);
    headerCell.setCellValue("Invoice number");

    headerCell = headerRow.createCell(7);
    headerCell.setCellValue("Work code");

    headerCell = headerRow.createCell(8);
    headerCell.setCellValue("Start hour");

    headerCell = headerRow.createCell(9);
    headerCell.setCellValue("End hour");

    headerCell = headerRow.createCell(10);
    headerCell.setCellValue("Place of work");

    headerCell = headerRow.createCell(11);
    headerCell.setCellValue("Type of work");

    headerCell = headerRow.createCell(12);
    headerCell.setCellValue("Work quantity");

    headerCell = headerRow.createCell(13);
    headerCell.setCellValue("Measure unit");

    headerCell = headerRow.createCell(14);
    headerCell.setCellValue("Price type");

    headerCell = headerRow.createCell(15);
    headerCell.setCellValue("Price");

    headerCell = headerRow.createCell(16);
    headerCell.setCellValue("Estimate name");

    headerCell = headerRow.createCell(17);
    headerCell.setCellValue("Estimate cost code");

    headerCell = headerRow.createCell(18);
    headerCell.setCellValue("Cost code (sell)");

    headerCell = headerRow.createCell(19);
    headerCell.setCellValue("Accepted by");

//    for (int i = 0; i <= 10; i++) {
//      Cell headerCell = headerRow.createCell(i);
//      headerCell.setCellValue();
//    }
  }

  private void writeDataLines(List<WorkReportEntry> workReportEntries, Sheet sheet) {

    int rowNumber = 1;

    for (WorkReportEntry workReportEntry : workReportEntries) {
      Row row = sheet.createRow(rowNumber);

      Cell rowCell = row.createCell(0);
      rowCell.setCellValue(workReportEntry.getWorkDocument().getId());

      rowCell = row.createCell(1);
      rowCell.setCellValue(workReportEntry.getWorkDocument().getDocumentType().name());

      rowCell = row.createCell(2);
      rowCell.setCellValue(convertToDateViaSqlDate(workReportEntry.getWorkDocument().getDate()));

      rowCell = row.createCell(3);
      rowCell.setCellValue(workReportEntry.getWorkDocument().getMachine().getInternalId());

      rowCell = row.createCell(4);
      rowCell.setCellValue(workReportEntry.getWorkDocument().getOperator().getName());

      rowCell = row.createCell(5);
      rowCell.setCellValue(workReportEntry.getWorkDocument().getDelegation());

      rowCell = row.createCell(6);
      rowCell.setCellValue(workReportEntry.getWorkDocument().getInvoiceNumber());

      rowCell = row.createCell(7);
      rowCell.setCellValue(workReportEntry.getWorkCode().toString());

      rowCell = row.createCell(8);
      rowCell.setCellValue(workReportEntry.getStartHour().toString());

      rowCell = row.createCell(9);
      rowCell.setCellValue(workReportEntry.getEndHour().toString());

      rowCell = row.createCell(10);
      rowCell.setCellValue(workReportEntry.getPlaceOfWork());

      rowCell = row.createCell(11);
      rowCell.setCellValue(workReportEntry.getTypeOfWork());

      rowCell = row.createCell(12);
      rowCell.setCellValue(workReportEntry.getWorkQuantity());

      rowCell = row.createCell(13);
      rowCell.setCellValue(workReportEntry.getMeasureUnit());

      rowCell = row.createCell(14);
      rowCell.setCellValue(workReportEntry.getHourPrice().getPriceType().name());

      rowCell = row.createCell(15);
      rowCell.setCellValue(workReportEntry.getHourPrice().getPrice().doubleValue());

      rowCell = row.createCell(16);
      rowCell.setCellValue(workReportEntry.getEstimatePosition().getName());

      rowCell = row.createCell(17);
      rowCell.setCellValue(workReportEntry.getEstimatePosition().getCostCode().getFullCode());

      rowCell = row.createCell(18);
      rowCell.setCellValue(workReportEntry.getCostCode().getFullCode());

      rowCell = row.createCell(19);
      rowCell.setCellValue(workReportEntry.getAcceptingPerson().getName());

      rowNumber++;

    }
  }

  public Date convertToDateViaSqlDate(LocalDate dateToConvert) {
    return java.sql.Date.valueOf(dateToConvert);
  }

  public List<WorkReportEntry> generateReport(LocalDate startDate, LocalDate endDate) {
    if (endDate.isBefore(startDate)) {
      throw new IncorrectDateException("End date must be equal or greater than start date.");
    }

    return workReportEntryService.getWorkReportEntriesBetweenDates(startDate, endDate);
  }

  public void export(HttpServletResponse response, LocalDate startDate, LocalDate endDate) throws IOException {

//    FileOutputStream fileOutputStream = new FileOutputStream(file);
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("WorkReportEntries");

    writeHeaderLine(sheet);

    List<WorkReportEntry> workReportEntries = workReportEntryService.getWorkReportEntriesBetweenDates(startDate, endDate);

    writeDataLines(workReportEntries, sheet);

    ServletOutputStream outputStream = response.getOutputStream();
    workbook.write(outputStream);
//    workbook.close();

    outputStream.close();

  }
}

