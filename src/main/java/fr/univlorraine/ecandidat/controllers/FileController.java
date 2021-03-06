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
package fr.univlorraine.ecandidat.controllers;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

import fi.solita.clamav.ClamAVClient;
import fr.univlorraine.ecandidat.entities.ecandidat.Candidature;
import fr.univlorraine.ecandidat.entities.ecandidat.Fichier;
import fr.univlorraine.ecandidat.entities.ecandidat.FichierFiabilisation;
import fr.univlorraine.ecandidat.entities.ecandidat.Fichier_;
import fr.univlorraine.ecandidat.entities.ecandidat.PjCandidat;
import fr.univlorraine.ecandidat.repositories.FichierFiabilisationRepository;
import fr.univlorraine.ecandidat.repositories.FichierRepository;
import fr.univlorraine.ecandidat.services.file.FileCustom;
import fr.univlorraine.ecandidat.services.file.FileException;
import fr.univlorraine.ecandidat.services.file.FileManager;
import fr.univlorraine.ecandidat.services.siscol.SiScolException;
import fr.univlorraine.ecandidat.utils.ByteArrayInOutStream;
import fr.univlorraine.ecandidat.utils.ConstanteUtils;
import fr.univlorraine.ecandidat.utils.bean.presentation.PjPresentation;

/**
 * Controller gérant les appels fichier
 * @author Kevin Hergalant
 *
 */
@Component
public class FileController {
	
	private Logger logger = LoggerFactory.getLogger(FileController.class);
		
	/*applicationContext pour les messages*/
	@Resource
	private transient ApplicationContext applicationContext;
	
	@Resource
	private transient FileManager fileManager;
	
	@Resource
	private transient FileManager fileManagerSecondaire;
	
	@Resource
	private transient ParametreController parametreController;
	
	@Resource
	private transient CandidatPieceController candidatPieceController;
	
	@Resource
	private transient FichierRepository fichierRepository;
	
	@Resource
	private transient FichierFiabilisationRepository fichierFiabilisationRepository;
	
	@Resource
	private transient ClamAVClient clamAVClientScanner;
	
	/** Mode de dematerialisation
	 * @return le mode de demat
	 */
	public String getModeDemat(){
		if (parametreController.getIsUtiliseDemat() && fileManager!=null){
			return fileManager.getType();
		}
		return ConstanteUtils.TYPE_FICHIER_STOCK_NONE;
	}
	
	/** Mode de dematerialisation secondaire
	 * @return le mode de demat
	 */
	public String getModeDematSecondaire(){
		if (parametreController.getIsUtiliseDemat() && fileManagerSecondaire!=null){
			return fileManagerSecondaire.getType();
		}
		return ConstanteUtils.TYPE_FICHIER_STOCK_NONE;
	}
	
	/** Mode de dematerialisation
	 * @return le mode de demat pour les pièces backoffice-->pas besoin du paramètre IsUtiliseDemat
	 */
	public String getModeDematBackoffice(){
		if (fileManager!=null){
			return fileManager.getType();
		}
		return ConstanteUtils.TYPE_FICHIER_STOCK_NONE;
	}
	
	/** Mode de dematerialisation backoffice secondaire
	 * @return le mode de demat pour les pièces backoffice-->pas besoin du paramètre IsUtiliseDemat
	 */
	public String getModeDematBackofficeSecondaire(){
		if (fileManagerSecondaire!=null){
			return fileManagerSecondaire.getType();
		}
		return ConstanteUtils.TYPE_FICHIER_STOCK_NONE;
	}
	
	/**
	 * Teste la démat
	 */
	public Boolean testDemat(Boolean showNotifIfOk){
		if (fileManager!=null){
			if (!fileManager.testSession()){
				Notification.show(applicationContext.getMessage("parametre.demat.check.ko", null, UI.getCurrent().getLocale()));			
				return false;
			}else{
				if (showNotifIfOk){
					Notification.show(applicationContext.getMessage("parametre.demat.check.ok", null, UI.getCurrent().getLocale()));
				}				
				return true;
			}
		}else{
			Notification.show(applicationContext.getMessage("parametre.demat.check.disable", null, UI.getCurrent().getLocale()));	
			return false;
		}
	}
	
