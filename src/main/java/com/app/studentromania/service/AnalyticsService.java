package com.app.studentromania.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Analytics;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.repo.AnalyticsRepo;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import com.app.studentromania.util.Utilities;

@Service
public class AnalyticsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

	@Autowired
	private LogUtils logUtils;

	@Autowired
	public AnalyticsService() {
//		logUtils.logMessage(LOGGER, "StatisticService initialized");
	}

	@Autowired
	private AnalyticsRepo analyticsRepo;

	@Autowired
	private FacultyDAO facultyDAO;

	public ResponseDTO saveAnalytics() {
		Analytics analytics = new Analytics();
		List<Analytics> statistics = analyticsRepo.findAll();
		if (CollectionUtils.isEmpty(statistics)) {
			analyticsRepo.save(analytics);
		}
		ErrorsEnum excelDocumentErrors = createAnalyticsExcelDocument();
		if (excelDocumentErrors != ErrorsEnum.NO_ERROR) {
			return ResponseDTO.createErrorResponse(excelDocumentErrors);
		}
		logUtils.logMessage(LOGGER, "Analytics document and excel created!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO resetAnalytics() {
		List<Analytics> analytics = analyticsRepo.findAll();
		if (CollectionUtils.isEmpty(analytics)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ANALYTICS_NOT_FOUND);
		}
		Analytics statistic = analytics.get(0);
		statistic.setHomePage(0);
		statistic.setSearch(0);
		statistic.setReviewIntention(0);
		statistic.setReviewAdded(0);
		analyticsRepo.save(statistic);
		logUtils.logMessage(LOGGER, "Analytics fields reset!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO resetAnalyticsExcel() {
		ErrorsEnum excelDocumentErrors = resetAnalyticsExcelDocument();
		if (excelDocumentErrors != ErrorsEnum.NO_ERROR) {
			return ResponseDTO.createErrorResponse(excelDocumentErrors);
		}
		logUtils.logMessage(LOGGER, "Analytics excel reset!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO increaseHomePageStatistic(boolean isMobile) {
		List<Analytics> analyticsList = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(analyticsList)) {
			Analytics analytics = analyticsList.get(0);
			long homePageStatistic = analytics.getHomePage();
			homePageStatistic++;
			analytics.setHomePage(homePageStatistic);
			analyticsRepo.save(analytics);
			logUtils.logMessage(LOGGER, "Analytics statistic 'homepage' increased");
		}
		ErrorsEnum excelDocumentErrors = updateHomepageAnalyticsExcelDocument(isMobile);
		if (excelDocumentErrors != ErrorsEnum.NO_ERROR) {
			logUtils.logMessage(LOGGER, "Could not update analytics excel document");
			return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
		}

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public void increaseSearchStatistic() {
		List<Analytics> analyticsList = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(analyticsList)) {
			Analytics analytics = analyticsList.get(0);
			long searchStatistic = analytics.getSearch();
			searchStatistic++;
			analytics.setSearch(searchStatistic);
			analyticsRepo.save(analytics);
			logUtils.logMessage(LOGGER, "Analytics statistic 'search' increased");
		}
	}

	public ResponseDTO increaseReviewStatistic(int step) {
		List<Analytics> analyticsList = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(analyticsList)) {
			Analytics analytics = analyticsList.get(0);
			switch (step) {
			case 0: {
				analytics.setReviewIntention(analytics.getReviewIntention() + 1);
				updateReviewAnalyticsExcelDocument(0);
			}
				break;
			case 1: {
				analytics.setReviewReachedFirstStep(analytics.getReviewReachedFirstStep() + 1);
				updateReviewAnalyticsExcelDocument(1);
			}
				break;
			case 2: {
				analytics.setReviewReachedSecondStep(analytics.getReviewReachedSecondStep() + 1);
				updateReviewAnalyticsExcelDocument(2);
			}
				break;
			case 3: {
				analytics.setReviewReachedThirdStep(analytics.getReviewReachedThirdStep() + 1);
				updateReviewAnalyticsExcelDocument(3);
			}
				break;
			case 4: {
				analytics.setReviewAdded(analytics.getReviewAdded() + 1);
				updateReviewAnalyticsExcelDocument(4);
			}
				break;
			}
			analyticsRepo.save(analytics);
			logUtils.logMessage(LOGGER, "Analytics review statistic increased");
		}

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO increaseFacultyViewCountStatistic(String facultyId) {
		Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(facultyId);
		if (facultyOpt.isPresent()) {
			int newViewsCount = facultyOpt.get().getViewsCount() + 1;
			facultyOpt.get().setViewsCount(newViewsCount);
			logUtils.logMessage(LOGGER,
					"Analytics faculty view count increased for faculty: " + facultyOpt.get().getFacultyId());
			facultyDAO.updateFaculty(facultyOpt.get());
		}

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO getAnalytics() {
		List<Analytics> statistics = analyticsRepo.findAll();
		if (CollectionUtils.isEmpty(statistics)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ANALYTICS_NOT_FOUND);
		}
		Analytics analytics = statistics.get(0);
		JSONObject response = new JSONObject(analytics);

		return ResponseDTO.createSuccessResponse(response);
	}

	private ErrorsEnum createAnalyticsExcelDocument() {
		File file = new File(Constants.ANALYTICS_EXCEL_PATH);
		if (file.exists()) {
			return ErrorsEnum.NO_ERROR;
		}

		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("General Statistics");

		Row header = sheet.createRow(0);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontHeightInPoints((short) 14);
		font.setBold(true);
		headerStyle.setFont(font);

		Cell headerCell = header.createCell(0);
		headerCell.setCellValue("Date");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(1);
		headerCell.setCellValue("Visits");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(2);
		headerCell.setCellValue("Unique Visits");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(3);
		headerCell.setCellValue("Web Visits");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(4);
		headerCell.setCellValue("Phone Visits");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(5);
		headerCell.setCellValue("Review Intentions");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(6);
		headerCell.setCellValue("Review Reached First Step");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(7);
		headerCell.setCellValue("Review Reached Second Step");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(8);
		headerCell.setCellValue("Review Reached Third Step");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(9);
		headerCell.setCellValue("Review Added");
		headerCell.setCellStyle(headerStyle);

		String fileLocation = Constants.ANALYTICS_EXCEL_PATH;

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(fileLocation);

			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		} catch (IOException e) {
			return ErrorsEnum.ANALYTICS_EXCEL_DOCUMENT_ERROR;
		}
		return ErrorsEnum.NO_ERROR;
	}

	private ErrorsEnum resetAnalyticsExcelDocument() {
		File file = new File(Constants.ANALYTICS_EXCEL_PATH);
		if (!file.exists()) {
			return ErrorsEnum.ANALYTICS_EXCEL_DOCUMENT_NOT_FOUND;
		}
		try {
			FileInputStream inputStream = new FileInputStream(file);
			Workbook workbook = new XSSFWorkbook(inputStream);

			Sheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) {
				for (Cell cell : row) {
					cell.setCellValue(0);
				}
			}

			inputStream.close();

			FileOutputStream outputStream = new FileOutputStream(Constants.ANALYTICS_EXCEL_PATH);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return ErrorsEnum.ANALYTICS_EXCEL_DOCUMENT_ERROR;
		}
		return ErrorsEnum.NO_ERROR;
	}

	private ErrorsEnum updateHomepageAnalyticsExcelDocument(boolean isMobile) {
		File file = new File(Constants.ANALYTICS_EXCEL_PATH);
		if (!file.exists()) {
			return ErrorsEnum.ANALYTICS_EXCEL_DOCUMENT_NOT_FOUND;
		}
		try {
			FileInputStream inputStream = new FileInputStream(file);
			Workbook workbook = new XSSFWorkbook(inputStream);

			Sheet sheet = workbook.getSheetAt(0);

			Date date = new Date();
			String formattedDate = Utilities.getFormattedDate(date);
			int lastRowNumber = sheet.getLastRowNum();
//			if (lastRowNumber > 0) {
			Row lastRow = sheet.getRow(lastRowNumber);
			String lastDate = lastRow.getCell(0).getStringCellValue();
			if (lastDate.equals(formattedDate)) {
				// visits
				double newVisitsValue = lastRow.getCell(1).getNumericCellValue() + 1;
				lastRow.getCell(1).setCellValue(newVisitsValue);
				// mobile or web
				if (!isMobile) {
					double newWebValue = lastRow.getCell(3).getNumericCellValue() + 1;
					lastRow.getCell(3).setCellValue(newWebValue);
				} else {
					double newMobileValue = lastRow.getCell(4).getNumericCellValue() + 1;
					lastRow.getCell(4).setCellValue(newMobileValue);
				}
			} else {
				Row newRow = createRowAndCells(sheet, lastRowNumber);
				newRow.getCell(0).setCellValue(formattedDate);
				// visits
				newRow.getCell(1).setCellValue(1);
				// unique visits
				newRow.getCell(2).setCellValue(1);
				// mobile or web
				if (!isMobile) {
					newRow.getCell(3).setCellValue(1);
					newRow.getCell(4).setCellValue(0);
				} else {
					newRow.getCell(3).setCellValue(0);
					newRow.getCell(4).setCellValue(1);
				}
			}
//			}

			inputStream.close();

			FileOutputStream outputStream = new FileOutputStream(Constants.ANALYTICS_EXCEL_PATH);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return ErrorsEnum.ANALYTICS_EXCEL_DOCUMENT_ERROR;
		}
		return ErrorsEnum.NO_ERROR;
	}

	private ErrorsEnum updateReviewAnalyticsExcelDocument(int step) {
		File file = new File(Constants.ANALYTICS_EXCEL_PATH);
		if (!file.exists()) {
			return ErrorsEnum.ANALYTICS_EXCEL_DOCUMENT_NOT_FOUND;
		}
		try {
			FileInputStream inputStream = new FileInputStream(file);
			Workbook workbook = new XSSFWorkbook(inputStream);

			Sheet sheet = workbook.getSheetAt(0);

			Date date = new Date();
			String formattedDate = Utilities.getFormattedDate(date);
			int lastRowNumber = sheet.getLastRowNum();
			Row lastRow = sheet.getRow(lastRowNumber);
			String lastDate = lastRow.getCell(0).getStringCellValue();
			if (lastDate.equals(formattedDate)) {
				switch (step) {
				case 0: {
					double newReviewIntentionsValue = lastRow.getCell(5).getNumericCellValue() + 1;
					lastRow.getCell(5).setCellValue(newReviewIntentionsValue);
				}
					break;
				case 1: {
					double newReviewReachedFirstStepValue = lastRow.getCell(6).getNumericCellValue() + 1;
					lastRow.getCell(6).setCellValue(newReviewReachedFirstStepValue);
				}
					break;
				case 2: {
					double newReviewReachedSecondStepValue = lastRow.getCell(7).getNumericCellValue() + 1;
					lastRow.getCell(7).setCellValue(newReviewReachedSecondStepValue);
				}
					break;
				case 3: {
					double newReviewReachedThirdStepValue = lastRow.getCell(8).getNumericCellValue() + 1;
					lastRow.getCell(8).setCellValue(newReviewReachedThirdStepValue);
				}
					break;
				case 4: {
					double newReviewAddedValue = lastRow.getCell(9).getNumericCellValue() + 1;
					lastRow.getCell(9).setCellValue(newReviewAddedValue);
				}
					break;
				}
			} else {
				Row newRow = createRowAndCells(sheet, lastRowNumber);
				newRow.getCell(0).setCellValue(formattedDate);
				switch (step) {
				case 0: {
					newRow.getCell(5).setCellValue(1);
				}
					break;
				case 1: {
					newRow.getCell(6).setCellValue(1);
				}
					break;
				case 2: {
					newRow.getCell(7).setCellValue(1);
				}
					break;
				case 3: {
					newRow.getCell(8).setCellValue(1);
				}
					break;
				case 4: {
					newRow.getCell(9).setCellValue(1);
				}
					break;
				}
			}

			inputStream.close();

			FileOutputStream outputStream = new FileOutputStream(Constants.ANALYTICS_EXCEL_PATH);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return ErrorsEnum.ANALYTICS_EXCEL_DOCUMENT_ERROR;
		}
		return ErrorsEnum.NO_ERROR;
	}

	private Row createRowAndCells(Sheet sheet, int lastRowNumber) {
		Row row = sheet.createRow(++lastRowNumber);
		row.createCell(0);
		row.createCell(1);
		row.createCell(2);
		row.createCell(3);
		row.createCell(4);
		row.createCell(5);
		row.createCell(6);
		row.createCell(7);
		row.createCell(8);
		row.createCell(9);

		return row;
	}

}
