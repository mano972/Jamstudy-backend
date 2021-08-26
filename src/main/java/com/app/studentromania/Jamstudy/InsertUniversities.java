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

import org.json.JSONException;
import org.json.JSONObject;

import com.app.studentromania.model.University;

public class InsertUniversities {

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
//		List<University> univList = parseUniversities();
//		insertUniversities(univList);
		long x = 0;
		long y = 0;
		
		do {
			y = 200;
			System.out.println(x);
			x++;
			
		} while(x < y);
	}

	private static List<University> parseUniversities() {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		List<University> univList = new ArrayList<>();

		String filepath = "C:\\Jamstudy_data\\Fac12Sept2019.csv";

		try {

			br = new BufferedReader(new FileReader(filepath));
			br.readLine(); // skip first line (header)

			while ((line = br.readLine()) != null) {

				String[] column = line.split(cvsSplitBy);

				University univ = new University();
				univ.setUniversityName(column[1]);
//				univ.setType(column[8]);
				univ.setUniversityCity(adjustCityDiacritics(column[2]));
				if (!containsUnivName(univList, univ.getUniversityName())) {
					univList.add(univ);
				}
			}
			return univList;
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

	public static void insertUniversities(List<University> univList) {

		HttpURLConnection conn = null;
		try {
			for (University univ : univList) {
				URL url = new URL("http://localhost:8080/Jamstudy/v1/university");
				conn = (HttpURLConnection) url.openConnection();

				String userCredentials = "jamstudyadmin:93E493C20FFDD3F7A2219584BE7E807EA2CA8ACCAA66A0F6C06ED24200A3B16D";
				String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
				conn.setRequestProperty("Authorization", basicAuth);
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");

				JSONObject json = new JSONObject();
				try {
					// can also use GSON
					json.put("universityName", univ.getUniversityName());
					json.put("universityCity", univ.getUniversityCity());
//					json.put("type", uu.getType());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				String input = json.toString();
				System.out.println(input);
				OutputStream os = conn.getOutputStream();
				os.write(input.getBytes());
				os.flush();

				// verifica ca charset sa nu produca probleme
				BufferedReader brr = new BufferedReader(
						new InputStreamReader((conn.getInputStream()), Charset.forName("ISO-8859-1")));

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
			conn.disconnect();
		}
	}

	private static boolean containsUnivName(final List<University> list, final String name) {
		return list.stream().filter(u -> u.getUniversityName().equals(name)).findFirst().isPresent();
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

}