	/** Verifie quele nom de fichier n'est pas trop long
	 * @return la taille max d'un nom de fichier
	 */
	public Integer getSizeMaxFileName(){
		try {
			return Fichier.class.getDeclaredField(Fichier_.nomFichier.getName()).getAnnotation(Size.class).max();
		} catch (NoSuchFieldException | SecurityException e) {
			return 0;
		}
	}
	
	/** Verifie si le nom du fichier est correct
	 * @param fileName
	 * @param sizeMax
	 * @return true si le nom de fichier est ok
	 */
	public Boolean isFileNameOk(String fileName, Integer sizeMax){
		if (fileName==null || fileName.length()==0){
			return false;
		}else{
			return fileName.length()<sizeMax;
		}
	}

	/** Créé un fichier provenant de l'upload
	 * @param file
	 * @param mimeType
	 * @param filename
	 * @param length
	 * @param typFile
	 * @param prefixe
	 * @param candidature 
	 * @return le fichier
	 */
	public FileCustom createFileFromUpload(ByteArrayInOutStream file,
			String mimeType, String filename, long length, String typFile, String prefixe, Candidature candidature, Boolean commune) {
		try {
			scanDocument(file);
			return fileManager.createFileFromUpload(file,mimeType,filename,length, typFile, prefixe, candidature, commune);
		} catch (FileException e) {
			Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
			return null;
		}
	}
	
	/** Renvoie l'inputstream d'une piece
	 * @param pieceJustif
	 * @return l'InputStream d'un fichier
	 */
	public InputStream getInputStreamFromPjPresentation(PjPresentation pieceJustif){
		if (pieceJustif.getPjCandidatFromApogee() != null){
			return getInputStreamFromPjCandidat(pieceJustif.getPjCandidatFromApogee());
		}else{
			return getInputStreamFromFichier(pieceJustif.getFilePj());
		}
	}
	
	/**
	 * @param pjCandidat
	 * @return l'InputStream d'un fichier apogee
	 */
	public InputStream getInputStreamFromPjCandidat(PjCandidat pjCandidat){
		try {
			InputStream is = candidatPieceController.getInputStreamFromFichier(pjCandidat);
			if (is == null){
				Notification.show(applicationContext.getMessage("file.error.stream", new Object[]{pjCandidat.getNomFicPjCandidat()}, UI.getCurrent().getLocale()), Type.WARNING_MESSAGE);
			}
			return is;
		} catch (SiScolException e) {
			Notification.show(applicationContext.getMessage("file.error.stream", new Object[]{pjCandidat.getNomFicPjCandidat()}, UI.getCurrent().getLocale()), Type.WARNING_MESSAGE);
			return null;
		}
	}
	
	/** Renvoie l'inputstream d'un fichier
	 * @param fichier
	 * @return l'InputStream d'un fichier
	 */
	public InputStream getInputStreamFromFichier(Fichier fichier){
		return getInputStreamFromFichier(fichier, true);
	}
	
	/** Renvoie l'inputstream d'un fichier (creation d'une seconde methode pour traiter dans un batch et desactiver la notif
	 * @param fichier
	 * @return l'InputStream d'un fichier
	 */
	public InputStream getInputStreamFromFichier(Fichier fichier, Boolean showNotif){
		Boolean isBackoffice = false;
		if (fichier.getTypFichier().equals(ConstanteUtils.TYPE_FICHIER_GESTIONNAIRE)){
			isBackoffice = true;
		}
		if (!isModeFileStockageOk(fichier, isBackoffice)){			
			return null;
		}
		try {			
			if (isModeStockagePrincipalOk(fichier, isBackoffice)){
				return fileManager.getInputStreamFromFile(fichier, true);
			}else if (isModeStockageSecondaireOk(fichier, isBackoffice)){
				return fileManagerSecondaire.getInputStreamFromFile(fichier, true);
			}
			return null;
		} catch (FileException e) {
			if (showNotif){
				Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
			}
			return null;
		}
	}
	
