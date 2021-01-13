package com.app.studentromania.Jamstudy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.app.studentromania.enumtype.ProgramTypeEnum;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.FacultyProgram;
import com.google.gson.Gson;

public class UpdateFacultiesWithMasterPrograms {

	//@formatter:off
	/*
	 * Unele date sunt per facultyDomain, deci trebuie agregate pentru faculty
	 * 
	 * columns:
	 * [0] facultyName
	 * [1] universityName
	 * [2] facultyCity / universityCity
	 * [3] availablePlaces (trebuie adunate de la toate programele)
	 * [4] lastEntranceGrade (difera pentru fiecare program din aceeasi faculty)
	 * [5] candidatesPerPlace (difera pentru fiecare program din aceeasi faculty)
	 * [6] annualTax (difera pentru fiecare program din aceeasi faculty)
	 * [7] facultyProgram (baza pe care e construit site-ul optiuni)
	 * [8] facultyType / universityType (s-ar putea sa nu fie date bune)
	 * [9] authorization/accreditation (difera pentru fiecare program din aceeasi faculty)
	 * [10] specialization (difera pentru fiecare program din aceeasi faculty)
	 * [11] domainOfLicenseOrMaster (difera pentru fiecare program din aceeasi faculty)
	 * [12] facultyDomain (difera pentru fiecare program, sunt mai multe per faculty, trebuie agregate)
	 * [13] admissionType (difera pentru fiecare program din aceeasi faculty)
	 * [14] budgetPlaces (trebuie adunate de la toate programele)
	 * [15] taxPlaces (trebuie adunate de la toate programele)
	 * 
	 */
	//@formatter:on

	public static void main(String[] args) {
		List<Faculty> facListFromCSV = parseFaculties();
		updateFaculties(facListFromCSV);
	}

