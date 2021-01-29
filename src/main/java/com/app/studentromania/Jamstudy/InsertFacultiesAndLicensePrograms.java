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
import com.app.studentromania.model.University;
import com.google.gson.Gson;

public class InsertFacultiesAndLicensePrograms {

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
		List<Faculty> facList = parseFaculties();
		insertFaculties(facList);
	}

	private static List<Faculty> parseFaculties() {

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		List<Faculty> facList = new ArrayList<>();

		String filepath = "C:\\Jamstudy_data\\Fac12Sept2019.csv";

		try {

			br = new BufferedReader(new FileReader(filepath));
			br.readLine(); // skip first line (header)

			while ((line = br.readLine()) != null) {

				String[] column = line.split(cvsSplitBy);

				Faculty fac = new Faculty();
				fac.setFacultyName(column[0]);
				fac.setUniversityName(column[1]);
				fac.setFacultyCity(adjustCityDiacritics(column[2]));

				addLicenseData(column, facList, fac);

//				if (facList.size() == 20) {
//					break;
//				}

			}

//			for (Faculty f : facList) {
//				System.out.println(
//						f.getFacultyName() + " //// " + f.getUniversityName() + " //// " + f.getAvailablePlacesLicense()
//								+ " //// " + f.getBudgetPlacesLicense() + " //// " + f.getTaxPlacesLicense());
//				System.out.println("-----------Domains-------------");
//				for (String facultyDomain : f.getFacultyDomainsLicense()) {
//					System.out.println(facultyDomain);
//				}
//				System.out.println("-----------Programs-------------");
//				for (FacultyProgram prog : f.getLicensePrograms()) {
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

	public static void insertFaculties(List<Faculty> facList) {
		List<University> univList = new ArrayList<>();
		HttpURLConnection connGet = null;
		try {
			URL url = new URL("http://localhost:8080/Jamstudy/university");
			connGet = (HttpURLConnection) url.openConnection();
			connGet.setDoOutput(true);
			connGet.setRequestMethod("GET");
			connGet.setRequestProperty("Content-Type", "application/json");
			connGet.setRequestProperty("Accept", "application/json");

			BufferedReader brr = new BufferedReader(new InputStreamReader((connGet.getInputStream())));

			StringBuilder sb = new StringBuilder();
			String output;
			System.out.println("Output from Server .... \n");
			while ((output = brr.readLine()) != null) {
				sb.append(output);
			}

			JSONObject json = new JSONObject(sb.toString());
			JSONObject result = json.getJSONObject("result");
			JSONArray univArray = result.getJSONArray("universities");
			for (int i = 0; i < univArray.length(); i++) {
				JSONObject universityJson = univArray.getJSONObject(i);
				University univ = mapJsonToUniversity(universityJson);
				univList.add(univ);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			connGet.disconnect();
		}

		for (Faculty fac : facList) {
			for (University univ : univList) {
				if (fac.getUniversityName().equals(univ.getUniversityName())) {
					fac.setUniversityId(univ.getUniversityId());
					fac.setUniversityName(univ.getUniversityName());
					break;
				}
			}
		}

		HttpURLConnection connPost = null;
		try {
			for (Faculty fac : facList) {
				String urlString = "http://localhost:8080/Jamstudy/faculty/" + fac.getUniversityId();
				URL url = new URL(urlString);
				connPost = (HttpURLConnection) url.openConnection();

				String userCredentials = "jamstudyadmin:93E493C20FFDD3F7A2219584BE7E807EA2CA8ACCAA66A0F6C06ED24200A3B16D";
				String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
				connPost.setRequestProperty("Authorization", basicAuth);
				connPost.setDoOutput(true);
				connPost.setRequestMethod("POST");
				connPost.setRequestProperty("Content-Type", "application/json");
				Gson gson = new Gson();
				String jsonString = gson.toJson(fac);
//				JSONObject json = null;
//				try {
//					json = new JSONObject(jsonString);

//					json.put("universityName", fac.getUniversityName());
//					json.put("universityId", fac.getUniversityId());
//					json.put("facultyCity", fac.getFacultyCity());
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}

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

//		for (Faculty f : facList) {
//			System.out.println(
//					f.getFacultyName() + " //// " + f.getUniversityName() + " //// " + f.getAvailablePlacesLicense()
//							+ " //// " + f.getBudgetPlacesLicense() + " //// " + f.getTaxPlacesLicense());
//			System.out.println("-----------Domains-------------");
//			for (String facultyDomain : f.getFacultyDomainsLicense()) {
//				System.out.println(facultyDomain);
//			}
//			System.out.println("-----------Programs-------------");
//			for (FacultyProgram prog : f.getLicensePrograms()) {
//				System.out.println(
//						prog.getProgramName() + " //// " + prog.getProgramDomain() + " //// " + prog.getAnnualTax());
//			}
//			System.out.println("---------UniversityId----------");
//			System.out.println(f.getUniversityId());
//			System.out.println("------------------------");
//			System.out.println();
//		}

	}

	private static University mapJsonToUniversity(JSONObject jsonObject) {
		University univ = new University();
		Gson gson = new Gson();
		univ = gson.fromJson(jsonObject.toString(), University.class);
		return univ;
	}

	private static void addLicenseData(String[] column, List<Faculty> facList, Faculty fac) {
		// program
		FacultyProgram prog = new FacultyProgram();
		prog.setProgramType(ProgramTypeEnum.LICENSE.getValue());
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
		fac.getLicensePrograms().add(prog);
		// faculty
		try {
			fac.setAvailablePlacesLicense(Integer.parseInt(column[3]));
		} catch (NumberFormatException e) {
			fac.setAvailablePlacesLicense(null);
		}
		try {
			fac.setBudgetPlacesLicense(Integer.parseInt(column[14]));
		} catch (NumberFormatException e) {
			fac.setBudgetPlacesLicense(null);
		}
		try {
			fac.setTaxPlacesLicense(Integer.parseInt(column[15]));
		} catch (NumberFormatException e) {
			fac.setTaxPlacesLicense(null);
		}
		if (fac.getFacultyDomainsLicense() == null) {
			fac.setFacultyDomainsLicense(new ArrayList<>());
		}
		fac.getFacultyDomainsLicense().add(adjustDomain(column[12]));

		if (!isTheSameFaculty(facList, fac.getFacultyName(), fac.getUniversityName())) {
			// if it is the first occurrence of the faculty, add faculty
			facList.add(fac);
		} else {
			// if it not the first occurrence of the faculty, find other occurrences and
			// combine the data
			for (Faculty f : facList) {
				if (f.getFacultyName().equals(fac.getFacultyName())
						&& f.getUniversityName().equals(fac.getUniversityName())) {

					f.getLicensePrograms().add(fac.getLicensePrograms().get(0));
					if (!f.getFacultyDomainsLicense().contains(fac.getFacultyDomainsLicense().get(0))) {
						f.getFacultyDomainsLicense().add(fac.getFacultyDomainsLicense().get(0));
					}

					if (fac.getAvailablePlacesLicense() != null) {
						int avpl = f.getAvailablePlacesLicense() == null ? 0 : f.getAvailablePlacesLicense();
						avpl = avpl + fac.getAvailablePlacesLicense();
						f.setAvailablePlacesLicense(avpl);
					}
					if (fac.getBudgetPlacesLicense() != null) {
						int budgetP = f.getBudgetPlacesLicense() == null ? 0 : f.getBudgetPlacesLicense();
						budgetP = budgetP + fac.getBudgetPlacesLicense();
						f.setBudgetPlacesLicense(budgetP);
					}
					if (fac.getTaxPlacesLicense() != null) {
						int taxP = f.getTaxPlacesLicense() == null ? 0 : f.getTaxPlacesLicense();
						taxP = taxP + fac.getTaxPlacesLicense();
						f.setTaxPlacesLicense(taxP);
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

	private static String adjustCityDiacritics(String city) {
		String adjustedCity = null;

		switch (city) {
		case "Bucuresti, Bucuresti":
			adjustedCity = "Bucureşti";
			break;
		case "Timisoara, Timis":
			adjustedCity = "Timişoara, Timiş";
			break;
		case "Iasi, Iasi":
			adjustedCity = "Iaşi, Iaşi";
			break;
		case "Constanta, Constanta":
			adjustedCity = "Constanţa, Constanţa";
			break;
		case "Brasov, Brasov":
			adjustedCity = "Braşov, Braşov";
			break;
		case "Pitesti, Arges":
			adjustedCity = "Piteşti, Argeş";
			break;
		case "Targoviste, Dambovita":
			adjustedCity = "Târgovişte, Damboviţa";
			break;
		case "Galati, Galati":
			adjustedCity = "Galaţi, Galaţi";
			break;
		case "Targu Mures, Mures":
			adjustedCity = "Târgu Mureş, Mureş";
			break;
		case "Baia Mare, Maramures":
			adjustedCity = "Baia Mare, Maramureş";
			break;
		case "Drobeta Turnu Severin, Mehedinti":
			adjustedCity = "Drobeta Turnu Severin, Mehedinţi";
			break;
		case "Bacau, Bacau":
			adjustedCity = "Bacău, Bacău";
			break;
		case "Ploiesti, Prahova":
			adjustedCity = "Ploieşti, Prahova";
			break;
		case "Targu Jiu, Gorj":
			adjustedCity = "Târgu Jiu, Gorj";
			break;
		case "Buzau, Buzau":
			adjustedCity = "Buzău, Buzău";
			break;
		case "Zalau, Salaj":
			adjustedCity = "Zalău, Sălaj";
			break;
		case "Ramnicu Valcea, Valcea":
			adjustedCity = "Râmnicu Vâlcea, Vâlcea";
			break;
		default:
			adjustedCity = city;
		}
		// TODO Nasaud, Bistrita, Lugoj, Resita, Sfantu Gheorghe, Sighetu Marmatiei,
		// Petrosani, Braila, Focsani, Slobozia (Ialomita), Beius;

		return adjustedCity;
	}

	// Adjust only facultyDomainsLicense, not also the licensePrograms
	private static String adjustDomain(String domain) {
		String adjustedDomain = null;

		switch (domain) {
		case "Arte, Arhitectură și Urbanism ":
			adjustedDomain = "Arte Arhitectură și Urbanism";
			break;
		case "Ştiinţe Militare, Informaţii şi Ordine publică":
			adjustedDomain = "Ştiinţe Militare Informaţii şi Ordine publică";
			break;
		default:
			adjustedDomain = domain;
		}

		return adjustedDomain;
	}

}
