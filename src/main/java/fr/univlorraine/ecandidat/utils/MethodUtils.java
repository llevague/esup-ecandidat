/**
 *  ESUP-Portail eCandidat - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package fr.univlorraine.ecandidat.utils;

import java.io.Closeable;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import org.apache.axis.utils.XMLChar;
import org.slf4j.Logger;

import com.vaadin.ui.UI;

import fr.univlorraine.ecandidat.utils.bean.presentation.SimpleTablePresentation;

/**
 * Class de methode utilitaires
 * 
 * @author Kevin Hergalant
 *
 */
public class MethodUtils {

	/**
	 * Renvoi pour une classe donnée si le champs est nullable ou non
	 * 
	 * @param classObject
	 * @param property
	 * @return true si l'objet n'est pas null
	 */
	public static Boolean getIsNotNull(Class<?> classObject, String property) {
		try {
			NotNull notNull = classObject.getDeclaredField(property).getAnnotation(NotNull.class);
			if (notNull == null) {
				return false;
			}
			return true;
		} catch (NoSuchFieldException | SecurityException e) {
			return false;
		}
	}

	/**
	 * Renvoi un boolean pour un temoin en string (O ou N)
	 * 
	 * @param temoin
	 * @return le boolean associe
	 */
	public static Boolean getBooleanFromTemoin(String temoin) {
		if (temoin == null || temoin.equals(ConstanteUtils.TYP_BOOLEAN_NO)) {
			return false;
		}
		return true;
	}

	/**
	 * Renvoi temoin en string (O ou N) pour un boolean
	 * 
	 * @param bool
	 * @return le String associe
	 */
	public static String getTemoinFromBoolean(Boolean bool) {
		if (!bool) {
			return ConstanteUtils.TYP_BOOLEAN_NO;
		}
		return ConstanteUtils.TYP_BOOLEAN_YES;
	}

	/**
	 * Ajoute du texte à la suite et place une virgule entre
	 * 
	 * @param text
	 * @param more
	 * @return le txt complété
	 */
	public static String constructStringEnum(String text, String more) {
		if (text == null || text.equals("")) {
			return more;
		} else {
			return text + ", " + more;
		}
	}

	/**
	 * Ajoute un 0 devant le label de temps pour 0, 1, 2, etc..
	 * 
	 * @param time
	 * @return le label de minute ou d'heure complété
	 */
	public static String getLabelMinuteHeure(Integer time) {
		if (time == null) {
			return "";
		} else {
			String temps = String.valueOf(time);
			if (temps.length() == 1) {
				temps = "0" + temps;
			}
			return temps;
		}
	}

	/**
	 * Nettoie un nom de fichier pour le stockage fs
	 * 
	 * @param fileName
	 * @return le nom de fichier pour le stockage fs
	 */
	public static String cleanFileName(String fileName) {
		if (fileName == null || fileName.equals("")) {
			return "_";
		}
		return removeAccents(fileName).replaceAll("[^A-Za-z0-9\\.\\-\\_]", "");
	}