	private static List<Faculty> parseFaculties() {

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		List<Faculty> facList = new ArrayList<>();

		String filepath = "E:\\Jamstudy_data\\FacMasterPrograms04Oct2019.csv";

		try {

			br = new BufferedReader(new FileReader(filepath));
			br.readLine(); // skip first line (header)

			while ((line = br.readLine()) != null) {

				String[] column = line.split(cvsSplitBy);

				Faculty fac = new Faculty();
				fac.setFacultyName(column[0]);
				fac.setUniversityName(column[1]);
				fac.setFacultyCity(column[2]);

				addMasterData(column, facList, fac);

			}

//			for (Faculty f : facList) {
//				System.out.println(f.getFacultyName() + " //// " + f.getUniversityName() + " //// "
//						+ f.getAvailablePlacesLicense() + " //// " + f.getBudgetPlacesLicense() + " //// "
//						+ f.getTaxPlacesLicense() + " //// " + f.getAvailablePlacesMaster() + " //// "
//						+ f.getBudgetPlacesMaster() + " //// " + f.getTaxPlacesMaster());
//				System.out.println("-----------Domains-------------");
//				for (String facultyDomain : f.getFacultyDomainsMaster()) {
//					System.out.println(facultyDomain);
//				}
//				System.out.println("-----------License-Programs-------------");
//				for (FacultyProgram prog : f.getLicensePrograms()) {
//					System.out.println(prog.getProgramName() + " //// " + prog.getProgramDomain() + " //// "
//							+ prog.getAnnualTax());
//				}
//				System.out.println("-----------Master-Programs-------------");
//				for (FacultyProgram prog : f.getMasterPrograms()) {
//					System.out.println(prog.getProgramName() + " //// " + prog.getProgramDomain() + " //// "
//							+ prog.getAnnualTax());
//				}
//				System.out.println("------------------------");
//				System.out.println();
//			}
			return facList;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new ArrayList<>();
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void updateFaculties(List<Faculty> facListFromCSV) {

		List<Faculty> existingFacList = new ArrayList<>();
		HttpURLConnection connGet = null;
		try {
			URL url = new URL("http://localhost:8080/Jamstudy/faculty");
			connGet = (HttpURLConnection) url.openConnection();
			connGet.setDoOutput(true);
			connGet.setRequestMethod("GET");
			connGet.setRequestProperty("Content-Type", "application/json");
			connGet.setRequestProperty("Accept", "application/json");
			// ai grija la LIMIT
			
			
//			Map<String, String> parameters = new HashMap<>();
//			parameters.put("limit", "99999");
//			String parameters = "limit=99999";
//			DataOutputStream out = new DataOutputStream(connGet.getOutputStream());
//			out.writeBytes(parameters);
//			out.flush();
			

//			StringBuilder requestData = new StringBuilder();
//			for (Map.Entry<String, String> param : parameters.entrySet()) {
//				if (requestData.length() != 0) {
//					requestData.append('&');
//				}
//				// Encode the parameter based on the parameter map we've defined
//				// and append the values from the map to form a single parameter
//				requestData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
//				requestData.append('=');
//				requestData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
//			}
//			
//			HttpRequest.BodyPublishers.ofString(builder.toString());

			BufferedReader brr = new BufferedReader(new InputStreamReader((connGet.getInputStream())));

			StringBuilder sb = new StringBuilder();
			String output;
			System.out.println("Output from Server .... \n");
			while ((output = brr.readLine()) != null) {
				sb.append(output);
			}

			JSONObject json = new JSONObject(sb.toString());
			JSONObject result = json.getJSONObject("result");
			JSONArray facArray = result.getJSONArray("faculties");
			for (int i = 0; i < facArray.length(); i++) {
				JSONObject facultyJson = facArray.getJSONObject(i);
				Faculty existingFac = mapJsonToFaculty(facultyJson);
				existingFacList.add(existingFac);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			connGet.disconnect();
		}

		for (Faculty facFromCSV : facListFromCSV) {
			for (Faculty existingFac : existingFacList) {
				if (facFromCSV.getFacultyName().equals(existingFac.getFacultyName())
						&& facFromCSV.getUniversityName().equals(existingFac.getUniversityName())) {
					existingFac.setMasterPrograms(facFromCSV.getMasterPrograms());
					existingFac.setFacultyDomainsMaster(facFromCSV.getFacultyDomainsMaster());
					existingFac.setAvailablePlacesMaster(facFromCSV.getAvailablePlacesMaster());
					existingFac.setBudgetPlacesMaster(facFromCSV.getBudgetPlacesMaster());
					existingFac.setTaxPlacesMaster(facFromCSV.getTaxPlacesMaster());
					break;
				}
			}
		}

		HttpURLConnection connPost = null;
		try {
			for (Faculty fac : existingFacList) {
				String urlString = "http://localhost:8080/Jamstudy/faculty/" + fac.getFacultyId();
				URL url = new URL(urlString);
				connPost = (HttpURLConnection) url.openConnection();

				String userCredentials = "jamstudyadmin:93E493C20FFDD3F7A2219584BE7E807EA2CA8ACCAA66A0F6C06ED24200A3B16D";
				String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
				connPost.setRequestProperty("Authorization", basicAuth);
				connPost.setDoOutput(true);
				connPost.setRequestMethod("PUT");
				connPost.setRequestProperty("Content-Type", "application/json");
				Gson gson = new Gson();
				String jsonString = gson.toJson(fac);

				String input = jsonString;
				System.out.println(input);
				OutputStream os = connPost.getOutputStream();
				os.write(input.getBytes());
				os.flush();

				// verifica ca charset sa nu produca probleme cu diacriticele
				BufferedReader brr = new BufferedReader(
						new InputStreamReader((connPost.getInputStream()), Charset.forName("ISO-8859-1")));

				String output;
				System.out.println("Output from Server .... \n");
				while ((output = brr.readLine()) != null) {
					System.out.println(output);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			connPost.disconnect();
		}

	}

	private static Faculty mapJsonToFaculty(JSONObject jsonObject) {
		Faculty fac = new Faculty();
		Gson gson = new Gson();
		fac = gson.fromJson(jsonObject.toString(), Faculty.class);
		return fac;
	}

	private static void addMasterData(String[] column, List<Faculty> facList, Faculty fac) {
		// programs
		FacultyProgram prog = new FacultyProgram();
		prog.setProgramType(ProgramTypeEnum.MASTER.getValue());
		prog.setProgramName(column[7]);
		prog.setProgramDomain(column[12]);
		prog.setDomainOfLicenseOrMaster(column[11]);
		prog.setSpecialization(column[10]);
		prog.setAdmissionType(column[13]);
		prog.setProgramAccreditation(column[9]);
		try {
			prog.setProgramAvailablePlaces(Integer.parseInt(column[3]));
		} catch (NumberFormatException e) {
			prog.setProgramAvailablePlaces(null);
		}
		try {
			prog.setProgramBudgetPlaces(Integer.parseInt(column[14]));
		} catch (NumberFormatException e) {
			prog.setProgramBudgetPlaces(null);
		}
		try {
			prog.setProgramTaxPlaces(Integer.parseInt(column[15]));
		} catch (NumberFormatException e) {
			prog.setProgramTaxPlaces(null);
		}
		try {
			prog.setCandidatesPerPlace(Double.parseDouble(column[5]));
		} catch (NumberFormatException e) {
			prog.setCandidatesPerPlace(null);
		}
		try {
			prog.setLastGrade(Double.parseDouble(column[4]));
		} catch (NumberFormatException e) {
			prog.setLastGrade(null);
		}
		try {
			String annualTax = column[6];
			annualTax = annualTax.replace(".", "");
			annualTax = annualTax.replace("lei", "").trim();
			prog.setAnnualTax(Integer.parseInt(annualTax));
		} catch (NumberFormatException e) {
			prog.setAnnualTax(null);
		}
		fac.getMasterPrograms().add(prog);
		// faculty
		try {
			fac.setAvailablePlacesMaster(Integer.parseInt(column[3]));
		} catch (NumberFormatException e) {
			fac.setAvailablePlacesMaster(null);
		}
		try {
			fac.setBudgetPlacesMaster(Integer.parseInt(column[14]));
		} catch (NumberFormatException e) {
			fac.setBudgetPlacesMaster(null);
		}
		try {
			fac.setTaxPlacesMaster(Integer.parseInt(column[15]));
		} catch (NumberFormatException e) {
			fac.setTaxPlacesMaster(null);
		}

		if (fac.getFacultyDomainsMaster() == null) {
			fac.setFacultyDomainsMaster(new ArrayList<>());
		}
		fac.getFacultyDomainsMaster().add(column[12]);

		if (!isTheSameFaculty(facList, fac.getFacultyName(), fac.getUniversityName())) {
			// if it is the first occurrence of the faculty, add faculty
			facList.add(fac);
		} else {
			// if it not the first occurrence of the faculty, find other occurrences and
			// combine the data
			for (Faculty f : facList) {
				if (f.getFacultyName().equals(fac.getFacultyName())
						&& f.getUniversityName().equals(fac.getUniversityName())) {

					f.getMasterPrograms().add(fac.getMasterPrograms().get(0));
					if (!f.getFacultyDomainsMaster().contains(fac.getFacultyDomainsMaster().get(0))) {
						f.getFacultyDomainsMaster().add(fac.getFacultyDomainsMaster().get(0));
					}

					if (fac.getAvailablePlacesMaster() != null) {
						int avpl = f.getAvailablePlacesMaster() == null ? 0 : f.getAvailablePlacesMaster();
						avpl = avpl + fac.getAvailablePlacesMaster();
						f.setAvailablePlacesMaster(avpl);
					}
					if (fac.getBudgetPlacesMaster() != null) {
						int budgetP = f.getBudgetPlacesMaster() == null ? 0 : f.getBudgetPlacesMaster();
						budgetP = budgetP + fac.getBudgetPlacesMaster();
						f.setBudgetPlacesMaster(budgetP);
					}
					if (fac.getTaxPlacesMaster() != null) {
						int taxP = f.getTaxPlacesMaster() == null ? 0 : f.getTaxPlacesMaster();
						taxP = taxP + fac.getTaxPlacesMaster();
						f.setTaxPlacesMaster(taxP);
					}
				}
			}
		}
	}

	/*
	 * if the facultyName and universityName are the same
	 */
	private static boolean isTheSameFaculty(final List<Faculty> list, final String facName, final String univName) {
		return list.stream().filter(f -> f.getFacultyName().equals(facName) && f.getUniversityName().equals(univName))
				.findFirst().isPresent();
	}

}
