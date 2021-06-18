package com.app.studentromania.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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

import com.app.studentromania.dto.ResponseDTO;
import com.app.studentromania.enumtype.ErrorsEnum;
import com.app.studentromania.model.Analytics;
import com.app.studentromania.repo.AnalyticsRepo;
import com.app.studentromania.util.Constants;
import com.app.studentromania.util.LogUtils;
import com.app.studentromania.util.Utilities;

@Service
public class AnalyticsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

	@Autowired
	public AnalyticsService() {
		LogUtils.logMessage(LOGGER, "StatisticService initialized");
	}

	@Autowired
	private AnalyticsRepo analyticsRepo;

	public ResponseDTO saveAnalytics() {
		Analytics analytics = new Analytics();
		List<Analytics> statistics = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(statistics)) {
			return ResponseDTO.createErrorResponse(ErrorsEnum.ANALYTICS_DOCUMENT_ALREADY_EXISTS);
		}
		ErrorsEnum excelDocumentErrors = createAnalyticsExcelDocument();
		if (excelDocumentErrors != ErrorsEnum.NO_ERROR) {
			return ResponseDTO.createErrorResponse(excelDocumentErrors);
		}
		analyticsRepo.save(analytics);
		LogUtils.logMessage(LOGGER, "Analytics document and excel created!");

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
		LogUtils.logMessage(LOGGER, "Analytics fields reset!");

		return ResponseDTO.createSuccessResponse(ResponseDTO.JSON_SUCCESS);
	}

	public ResponseDTO resetAnalyticsExcel() {
		ErrorsEnum excelDocumentErrors = resetAnalyticsExcelDocument();
		if (excelDocumentErrors != ErrorsEnum.NO_ERROR) {
			return ResponseDTO.createErrorResponse(excelDocumentErrors);
		}
		LogUtils.logMessage(LOGGER, "Analytics excel reset!");

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
			LogUtils.logMessage(LOGGER, "Analytics statistic 'homepage' increased");
		}
		ErrorsEnum excelDocumentErrors = updateAnalyticsExcelDocument(isMobile);
		if (excelDocumentErrors != ErrorsEnum.NO_ERROR) {
			LogUtils.logMessage(LOGGER, "Could not update analytics excel document");
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
			LogUtils.logMessage(LOGGER, "Analytics statistic 'search' increased");
		}
	}

	public void increaseReviewIntentionStatistic() {
		List<Analytics> analyticsList = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(analyticsList)) {
			Analytics analytics = analyticsList.get(0);
			long reviewIntentionStatistic = analytics.getReviewIntention();
			reviewIntentionStatistic++;
			analytics.setReviewIntention(reviewIntentionStatistic);
			analyticsRepo.save(analytics);
			LogUtils.logMessage(LOGGER, "Analytics statistic 'review intentions' increased");
		}
	}

	public void increaseReviewAddedStatistic() {
		List<Analytics> analyticsList = analyticsRepo.findAll();
		if (!CollectionUtils.isEmpty(analyticsList)) {
			Analytics analytics = analyticsList.get(0);
			long reviewAdded = analytics.getReviewAdded();
			reviewAdded++;
			analytics.setReviewAdded(reviewAdded);
			analyticsRepo.save(analytics);
			LogUtils.logMessage(LOGGER, "Analytics statistic 'review added' increased");
		}
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
			return ErrorsEnum.ANALYTICS_EXCEL_DOCUMENT_ALREADY_EXISTS;
		}

		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("General Statistics");

		Row header = sheet.createRow(0);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
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

		String fileLocation = Constants.ANALYTICS_EXCEL_PATH;

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(fileLocation);

			workbook.write(outputStream);
			workbook.close();
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

	private ErrorsEnum updateAnalyticsExcelDocument(boolean isMobile) {
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
			if (lastRowNumber > 0) {
				Row lastRow = sheet.getRow(lastRowNumber);
				String lastDate = lastRow.getCell(0).getStringCellValue();
				if (lastDate.equals(formattedDate)) {
					// visits
					double newVisitsValue = lastRow.getCell(1).getNumericCellValue() + 1;
					lastRow.getCell(1).setCellValue(newVisitsValue);
					//mobile or web
					if (!isMobile) {
						double newWebValue = lastRow.getCell(3).getNumericCellValue() + 1;
						lastRow.getCell(3).setCellValue(newWebValue);
					} else {
						double newMobileValue = lastRow.getCell(4).getNumericCellValue() + 1;
						lastRow.getCell(4).setCellValue(newMobileValue);
					}
				} else {
					 Row row = sheet.createRow(++lastRowNumber);
					 Cell cell0 = row.createCell(0);
					 cell0.setCellValue(formattedDate);
					 //visits
					 Cell cell1 = row.createCell(1);
					 cell1.setCellValue(1);
					//mobile or web
					 Cell cell3 = row.createCell(3);
					 Cell cell4 = row.createCell(4);
					 if (!isMobile) {
						 cell3.setCellValue(1);
						 cell4.setCellValue(0);
					 } else {
						 cell3.setCellValue(0);
						 cell4.setCellValue(1);
					 }
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

}