	/**
	 * Remplace les accents
	 * 
	 * @param text
	 * @return le text sans accents
	 */
	public static String removeAccents(String text) {
		return text == null ? ""
				: Normalizer.normalize(text, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	/**
	 * Valide un bean
	 * 
	 * @param bean
	 * @throws CustomException
	 */
	public static <T> Boolean validateBean(T bean, Logger logger) {
		logger.debug(" ***VALIDATION*** : " + bean);
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(bean);
		if (constraintViolations != null && constraintViolations.size() > 0) {
			for (ConstraintViolation<?> violation : constraintViolations) {
				logger.debug(" *** " + violation.getPropertyPath().toString() + " : " + violation.getMessage());
			}
			return false;
		}
		return true;
	}

	/**
	 * Formate un texte
	 * 
	 * @param txt
	 * @return un txt formaté
	 */
	public static String formatToExport(String txt) {
		if (txt == null) {
			return "";
		}
		return txt;
	}

	/**
	 * Verifie que le fichier est un pdf
	 * 
	 * @param fileName
	 * @return true si le fichier est un pdf
	 */
	public static Boolean isPdfFileName(String fileName) {
		return Arrays.asList(ConstanteUtils.EXTENSION_PDF).contains(getExtension(fileName.toLowerCase()));
	}

	/**
	 * Verifie que le fichier est une image
	 * 
	 * @param fileName
	 * @return true si le fichier est une image
	 */
	public static Boolean isImgFileName(String fileName) {
		return Arrays.asList(ConstanteUtils.EXTENSION_IMG).contains(getExtension(fileName.toLowerCase()));
	}

	/**
	 * Verifie que le fichier est un jpg
	 * 
	 * @param fileName
	 * @return true si le fichier est un jpg
	 */
	public static Boolean isJpgFileName(String fileName) {
		return Arrays.asList(ConstanteUtils.EXTENSION_JPG).contains(getExtension(fileName.toLowerCase()));
	}

	/**
	 * Verifie que le fichier est un png
	 * 
	 * @param fileName
	 * @return true si le fichier est un png
	 */
	public static Boolean isPngFileName(String fileName) {
		return Arrays.asList(ConstanteUtils.EXTENSION_PNG).contains(getExtension(fileName.toLowerCase()));
	}

	/**
	 * renvoie l'extension
	 * 
	 * @param fileName
	 * @return l'extension du fichier
	 */
	private static String getExtension(String fileName) {
		String extension = "";

		int i = fileName.lastIndexOf('.');
		if (i >= 0) {
			extension = fileName.substring(i + 1);
		}
		return extension;
	}

	/**
	 * @param liste
	 * @param code
	 * @return le libellé de presentation
	 */
	public static String getLibByPresentationCode(List<SimpleTablePresentation> liste, String code) {
		Optional<SimpleTablePresentation> opt = liste.stream().filter(e -> e.getCode().equals(code)).findFirst();
		if (opt.isPresent() && opt.get().getValue() != null) {
			return opt.get().getValue().toString();
		}
		return "";
	}

	/**
	 * Verifie qu'une date est inclue dans un intervalle
	 * 
	 * @param dateToCompare
	 * @return true si la date est incluse dans un interval
	 */
	public static Boolean isDateIncludeInInterval(LocalDate dateToCompare, LocalDate dateDebut, LocalDate dateFin) {
		if (dateToCompare == null) {
			/* Si la date est null, c'est ok! */
			return true;
		} else if ((dateToCompare.equals(dateDebut) || dateToCompare.isAfter(dateDebut))
				&& (dateToCompare.equals(dateFin) || dateToCompare.isBefore(dateFin))) {
			return true;
		}
		return false;
	}

	/**
	 * Converti un String en entier
	 * 
	 * @param txt
	 * @return l'entier converti
	 */
	public static Integer convertStringToIntger(String txt) {
		if (txt == null) {
			return null;
		}
		try {
			return Integer.valueOf(txt);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Converti une date en LocalDate
	 * 
	 * @param date
	 * @return la localDate convertie
	 */
	public static LocalDate convertDateToLocalDate(Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * Converti une LocalDate en date
	 * 
	 * @param date
	 * @return la date convertie
	 */
	public static Date convertLocalDateToDate(LocalDate date) {
		if (date == null) {
			return null;
		}
		Instant instant = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Converti une LocalDateTime en LocalDate
	 * 
	 * @param localDateTime
	 * @return la date convertie
	 */
	public static LocalDate convertLocalDateTimeToDate(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}
		return localDateTime.toLocalDate();
	}

	/**
	 * Replace la derniere occurence
	 * 
	 * @param string
	 * @param from
	 * @param to
	 * @return le string nettoye
	 */
	public static String replaceLast(String string, String from, String to) {
		int lastIndex = string.lastIndexOf(from);
		if (lastIndex < 0)
			return string;
		String tail = string.substring(lastIndex).replaceFirst(from, to);
		return string.substring(0, lastIndex) + tail;
	}

	/**
	 * @param fileName
	 * @param isOnlyImg
	 * @return true si l'extension est jpg ou pdf
	 */
	public static Boolean checkExtension(String fileName, Boolean isOnlyImg) {
		String extension = "";
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
		} else {
			return false;
		}

		if (extension.equals("")) {
			return false;
		}
		extension = extension.toLowerCase();
		if (!isOnlyImg && Arrays.asList(ConstanteUtils.EXTENSION_PDF_IMG).contains(extension)) {
			return true;
		} else if (isOnlyImg && Arrays.asList(ConstanteUtils.EXTENSION_IMG).contains(extension)) {
			return true;
		}
		return false;
	}

	/**
	 * @param nomFichier
	 * @return le type MIME d'un fichier
	 */
	public static String getMimeType(String nomFichier) {
		if (isPdfFileName(nomFichier)) {
			return ConstanteUtils.TYPE_MIME_FILE_PDF;
		} else if (isJpgFileName(nomFichier)) {
			return ConstanteUtils.TYPE_MIME_FILE_JPG;
		} else if (isPngFileName(nomFichier)) {
			return ConstanteUtils.TYPE_MIME_FILE_PNG;
		} else if (Arrays.asList(new String[] { "docx" }).contains(getExtension(nomFichier.toLowerCase()))) {
			return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		}
		return null;
	}

	/**
	 * @param path
	 * @return la path agrémenté d'un / a la fin
	 */
	public static String formatUrlApplication(String path) {
		if (path != null && !path.equals("")) {
			if (!path.substring(path.length() - 1).equals("/")) {
				path += "/";
			}
		}
		return path;
	}

	/**
	 * @param appPath
	 * @param add
	 * @return l'url formatée pour switch user
	 */
	public static String formatSecurityPath(String appPath, String add) {
		if (appPath != null && !appPath.equals("")) {
			if (appPath.substring(appPath.length() - 1).equals("/")) {
				appPath = appPath.substring(0, appPath.length() - 1);
			}
		}
		return appPath + add;
	}

	/**
	 * @param date
	 * @param formatterDate
	 * @return la date formatee
	 */
	public static String formatDate(LocalDateTime date, DateTimeFormatter formatterDate) {
		if (date == null) {
			return "";
		} else {
			return date.format(formatterDate);
		}
	}

	/**
	 * @param date
	 * @param formatterDate
	 * @return la date formatee
	 */
	public static String formatDate(LocalDate date, DateTimeFormatter formatterDate) {
		if (date == null) {
			return "";
		} else {
			return date.format(formatterDate);
		}
	}

	/**
	 * @return la version des WS
	 */
	public static String getClassVersion(Class<?> theClass) {
		try {

			// Find the path of the compiled class
			String classPath = theClass.getResource(theClass.getSimpleName() + ".class").toString();

			// Find the path of the lib which includes the class
			String libPath = classPath.substring(0, classPath.lastIndexOf("!"));

			if (libPath != null) {
				Integer lastIndex = libPath.lastIndexOf("/");
				if (lastIndex != -1) {
					libPath = libPath.substring(lastIndex + 1, libPath.length());
					libPath = libPath.replaceAll(".jar", "");
					return libPath;
				}
			}
			/*
			 * if (libPath!=null){ Integer lastIndex = libPath.lastIndexOf("/"); if
			 * (lastIndex!=-1){ libPath = libPath.substring(0,lastIndex); lastIndex =
			 * libPath.lastIndexOf("/"); if (lastIndex!=-1){ libPath =
			 * libPath.substring(lastIndex+1,libPath.length()); } return libPath; } }
			 */
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * Vérifie si une throwable appartient à une classe
	 * 
	 * @param cause
	 * @param causeSearch
	 * @return true si la cause existe
	 */
	public static Boolean checkCause(Throwable cause, String causeSearch) {
		try {
			if (cause.getClass().getName().contains(causeSearch)) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * @param cause
	 * @param causeSearch
	 * @param lineNumber
	 * @return vérifie si la premiere cause de la stack appartient a une classe
	 */
	public static Boolean checkCauseByStackTrace(Throwable cause, String causeSearch, Integer lineNumber) {
		try {
			if (cause.getStackTrace()[lineNumber].getClassName().contains(causeSearch)) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * Pour vérifier le cas des "NullPointerException" sans stack
	 * 
	 * @param cause
	 * @return true si il n'y a pas de stackTrace
	 */
	public static Boolean checkCauseEmpty(Throwable cause) {
		try {
			if (cause.getStackTrace() == null || cause.getStackTrace().length == 0) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * @param value
	 * @return le string modifié en upperCase et sans espace à la fin
	 */
	public static String cleanForApogee(String value) {
		if (value == null) {
			return null;
		}
		value = value.replaceAll("\\s+$", "");
		return value.toUpperCase();
	}

	/**
	 * @param typGestionCandidature
	 * @return true si le mode de gestion de candidature est centre de candidature
	 */
	public static boolean isGestionCandidatureCtrCand(String typGestionCandidature) {
		if (typGestionCandidature == null) {
			return false;
		}
		return typGestionCandidature.equals(ConstanteUtils.TYP_GESTION_CANDIDATURE_CTR_CAND);
	}

	/**
	 * @param typGestionCandidature
	 * @return true si le mode de gestion de candidature est candidat
	 */
	public static boolean isGestionCandidatureCandidat(String typGestionCandidature) {
		if (typGestionCandidature == null) {
			return false;
		}
		return typGestionCandidature.equals(ConstanteUtils.TYP_GESTION_CANDIDATURE_CANDIDAT);
	}

	/**
	 * @param typGestionCandidature
	 * @return true si le mode de gestion de candidature est commission
	 */
	public static boolean isGestionCandidatureCommission(String typGestionCandidature) {
		if (typGestionCandidature == null) {
			return false;
		}
		return typGestionCandidature.equals(ConstanteUtils.TYP_GESTION_CANDIDATURE_COMMISSION);
	}

	/**
	 * @param fileNameDefault
	 * @param codeLangue
	 * @param codLangueDefault
	 * @return le template XDocReport
	 */
	public static InputStream getXDocReportTemplate(String fileNameDefault, String codeLangue,
			String codLangueDefault) {
		String resourcePath = "/template/";
		String extension = ConstanteUtils.TEMPLATE_EXTENSION;
		InputStream in = null;
		if (codeLangue != null && !codeLangue.equals(codLangueDefault)) {
			in = MethodUtils.class.getResourceAsStream(resourcePath + fileNameDefault + "_" + codeLangue + extension);
		}

		if (in == null) {
			in = MethodUtils.class.getResourceAsStream(resourcePath + fileNameDefault + extension);
			if (in == null) {
				return null;
			}
		}
		return in;
	}

	/**
	 * @param email
	 * @return true si l'adrese est correcte
	 */
	public static boolean isValidEmailAddress(String email) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

	/**
	 * @param id
	 * @param listeId
	 * @return true si l'id est trouvé dans la liste
	 */
	public static boolean isIdInListId(Integer id, List<Integer> listeId) {
		if (listeId == null) {
			return false;
		}
		return listeId.stream().filter(i -> i.equals(id)).findAny().isPresent();
	}

	/**
	 * Fonction Modulo calculant le modulo-->Rouen
	 * 
	 * @param a
	 * @param b
	 * @return le modulo
	 */
	private static Integer modulo(Long a, int b) {

		Long quotient;
		int mod;
		quotient = a / b;
		mod = (int) (a - (quotient * b));
		return (mod);
	}

	/**
	 * Méthode vérifiant la validité d'un numéro INE basé en code 23-->Rouen
	 * (principalement issu d'un rectorat)
	 * 
	 * @param bea23
	 * @return true si l'ine est ok
	 */
	private static boolean checkBEA23(String bea23) {

		boolean isBea23 = true;
		String localBea = bea23.toUpperCase();

		if ((localBea.length() < 11) || // Ine a la bonne longueur ?
				(!localBea.matches("^[0-9]{10}[A-Z]{1}$")) // INE RECTORAT est
															// écrit
															// correctement ?
		) {
			isBea23 = false;
		} else {
			// Décomposition du BEA

			String beaSansCle = localBea.substring(0, 10);
			char lettreCle = localBea.charAt(10);
			/*
			 * String academie = localBea.substring(0, 2); String anneeScol =
			 * localBea.substring(2, 2); String numSeq = localBea.substring(4, 6);
			 */
			// Calcul de la somme des 10 caractères
			Long extractBea = Long.parseLong(localBea.substring(0, 10));
			Integer moduloBea = modulo(extractBea, 23);

			// Génération de l'alphabet de 23 caractères sans les lettres I, O,
			// Q
			String alphabet23 = "ABCDEFGHJKLMNPRSTUVWXYZ";

			// Calcul de la clé réelle attendu
			char cleCalcule = alphabet23.charAt(moduloBea);

			// Opération de vérification du numéro INE sans clé
			// Vérification de la forme sur les 10 premiers caractères
			// <> succession du même chiffre sur 10
			if (beaSansCle.matches("^[0]{10}$") || beaSansCle.matches("^[1]{10}$") || beaSansCle.matches("^[2]{10}$")
					|| beaSansCle.matches("^[3]{10}$") || beaSansCle.matches("^[4]{10}$")
					|| beaSansCle.matches("^[5]{10}$") || beaSansCle.matches("^[6]{10}$")
					|| beaSansCle.matches("^[7]{10}$") || beaSansCle.matches("^[8]{10}$")
					|| beaSansCle.matches("^[9]{10}$")) {
				isBea23 = false;
			}
			// contrÃ´le supplémentaire sur la clé, elle ne doit pas être égale
			// Ã 'I','O','Q'
			// sinon clé invalide
			if ((lettreCle == 'I') || (lettreCle == 'O') || (lettreCle == 'Q')) {
				isBea23 = false;
			}
			// si clé passé en paramètre <> cleCalcule alors ineBea23 pas bon
			if (lettreCle != cleCalcule) {
				isBea23 = false;
			}
		}
		return (isBea23);
	}

	/**
	 * méthode vérifiant la validité d'un numéro INE saisie en base 36 (ine
	 * universitaire)-->Rouen
	 * 
	 * @param nne36
	 * @return si l'ine est ok
	 */
	private static boolean checkNNE36(String nne36) {

		boolean isNNE36 = true;

		if (nne36.length() < 11) {
			isNNE36 = false;
		} else {
			String localNNE36 = nne36.toUpperCase();
			String extractNNE = localNNE36.substring(0, 10);
			char cleNNE36 = localNNE36.charAt(10);
			char cleCalc;

			// Opération de vérification du numéro INE sans clé
			// Vérification de la forme sur les 10 premiers caractères
			// <> succession du même chiffre sur 10
			// ou numéro non écrit correctement

			if (extractNNE.matches("^[0]{10}$") || extractNNE.matches("^[1]{10}$") || extractNNE.matches("^[2]{10}$")
					|| extractNNE.matches("^[3]{10}$") || extractNNE.matches("^[4]{10}$")
					|| extractNNE.matches("^[5]{10}$") || extractNNE.matches("^[6]{10}$")
					|| extractNNE.matches("^[7]{10}$") || extractNNE.matches("^[8]{10}$")
					|| extractNNE.matches("^[9]{10}$") || extractNNE.matches("^[A-Z]{10}$")
					|| !localNNE36.matches("^[0-9A-Z]{10}[0-9]|[A-Z]{1}$")) {
				isNNE36 = false;
			}

			// Calcul de la clé
			int sum = 0;
			int valeurInt = 0;
			//
			for (int i = 0; i < 9; i++) {
				valeurInt = Integer.parseInt(String.valueOf(localNNE36.charAt(i)), 36);
				sum += (valeurInt * 6);
			}
			char dernierChar = extractNNE.charAt(extractNNE.length() - 1);
			sum += Integer.parseInt(String.valueOf(dernierChar), 36);
			cleCalc = Integer.toString(sum).charAt(Integer.toString(sum).length() - 1);
			//
			if (cleCalc != cleNNE36) {
				isNNE36 = false;
			}
		} // fin du else
		return (isNNE36);
	}

	/**
	 * fonction assurant la vérification d'un numéro INE passé en paramètre
	 * 
	 * @param theStudentINE
	 * @return boolean true si l'ine est ok
	 */
	public static boolean checkStudentINE(String theStudentINE, String theStudentINEKey) {
		try {
			if (theStudentINE == null || theStudentINE.length() == 0 || theStudentINEKey == null
					|| theStudentINEKey.length() == 0) {
				return true;
			}
			theStudentINE += theStudentINEKey;

			if (theStudentINE != null) {
				theStudentINE = theStudentINE.replaceAll(" ", "");
			}

			boolean isIneCorrect = true;
			//
			if (checkBEA23(theStudentINE) && (!checkNNE36(theStudentINE))) {
				isIneCorrect = true;
			} else if ((!checkBEA23(theStudentINE)) && (checkNNE36(theStudentINE))) {
				isIneCorrect = true;
			} else if ((!checkBEA23(theStudentINE)) && (!checkNNE36(theStudentINE))) {
				isIneCorrect = false;
			}

			return (isIneCorrect);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Methode utilitaire pour nettoyer les string (erreur au téléchargement du
	 * dossier)
	 * 
	 * @param xmlstring
	 * @return le string nettoyé
	 */
	public static String stripNonValidXMLCharacters(String xmlstring) {
		if (xmlstring == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < xmlstring.length(); i++) {
			char c = xmlstring.charAt(i);
			if (XMLChar.isValid(c)) {
				sb.append(c);
			}
		}
		xmlstring = sb.toString();

		// peut être pas utile mais je le laisse qd meme..
		if (xmlstring.contains("\0")) {
			xmlstring = xmlstring.replaceAll("\0", "");
		}
		return xmlstring;
	}

	/**
	 * Ferme une ressource closeable
	 * 
	 * @param ressource
	 *            la ressource a fermer
	 */
	public static void closeRessource(Closeable ressource) {
		try {
			if (ressource != null) {
				ressource.close();
				ressource = null;
			}
		} catch (Exception e) {
		}
	}

	/**
	 * @return la locale par défaut
	 */
	public static Locale getLocale() {
		try {
			Locale locale = UI.getCurrent().getLocale();
			if (locale != null) {
				return locale;
			}
		} catch (Exception e) {
		}
		return new Locale("fr");
	}

	/**
	 * @param codCouleur
	 * @param description
	 * @return un carré Html coloré
	 */
	public static String getHtmlColoredSquare(String codCouleur, String description, Integer size, String extraCss) {
		if (codCouleur == null) {
			codCouleur = "#FFFFFF";
		}
		if (extraCss == null) {
			extraCss = "";
		}
		if (description == null) {
			description = "";
		} else {
			description = "title='" + description + "'";
		}
		return "<div " + description + " style='" + extraCss + "display:inline-block;border:1px solid;width:" + size
				+ "px;height:" + size + "px;background:" + codCouleur + ";'></div>";
	}

	/**
	 * @param object
	 * @param prop
	 * @return le type d'un champ d'un objet
	 */
	public static Class<?> getClassProperty(Class<?> object, String prop) {
		try {
			if (object == null || prop == null) {
				return null;
			}
			if (prop.contains(".")) {
				StringTokenizer st = new StringTokenizer(prop, ".");
				while (st.hasMoreTokens()) {
					object = object.getDeclaredField(st.nextToken()).getType();
				}
				return object;
			} else {
				return object.getDeclaredField(prop).getType();
			}
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param number
	 * @return une valeur en long pour les totaux (prend en compte les null)
	 */
	public static Long getLongValue(Long number) {
		if (number == null) {
			return new Long(0);
		}
		return number;
	}
}