	/** Verifie le mode de stockage d'un fichier et de l'application, si différent --> erreur
	 * @param fichier
	 * @param isBackoffice boolean pour indiquer que les pièces proviennent du backoffice
	 * @return true si le mode de stockage est ok
	 */
	public Boolean isModeFileStockageOk(Fichier fichier, Boolean isBackoffice){
		if (!isModeStockagePrincipalOk(fichier, isBackoffice) && !isModeStockageSecondaireOk(fichier, isBackoffice)){
			Notification.show(applicationContext.getMessage("file.error.mode", null, UI.getCurrent().getLocale()), Type.WARNING_MESSAGE);
			return false;
		}
		return true;
	}
	
	
	/** Verifie le mode de stockage d'un fichier et de l'application sur le fileSystem principal, si différent --> erreur
	 * @param fichier
	 * @param isBackoffice boolean pour indiquer que les pièces proviennent du backoffice
	 * @return true si le mode de stockage est ok
	 */
	private Boolean isModeStockagePrincipalOk(Fichier fichier, Boolean isBackoffice){
		if (fichier == null){
			return true;
		}
		
		String modeDemat = null;
		if (isBackoffice){
			modeDemat = getModeDematBackoffice();
		}else{
			modeDemat = getModeDemat();
		}
		
		if (!fichier.getTypStockageFichier().equals(modeDemat)){			
			return false;
		}
		return true;
	}
	
	/** Verifie le mode de stockage d'un fichier et de l'application sur le fileSystem secondaire, si différent --> erreur
	 * @param fichier
	 * @param isBackoffice boolean pour indiquer que les pièces proviennent du backoffice
	 * @return true si le mode de stockage est ok
	 */
	private Boolean isModeStockageSecondaireOk(Fichier fichier, Boolean isBackoffice){
		if (fichier == null){
			return true;
		}
		
		String modeDemat = null;
		if (isBackoffice){
			modeDemat = getModeDematBackofficeSecondaire();
		}else{
			modeDemat = getModeDematSecondaire();
		}
		
		if (!fichier.getTypStockageFichier().equals(modeDemat)){
			return false;
		}
		return true;
	}
		
	/** Créé un fichier à partir d'un customFile
	 * @param file
	 * @param user 
	 * @return le fichier créé
	 */
	public Fichier createFile(FileCustom file, String user, String typFichier){
		String lib = file.getFileName().replaceAll(" ", "_");
		Fichier fichierToSave = new Fichier(file.getCod(),file.getId(),lib,typFichier,fileManager.getType(),user);
		return fichierRepository.save(fichierToSave);
	}
	
	/** Supprime un fichier
	 * @param fichier
	 * @throws FileException
	 */
	/*@Transactional(rollbackFor=FileException.class)
	public void deleteFichier(Fichier fichier, Boolean isBackoffice) throws FileException{
		if (!isModeFileStockageOk(fichier, isBackoffice)){
			throw new FileException(applicationContext.getMessage("file.error.mode", null, UI.getCurrent().getLocale()));
		}
		fichierRepository.delete(fichier);
		if (isModeStockagePrincipalOk(fichier, isBackoffice)){
			fileManager.deleteFile(fichier, true);
		}else if (isModeStockageSecondaireOk(fichier, isBackoffice)){
			fileManagerSecondaire.deleteFile(fichier, true);
		}			
	}*/
	
	/** Supprime un fichier
	 * @param fichier
	 * @throws FileException
	 */
	public void deleteFichier(Fichier fichier) throws FileException{
		Boolean isBackoffice = false;
		if (fichier.getTypFichier().equals(ConstanteUtils.TYPE_FICHIER_GESTIONNAIRE)){
			isBackoffice = true;
		}
		fichierRepository.delete(fichier);
		if (!isModeFileStockageOk(fichier, isBackoffice)){
			throw new FileException(applicationContext.getMessage("file.error.mode", null, UI.getCurrent().getLocale()));
		}
		if (isModeStockagePrincipalOk(fichier, isBackoffice)){
			fileManager.deleteFile(fichier, false);
		}else if (isModeStockageSecondaireOk(fichier, isBackoffice)){
			fileManagerSecondaire.deleteFile(fichier, false);
		}			
	}
	
