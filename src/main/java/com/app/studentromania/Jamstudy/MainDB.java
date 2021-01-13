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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.app.studentromania.dao.UniversityDAO;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.University;

public class MainDB {

	private static UniversityDAO universityDao;

	public static void main(String[] args) {

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
		 * [9] authorization / accreditation (difera pentru fiecare program din aceeasi faculty)
		 * [10] specialization (difera pentru fiecare program din aceeasi faculty)
		 * [11] domainOfLicenseOrMaster (difera pentru fiecare program din aceeasi faculty)
		 * [12] facultyDomain (difera pentru fiecare program, sunt mai multe per faculty, trebuie agregate)
		 * [13] admissionType (difera pentru fiecare program din aceeasi faculty)
		 * [14] budgetPlaces (trebuie adunate de la toate programele)
		 * [15] taxPlaces (trebuie adunate de la toate programele)
		 * 
		 */
		//@formatter:on

		String filepath = "E:\\Jamstudy_data\\Fac12Sept2019.csv";

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		List<University> univList = new ArrayList<>();
		List<Faculty> facList = new ArrayList<>();
		Map<String, List<Faculty>> map = new HashMap<>();

		try {

			br = new BufferedReader(new FileReader(filepath));

			while ((line = br.readLine()) != null) {

				String[] column = line.split(cvsSplitBy);

				University univ = new University();
				univ.setUniversityName(column[1]);
				univ.setType(column[8]);
				univ.setUniversityCity(column[2]);
//				 String nfdNormalizedString = Normalizer.normalize(univ.getUniversityName(), Normalizer.Form.NFD); 
//				    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//				   String x = pattern.matcher(nfdNormalizedString).replaceAll("");
//				   
//				  if (1==1) {
//					  throw new NullPointerException();
//					  }

				if (!containsUnivName(univList, univ.getUniversityName())) {
					univList.add(univ);
					map.put(univ.getUniversityName(), new ArrayList<>());
				}

				Faculty fac = new Faculty();
				fac.setFacultyName(column[0]);
				fac.setFacultyCity(column[2]);
				try {
					fac.setAvailablePlacesLicense(Integer.parseInt(column[3]));
				} catch (NumberFormatException e) {
					fac.setAvailablePlacesLicense(0);
				}
				try {
					fac.setBudgetPlacesLicense(Integer.parseInt(column[13]));
				} catch (NumberFormatException e) {
					fac.setBudgetPlacesLicense(0);
				}
				try {
					fac.setTaxPlacesLicense(Integer.parseInt(column[14]));
				} catch (NumberFormatException e) {
					fac.setTaxPlacesLicense(0);
				}

				if (map.keySet().contains(column[1])) {
					// if(!map.get(column[1]).contains(o)) {
					//
					// }
					List<Faculty> flist = map.get(column[1]);
					if (!containsFacName(flist, fac.getFacultyName())) {
						flist.add(fac);
					} else {
						for (Faculty f : flist) {
							if (f.getFacultyName().equals(fac.getFacultyName())) {
								int avpl = f.getAvailablePlacesLicense();
								avpl = avpl + fac.getAvailablePlacesLicense();
								f.setAvailablePlacesLicense(avpl);
								int budgetP = f.getBudgetPlacesLicense();
								budgetP = budgetP + fac.getBudgetPlacesLicense();
								f.setBudgetPlacesLicense(budgetP);
								int taxP = f.getTaxPlacesLicense();
								taxP = taxP + fac.getTaxPlacesLicense();
								f.setTaxPlacesLicense(taxP);
							}
						}
					}
				}

			}
			for (Map.Entry<String, List<Faculty>> entry : map.entrySet()) {
				for (Faculty f : entry.getValue())
					System.out
							.println(entry.getKey() + " = " + f.getFacultyName() + " " + f.getAvailablePlacesLicense());
			}
			// univList.get(0).setUniversityId(241);
			// universityDao = new UniversityDaoImpl();
			// System.out.println(universityDao.retrieve(2));
			// universityDao.save(univList.get(0));

			// Iterator<Entry<String, List<Faculty>>> iter = map.entrySet().iterator();
			// while (iter.hasNext()) {
			// Entry<String, List<Faculty>> entry = iter.next();
			// System.out.println(entry.getKey() + " = " + entry.getValue().toString());
			// }
			System.out.println(map.size());
			int count = 0;
			for (List<Faculty> l : map.values()) {
				count = count + l.size();
			}
			System.out.println(count);

			// for (University u : univList) {
			// System.out.println(u.getUniversityName());
			// }
			// System.out.println(univList.size());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// try {
		// for (University uu : univList) {
		// JSONArray array = new JSONArray();
		// // for (University u : univList) {
		// URL url = new
		// URL("http://localhost:8080/Jamstudy4/jamstudy/university/save");
		// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// conn.setDoOutput(true);
		// conn.setRequestMethod("POST");
		// conn.setRequestProperty("Content-Type", "application/json");
		//
		// // String input = "{\"universityName\":\"iPad 4\",\"universityCity\":\"iPad
		// // 4\"}";
		//
		// int c = 0;
		// // for (University uu : univList) {
		// // University u = univList.get(0);
		// JSONObject json = new JSONObject();
		// try {
		// json.put("universityName", Normalizer.normalize(uu.getUniversityName(),
		// Normalizer.Form.NFD)
		// .replaceAll("[^\\p{ASCII}]", ""));
		//// json.put("universityName", uu.getUniversityName());
		// json.put("universityCity", uu.getUniversityCity());
		// json.put("type", uu.getType());
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// array.put(json);
		// // c++;
		// // if (c == 2) {
		// // break;
		// // }
		//
		// // if (1==1) {
		// // throw new NullPointerException();
		// // }
		//
		// String input = json.toString();
		//
		// OutputStream os = conn.getOutputStream();
		// os.write(input.getBytes());
		// os.flush();
		//
		// BufferedReader brr = new BufferedReader(
		// new InputStreamReader((conn.getInputStream()),
		// Charset.forName("ISO-8859-1")));
		//
		// //
		// // String output;
		// // System.out.println("Output from Server .... \n");
		// // while ((output = brr.readLine()) != null) {
		// // System.out.println(output);
		// // }
		//
		// conn.disconnect();
		// }
		//
		// } catch (MalformedURLException e) {
		//
		// e.printStackTrace();
		//
		// } catch (IOException e) {
		//
		// e.printStackTrace();
		//
		// }
		List<University> finalUnivList = new ArrayList<>();
		try {

			URL url = new URL("http://localhost:8080/Jamstudy4/jamstudy/university/retrieveall?limit=500");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			BufferedReader brxx = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			JSONArray array = null;
			System.out.println("Output from Server .... \n");
			while ((output = brxx.readLine()) != null) {
				System.out.println(output);
				try {
					array = new JSONArray(output);
					String x = "";
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			for (int i = 0; i < array.length(); i++) {
				try {
					JSONObject json = array.getJSONObject(i);
					University univ = new University();
					univ.setUniversityName(json.getString("universityName"));
					univ.setUniversityId(json.getString("universityId"));
					finalUnivList.add(univ);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			conn.disconnect();
//			if (1 == 1) {
//				throw new NullPointerException();
//			}

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

		try {
			String id = null;
			for (Map.Entry<String, List<Faculty>> entry : map.entrySet()) {
				for (Faculty f : entry.getValue()) {

					JSONObject json = new JSONObject();
					try {
						json.put("facultyName", Normalizer.normalize(f.getFacultyName(), Normalizer.Form.NFD)
								.replaceAll("[^\\p{ASCII}]", ""));
						// json.put("universityName", uu.getUniversityName());
						json.put("facultyCity", f.getFacultyCity());
						json.put("availablePlacesLicense", f.getAvailablePlacesLicense());
						json.put("budgetPlacesLicense", f.getBudgetPlacesLicense());
						json.put("taxPlacesLicense", f.getTaxPlacesLicense());
						for (University u : finalUnivList) {
							if (Normalizer.normalize(entry.getKey(), Normalizer.Form.NFD)
									.replaceAll("[^\\p{ASCII}]", "").equals(u.getUniversityName())) {
//								json.put("universityId", u.getUniversityId());
								id = u.getUniversityId();
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String input = json.toString();
					URL url = new URL("http://localhost:8080/Jamstudy4/jamstudy/faculty/" + id + "/save");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");

					OutputStream os = conn.getOutputStream();
					os.write(input.getBytes());
					os.flush();

					BufferedReader brr = new BufferedReader(
							new InputStreamReader((conn.getInputStream()), Charset.forName("ISO-8859-1")));

					conn.disconnect();
				}
			}

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

		BufferedReader br1 = null;
		String line1 = "";

		// try {
		//
		// br1 = new BufferedReader(new FileReader(filepath));
		//
		//
		// while ((line1 = br1.readLine()) != null) {
		//
		// String[] column = line1.split(cvsSplitBy);
		//
		// Faculty fac = new Faculty();
		// fac.setFacultyName(column[0]);
		// fac.setFacultyCity(column[2]);
		// try {
		// fac.setAvailablePlaces(Integer.parseInt(column[3]));
		// } catch (NumberFormatException e) {
		// fac.setAvailablePlaces(0);
		// }
		// try {
		// fac.setBudgetPlaces(Integer.parseInt(column[13]));
		// } catch (NumberFormatException e) {
		// fac.setBudgetPlaces(0);
		// }
		// try {
		// fac.setTaxPlaces(Integer.parseInt(column[14]));
		// } catch (NumberFormatException e) {
		// fac.setTaxPlaces(0);
		// }
		//
		// for (University u : univList) {
		// if(u.getUniversityName().equals(column[1])) {
		//
		// }
		// if (!containsFacName(facList, fac.getFacultyName())) {
		// facList.add(fac);
		// System.out.println(fac.getFacultyName() + column[1]);
		// }
		// }
		//
		// }
		// // for (Faculty f : facList) {
		// // System.out.println(f.getFacultyName() + f.getAvailablePlaces() +
		// // f.getBudgetPlaces());
		// // }
		// System.out.println(facList.size());
		//
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// } finally {
		// if (br != null) {
		// try {
		// br.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		//

	}

	public static boolean containsUnivName(final List<University> list, final String name) {
		return list.stream().filter(u -> u.getUniversityName().equals(name)).findFirst().isPresent();
	}

	public static boolean containsFacName(final List<Faculty> list, final String name) {
		return list.stream().filter(f -> f.getFacultyName().equals(name)).findFirst().isPresent();
	}

	public static int addFacAvailablePlacesLicense(final List<Faculty> list, final String name) {
		return list.stream().filter(f -> f.getFacultyName().equals(name)).mapToInt(Faculty::getAvailablePlacesLicense)
				.sum();
	}

	public static int addFacAvailablePlacesMaster(final List<Faculty> list, final String name) {
		return list.stream().filter(f -> f.getFacultyName().equals(name)).mapToInt(Faculty::getAvailablePlacesMaster)
				.sum();
	}

}
