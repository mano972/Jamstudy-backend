package com.app.studentromania.Jamstudy;

import java.io.BufferedReader;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.app.studentromania.model.Faculty;
import com.google.gson.Gson;
import com.opencsv.CSVReader;

public class UpdateFacultiesWithExtraInfo {

	public static void main(String[] args) {
		parseFacultyInfoExtra();
		Map<String, List<String>> facInfoFromCSV = parseFacultyInfo();
		Map<String, List<String>> facInfoFromCSVExtra = parseFacultyInfoExtra();
		updateFaculties(facInfoFromCSV, facInfoFromCSVExtra);
	}

	private static Map<String, List<String>> parseFacultyInfo() {

		LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();

		String filepath = "C:\\Jamstudy_data\\faculty_data_good.csv";
		

		try (CSVReader reader = new CSVReader(new FileReader(filepath))) {
			String[] columns;
			reader.readNext(); // skip first line (header)
			while ((columns = reader.readNext()) != null) {
				String key = columns[1];
				String infoValue = columns[2];
				if (map.containsKey(key)) {
					map.get(key).add(infoValue);
				} else {
					List<String> list = new ArrayList<>();
					list.add(infoValue);
					map.put(key, list);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

//		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//			System.out.println("-------------Map--------------");
//			System.out.println(entry.getKey());
//			for (String info : entry.getValue()) {
//				System.out.println("--------");
//				System.out.println(info);
//			}
//			System.out.println("-----------------------------");
//		}

		return map;
	}

	// mainly faculty description
	private static Map<String, List<String>> parseFacultyInfoExtra() {

		LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();

		String filepath = "C:\\Jamstudy_data\\faculty_data_extra.csv";

		try (CSVReader reader = new CSVReader(new FileReader(filepath))) {
			String[] columns;
			reader.readNext(); // skip first line (header)
			while ((columns = reader.readNext()) != null) {
				String key = columns[1];
				String infoValue = columns[2];
				if (map.containsKey(key)) {
					map.get(key).add(infoValue);
				} else {
					List<String> list = new ArrayList<>();
					list.add(infoValue);
					map.put(key, list);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

//		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//			System.out.println("-------------Map--------------");
//			System.out.println(entry.getKey());
//			for (String info : entry.getValue()) {
//				System.out.println("--------");
//				System.out.println(info);
//			}
//			System.out.println("-----------------------------");
//		}

		return map;
	}

	public static void updateFaculties(Map<String, List<String>> facInfoFromCSV,
			Map<String, List<String>> facInfoFromCSVExtra) {

		List<Faculty> existingFacList = new ArrayList<>();
		HttpURLConnection connGet = null;
		try {
			URL url = new URL("http://localhost:8080/Jamstudy/v1/faculty/details?limit=1000"); // admin limit = 1000
			connGet = (HttpURLConnection) url.openConnection();
			connGet.setDoOutput(true);
			connGet.setRequestMethod("GET");
			connGet.setRequestProperty("Content-Type", "application/json");
			connGet.setRequestProperty("Accept", "application/json");
			// ai grija la LIMIT

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

		for (Faculty existingFac : existingFacList) {
			for (Map.Entry<String, List<String>> entry : facInfoFromCSV.entrySet()) {
				List<String> infoEntries = entry.getValue();
				if (infoEntries.contains(existingFac.getFacultyName())
						&& infoEntries.contains(existingFac.getUniversityName())) {
					for (String infoEntry : infoEntries) {

						if (infoEntry.contains("Site web")) {
							String keyWord1 = "Site web";
							String keyWord2 = "Adresă";
							String keyWord3 = "E-mail";
							String keyWord4 = "Telefon";

							// website
							String website = infoEntry.trim().substring(infoEntry.indexOf(keyWord1) + keyWord1.length(),
									infoEntry.indexOf(keyWord2)).trim();
							if (website.contains("http")) {
								website = website.substring(website.indexOf("http"));
							} else if (website.contains("www")) {
								website = website.substring(website.indexOf("www"));
							} else {
								website = null;
							}
							existingFac.setFacultyWebsite(website);
							// address
							String address = infoEntry.trim().substring(infoEntry.indexOf(keyWord2) + keyWord2.length(),
									infoEntry.indexOf(keyWord3)).trim();
							existingFac.setFacultyAddress(address);
							// email
							String email = infoEntry.trim().substring(infoEntry.indexOf(keyWord3) + keyWord3.length(),
									infoEntry.indexOf(keyWord4)).trim();
							existingFac.setFacultyEmail(email);
							// phone
							String phone = infoEntry.trim().substring(infoEntry.indexOf(keyWord4) + keyWord4.length())
									.trim();
							existingFac.setFacultyPhone(phone);
						}

						if (infoEntry.contains("Cadre didactice") || infoEntry.contains("înmatriculați")) {
							String keyWord1 = "Studenți înmatriculați licență:";
							String keyWord2 = "Studenți înmatriculați master:";
							String keyWord3 = "Cadre didactice:";
							String keyWord4 = "Nr. Mediu Studenți/Cadru didactic:";
							String keyWord5 = "Burse:";

							// enrolled students license
							if (infoEntry.contains(keyWord1)) {
								String end = null;
								if (infoEntry.contains(keyWord2)) {
									end = keyWord2;
								} else if (infoEntry.contains(keyWord3)) {
									end = keyWord3;
								} else if (infoEntry.contains(keyWord4)) {
									end = keyWord4;
								} else if (infoEntry.contains(keyWord5)) {
									end = keyWord5;
								}

								String enrolledStudentsLicence = end != null
										? infoEntry.trim()
												.substring(infoEntry.indexOf(keyWord1) + keyWord1.length(),
														infoEntry.indexOf(end))
												.trim()
										: infoEntry.trim().substring(infoEntry.indexOf(keyWord1) + keyWord1.length())
												.trim();
								try {
									existingFac.setEnrolledStudentsLicence(Integer.parseInt(enrolledStudentsLicence));
								} catch (NumberFormatException e) {
									existingFac.setEnrolledStudentsLicence(null);
								}
							}

							// enrolled students master
							if (infoEntry.contains(keyWord2)) {
								String end = null;
								if (infoEntry.contains(keyWord3)) {
									end = keyWord3;
								} else if (infoEntry.contains(keyWord4)) {
									end = keyWord4;
								} else if (infoEntry.contains(keyWord5)) {
									end = keyWord5;
								}

								String enrolledStudentsMaster = end != null
										? infoEntry.trim()
												.substring(infoEntry.indexOf(keyWord2) + keyWord2.length(),
														infoEntry.indexOf(end))
												.trim()
										: infoEntry.trim().substring(infoEntry.indexOf(keyWord2) + keyWord2.length())
												.trim();
								try {
									existingFac.setEnrolledStudentsMaster(Integer.parseInt(enrolledStudentsMaster));
								} catch (NumberFormatException e) {
									existingFac.setEnrolledStudentsMaster(null);
								}
							}

							// number of professors
							if (infoEntry.contains(keyWord3)) {
								String end = null;
								if (infoEntry.contains(keyWord4)) {
									end = keyWord4;
								} else if (infoEntry.contains(keyWord5)) {
									end = keyWord5;
								}

								String noOfProfessors = end != null
										? infoEntry.trim()
												.substring(infoEntry.indexOf(keyWord3) + keyWord3.length(),
														infoEntry.indexOf(end))
												.trim()
										: infoEntry.trim().substring(infoEntry.indexOf(keyWord3) + keyWord3.length())
												.trim();
								try {
									existingFac.setNoOfProfessors(Integer.parseInt(noOfProfessors));
								} catch (NumberFormatException e) {
									existingFac.setNoOfProfessors(null);
								}
							}

							// number of students per professor
							if (infoEntry.contains(keyWord4)) {
								String end = null;
								if (infoEntry.contains(keyWord5)) {
									end = keyWord5;
								}

								String noOfStudentsPerProfessor = end != null
										? infoEntry.trim()
												.substring(infoEntry.indexOf(keyWord4) + keyWord4.length(),
														infoEntry.indexOf(end))
												.trim()
										: infoEntry.trim().substring(infoEntry.indexOf(keyWord4) + keyWord4.length())
												.trim();
								try {
									existingFac.setNoOfStudentsPerProfessor(Integer.parseInt(noOfStudentsPerProfessor));
								} catch (NumberFormatException e) {
									existingFac.setNoOfStudentsPerProfessor(null);
								}
							}
						}

					}
				}
			}

			for (Map.Entry<String, List<String>> entry : facInfoFromCSVExtra.entrySet()) {
				List<String> infoEntries = entry.getValue();
				if (infoEntries.contains(existingFac.getFacultyName())
						&& infoEntries.contains(existingFac.getUniversityName())) {
					for (String infoEntry : infoEntries) {
						if (infoEntry.contains("Descriere")
								&& !infoEntry.contains("Descriere indisponibilă momentan")) {
							String keyWord = "Descriere";
							String description = infoEntry.trim()
									.substring(infoEntry.indexOf(keyWord) + keyWord.length()).trim();
							existingFac.setFacultyDescription(description);
						}
					}

				}
			}
		}

		HttpURLConnection connPost = null;
		try {
			for (Faculty fac : existingFacList) {
				if (fac.getFacultyDomainsMaster() == null || fac.getFacultyDomainsMaster().isEmpty()) {
					continue;
				}
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

}