	/**
	 * @param fichier
	 * @return true si le fichier existe en base
	 * @throws FileException
	 */
	public Boolean existFileInDb(Fichier fichier) {
		if (fichier == null || fichier.getIdFichier()==null){
			Notification.show(applicationContext.getMessage("pj.gestionnaire.modified", null, UI.getCurrent().getLocale()), Type.WARNING_MESSAGE);
			return false;
		}
		Boolean exist = fichierRepository.exists(fichier.getIdFichier());
		if (!exist){
			Notification.show(applicationContext.getMessage("pj.gestionnaire.modified", null, UI.getCurrent().getLocale()), Type.WARNING_MESSAGE);
			return false;
		}
		return true;
	}

	/** Vérifie qu'un fichier existe
	 * @param fichier
	 * @return true si le fichier existe
	 * @throws FileException
	 */
	public Boolean existFile(Fichier fichier) throws FileException {
		if (!isModeFileStockageOk(fichier, false)){
			throw new FileException(applicationContext.getMessage("file.error.mode", null, UI.getCurrent().getLocale()));
		}
		if (isModeStockagePrincipalOk(fichier, false)){
			return fileManager.existFile(fichier);
		}else if (isModeStockageSecondaireOk(fichier, false)){
			return fileManagerSecondaire.existFile(fichier);
		}
		return false;
	}
	
	/** Detruit le fichier racine de la campagne
	 * @param codCampagne
	 * @throws FileException 
	 */
	public Boolean deleteCampagneFolder(String codCampagne) throws FileException{
		return fileManager.deleteCampagneFolder(codCampagne);
	}
	
