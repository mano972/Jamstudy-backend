package com.app.studentromania.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.studentromania.annotation.RestCall;
import com.app.studentromania.annotation.VerifyAdmin;
import com.app.studentromania.dao.ConfigDAO;
import com.app.studentromania.dao.UniversityDAO;
import com.app.studentromania.dto.FacultyDTO;
import com.app.studentromania.dto.UniversityDTO;
import com.app.studentromania.enumtype.ProgramTypeEnum;
import com.app.studentromania.model.FacultyProgram;
import com.app.studentromania.model.University;
import com.app.studentromania.service.FacultyService;
import com.app.studentromania.service.UniversityService;
import com.app.studentromania.util.Constants;

@RestController
@RequestMapping("${base.path}/init")
public class InitController {

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

	@Autowired
	private ConfigDAO configDAO;

	@Autowired
	private UniversityService universityService;

	@Autowired
	private FacultyService facultyService;

	@Autowired
	private UniversityDAO universityDAO;

	@PostMapping(value = "inituniv", consumes = "application/json; charset=ISO-8859-2", produces = "application/json; charset=ISO-8859-2")
	@RestCall
	@VerifyAdmin
	public void insertUniversities() {
		List<UniversityDTO> universityDTOList = parseUniversities();
		insertUniversities(universityDTOList);
	}

	@PostMapping(value = "initfac", consumes = "application/json; charset=ISO-8859-2", produces = "application/json; charset=ISO-8859-2")
	@RestCall
	@VerifyAdmin
	public void insertFaculties() {
		List<FacultyDTO> facultyDTOList = parseFaculties();
		insertFaculties(facultyDTOList);
	}

	private List<UniversityDTO> parseUniversities() {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		List<UniversityDTO> universityDTOList = new ArrayList<>();

		final String filepath = configDAO.getValueByConfigKey(Constants.CONFIG_KEY_FACULTIES_LICENSE_FILEPATH);

		try {

			br = new BufferedReader(new FileReader(filepath));
			br.readLine(); // skip first line (header)

			while ((line = br.readLine()) != null) {

				String[] column = line.split(cvsSplitBy);

				UniversityDTO universityDTO = new UniversityDTO();
				universityDTO.setUniversityName(column[1]);
//				univ.setType(column[8]);
				universityDTO.setUniversityCity(adjustCityDiacritics(column[2]));
				if (!containsUnivName(universityDTOList, universityDTO.getUniversityName())) {
					universityDTOList.add(universityDTO);
				}
			}
			return universityDTOList;
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

	private List<FacultyDTO> parseFaculties() {

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		List<FacultyDTO> facultyDTOList = new ArrayList<>();

		final String filepath = configDAO.getValueByConfigKey(Constants.CONFIG_KEY_FACULTIES_LICENSE_FILEPATH);

		try {

			br = new BufferedReader(new FileReader(filepath));
			br.readLine(); // skip first line (header)

			while ((line = br.readLine()) != null) {

				String[] column = line.split(cvsSplitBy);

				FacultyDTO facultyDTO = new FacultyDTO();
				facultyDTO.setFacultyName(column[0]);
				facultyDTO.setUniversityName(column[1]);
				facultyDTO.setFacultyCity(adjustCityDiacritics(column[2]));

				addLicenseData(column, facultyDTOList, facultyDTO);

			}

//			for (FacultyDTO f : facultyDTOList) {
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

			return facultyDTOList;
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

	public void insertUniversities(List<UniversityDTO> universityDTOList) {
		for (UniversityDTO universityDTO : universityDTOList) {
			universityService.createUniversity(universityDTO);
		}
	}

	public void insertFaculties(List<FacultyDTO> facultyDTOList) {
		List<University> univList = universityDAO.getAllUniversities();

		for (FacultyDTO facultyDTO : facultyDTOList) {
			for (University univ : univList) {
				if (facultyDTO.getUniversityName().equals(univ.getUniversityName())) {
					facultyDTO.setUniversityId(univ.getUniversityId());
					facultyDTO.setUniversityName(univ.getUniversityName());
					break;
				}
			}
		}

		for (FacultyDTO facultyDTO : facultyDTOList) {
			facultyService.createFaculty(facultyDTO);

		}

//		for (FacultyDTO f : facultyDTOList) {
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

	private void addLicenseData(String[] column, List<FacultyDTO> facList, FacultyDTO fac) {
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
		if (fac.getLicensePrograms() == null) {
			fac.setLicensePrograms(new ArrayList<>());
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
			for (FacultyDTO f : facList) {
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

	private boolean containsUnivName(final List<UniversityDTO> list, final String name) {
		return list.stream().filter(u -> u.getUniversityName().equals(name)).findFirst().isPresent();
	}

	/*
	 * if the facultyName and universityName are the same
	 */
	private boolean isTheSameFaculty(final List<FacultyDTO> list, final String facName, final String univName) {
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