	//@Scheduled(fixedRateString="${fiabilisation.fichier.refresh.fixedRate}")
	//@Scheduled(fixedRateString="30000")
    /** Batch de fiabilisation des fichiers
     * @param dateArchivageCampagne
     */
    public void launchFiabilisationFichier(LocalDateTime dateArchivageCampagne) {
		logger.debug("Lancement batch fiabilisation fichiers");
		
		//Suppression des fichiers orphelins et créés depuis plus d'un jour
		List<Fichier> listOrphans = fichierRepository.findFichierOrphelin(dateArchivageCampagne);		
		logger.debug("Suppression fichiers orphelins : "+listOrphans.size()+" fichiers a supprimer");
		Integer i = 0;
		Integer nb = 0;
		for (Fichier fichier : listOrphans){
			try {
				List<Fichier> listFileFichier = fichierRepository.findByFileFichierAndIdFichierNot(fichier.getFileFichier(), fichier.getIdFichier());
				if (listFileFichier.size() == 0){
					logger.trace("Fichiers orphelins non trouvés avec le meme Fichier ("+fichier.getFileFichier()+")");
					FichierFiabilisation fichierFiabilisation = new FichierFiabilisation(fichier);
					fichierFiabilisation = fichierFiabilisationRepository.save(fichierFiabilisation);
					deleteFichier(fichier);
					fichierFiabilisationRepository.delete(fichierFiabilisation);
				}else{
					logger.trace("Fichiers orphelins trouvés avec le meme Fichier ("+fichier.getFileFichier()+"), aucune suppression");
					fichierRepository.delete(fichier);
				}				
			} catch (Exception e) {}
			i++;
			nb++;
			if (i.equals(1000)){
				logger.debug("Batch de destruction fichiers orphelins, destruction de "+nb+" fichiers ok");
				i = 0;
			}
		}
		//Suppression des fichiers non supprimés		
		List<FichierFiabilisation> listFichierFiab = fichierFiabilisationRepository.findByDatCreFichierBefore(dateArchivageCampagne);
		logger.debug("Suppression fichiers à fiabiliser : "+listFichierFiab.size()+" fichiers a supprimer");
		i = 0;
		nb = 0;
		for (FichierFiabilisation fichierFiab : listFichierFiab){
			logger.trace("Début fiabilisation de : "+fichierFiab);
			if (fichierFiab.getIdFichier()!=null && fichierFiab.getFileFichier()!=null){
				Fichier file = fichierRepository.findOne(fichierFiab.getIdFichier());
				if (file == null){
					List<Fichier> listFileFichier = fichierRepository.findByFileFichier(fichierFiab.getFileFichier());
					if (listFileFichier.size()==0){
						logger.trace("Fiabilisation activée pour : "+fichierFiab);
						Fichier fichier = new Fichier(fichierFiab.getCodFichier(), fichierFiab.getFileFichier(), fichierFiab.getNomFichier(),
								fichierFiab.getTypFichier(), fichierFiab.getTypStockageFichier(), fichierFiab.getAuteurFichier());
						Boolean isBackoffice = false;
						if (fichier.getTypFichier().equals(ConstanteUtils.TYPE_FICHIER_GESTIONNAIRE)){
							isBackoffice = true;
						}						
						try{
							if (!isModeFileStockageOk(fichier, isBackoffice)){
								return;
							}
							if (isModeStockagePrincipalOk(fichier, isBackoffice)){
								logger.trace("Suppression fichier activée pour : "+fichierFiab);
								fileManager.deleteFile(fichier, false);
							}else if (isModeStockageSecondaireOk(fichier, isBackoffice)){
								logger.trace("Suppression fichier activée pour : "+fichierFiab);
								fileManagerSecondaire.deleteFile(fichier, false);
							}
							logger.trace("Suppression ligne fiabilisation pour : "+fichierFiab);
							fichierFiabilisationRepository.delete(fichierFiab);
						}catch(Exception ex){
							try{
								if (!existFile(fichier)){
									logger.trace("Suppression ligne fiabilisation (fichier non existant) suite à erreur pour : "+fichierFiab);
									fichierFiabilisationRepository.delete(fichierFiab);
								}
							}catch(Exception ex1){}
						}
					}else{
						logger.trace("Pas de fiabilisation (File fichier trouvé) pour : "+fichierFiab);
						fichierFiabilisationRepository.delete(fichierFiab);
					}
				}else{
					logger.trace("Pas de fiabilisation (Id fichier trouvé) pour : "+fichierFiab);
					fichierFiabilisationRepository.delete(fichierFiab);
				}
			}
			i++;
			nb++;
			if (i.equals(1000)){
				logger.debug("Batch de destruction fichiers fiabilisation, destruction de "+nb+" fichiers ok");
				i = 0;
			}
		}
    }
			
	/** Analyse un fichier et renvoie une exception si erreur
	 * @param file
	 * @throws FileException
	 */
	private void scanDocument(ByteArrayInOutStream file) throws FileException {
		/*Teste si ClamAV est configuré*/
		if (clamAVClientScanner==null){
			return;
		}
		/*On scan l'objet-->Exception si erreur avec l'antivirus*/
		byte[] reply = null;
		try {
			reply = clamAVClientScanner.scan(file.getByte());			
		} catch (Exception e) {
			logger.error(applicationContext.getMessage("file.error.scan.error", null, UI.getCurrent().getLocale()), e);
			throw new FileException(applicationContext.getMessage("file.error.scan.error", null, UI.getCurrent().getLocale()), e);
		}
		/*On vérifie que le scan a donné un résultat-->Exception si erreur avec l'antivirus*/
		if (reply == null){
			throw new FileException(applicationContext.getMessage("file.error.scan.error", null, UI.getCurrent().getLocale()));
		}
		/*On vérifie le test du scan-->NOK=Virus-->Exception*/
		if (!ClamAVClient.isCleanReply(reply)) {
			logger.debug("Scan du fichier NOK : VIRUS");
			throw new FileException(applicationContext.getMessage("file.error.scan.virus", null, UI.getCurrent().getLocale()));
		}
	}
}
