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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

import fr.univlorraine.ecandidat.entities.ecandidat.Campagne;
import fr.univlorraine.ecandidat.entities.ecandidat.Candidature;
import fr.univlorraine.ecandidat.entities.ecandidat.CompteMinima;
import fr.univlorraine.ecandidat.entities.ecandidat.I18n;
import fr.univlorraine.ecandidat.repositories.CampagneRepository;
import fr.univlorraine.ecandidat.repositories.CandidatureRepository;
import fr.univlorraine.ecandidat.repositories.CompteMinimaRepository;
import fr.univlorraine.ecandidat.utils.NomenclatureUtils;
import fr.univlorraine.ecandidat.views.windows.AdminCampagneWindow;
import fr.univlorraine.ecandidat.views.windows.ConfirmWindow;

/**
 * Gestion de l'entité campagne
 * 
 * @author Kevin Hergalant
 *
 */
@Component
public class CampagneController {

	private Logger logger = LoggerFactory.getLogger(CampagneController.class);

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient LockController lockController;
	@Resource
	private transient ParametreController parametreController;
	@Resource
	private transient I18nController i18nController;
	@Resource
	private transient CampagneRepository campagneRepository;
	@Resource
	private transient CompteMinimaRepository compteMinimaRepository;
	@Resource
	private transient CandidatureRepository candidatureRepository;
	@Resource
	private transient CacheController cacheController;

	/**
	 * @return liste des campagnes
	 */
	public List<Campagne> getCampagnes() {
		return campagneRepository.findAll();
	}

	/**
	 * @return la campagne active
	 */
	public Campagne getCampagneEnServiceToCache() {
		List<Campagne> liste = campagneRepository.findByTesCampAndDatArchivCampIsNull(true);
		if (liste == null || liste.size() == 0) {
			return null;
		}
		return liste.get(0);
	}

	/**
	 * @return la campagne active
	 */
	public Campagne getCampagneActive() {
		Campagne campagne = cacheController.getCampagneEnService();
		if (campagne == null) {
			return null;
		}

		if (campagne.getDatFinCamp().isBefore(LocalDate.now()) || campagne.getDatDebCamp().isAfter(LocalDate.now())) {
			return null;
		}

		return campagne;
	}

	/**
	 * @return true si la campagne est ouverte aux candidats
	 */
	public Boolean isCampagneActiveCandidat(Campagne campagne) {
		if (campagne == null) {
			return false;
		} else if (campagne.getDatFinCandidatCamp() != null
				&& campagne.getDatFinCandidatCamp().isBefore(LocalDate.now())) {
			return false;
		}
		return true;
	}

	/**
	 * Ouvre une fenêtre d'édition d'un nouveau campagne.
	 */
	public void editNewCampagne() {
		List<Campagne> listeCampagneToActivate = campagneRepository
				.findByDatActivatPrevCampIsNotNullAndDatActivatEffecCampIsNull();
		if (listeCampagneToActivate.size() > 0) {
			Notification.show(applicationContext.getMessage("campagne.error.new", null, UI.getCurrent().getLocale()),
					Type.WARNING_MESSAGE);
			return;
		}

		Campagne camp = cacheController.getCampagneEnService();
		/* Verrou */
		if (camp != null && !lockController.getLockOrNotify(camp, null)) {
			return;
		}

		Campagne nouvelleCampagne = new Campagne();
		nouvelleCampagne
				.setI18nLibCamp(new I18n(i18nController.getTypeTraduction(NomenclatureUtils.TYP_TRAD_CAMP_LIB)));

		AdminCampagneWindow window = new AdminCampagneWindow(nouvelleCampagne, camp);
		window.addCloseListener(e -> {
			if (camp != null) {
				lockController.releaseLock(camp);
			}
		});
		UI.getCurrent().addWindow(window);

	}

	/**
	 * Ouvre une fenêtre d'édition de campagne.
	 * 
	 * @param campagne
	 */
	public void editCampagne(Campagne campagne) {
		Assert.notNull(campagne, applicationContext.getMessage("assert.notNull", null, UI.getCurrent().getLocale()));

		/* Verrou */
		if (!lockController.getLockOrNotify(campagne, null)) {
			return;
		}
		AdminCampagneWindow window = new AdminCampagneWindow(campagne, null);
		window.addCloseListener(e -> lockController.releaseLock(campagne));
		UI.getCurrent().addWindow(window);
	}

	/**
	 * Enregistre un campagne
	 * 
	 * @param campagne
	 * @param campagneAArchiver
	 */
	public void saveCampagne(Campagne campagne, Campagne campagneAArchiver) {
		Assert.notNull(campagne, applicationContext.getMessage("assert.notNull", null, UI.getCurrent().getLocale()));
		if (campagne.getIdCamp() == null) {
			if (campagneRepository.findAll().size() == 0) {
				campagne.setTesCamp(true);
				campagne.setDatActivatPrevCamp(LocalDateTime.now());
				campagne.setDatActivatEffecCamp(LocalDateTime.now());
			} else {
				campagne.setTesCamp(false);
			}
		}

		/* Verrou */
		if (!lockController.getLockOrNotify(campagne, null)) {
			return;
		}

		if (campagneAArchiver != null) {
			if (!lockController.getLockOrNotify(campagneAArchiver, null)) {
				return;
			}
			campagne.setCampagneArchiv(campagneAArchiver);
		}
		campagne.setI18nLibCamp(i18nController.saveI18n(campagne.getI18nLibCamp()));
		campagne = campagneRepository.saveAndFlush(campagne);
		cacheController.reloadCampagneEnService(true);
		lockController.releaseLock(campagne);
		if (campagneAArchiver != null) {
			lockController.releaseLock(campagneAArchiver);
		}
	}

	/**
	 * Supprime une campagne
	 * 
	 * @param campagne
	 */
	public void deleteCampagne(Campagne campagne) {
		Assert.notNull(campagne, applicationContext.getMessage("assert.notNull", null, UI.getCurrent().getLocale()));

		if (campagne.getDatActivatEffecCamp() != null) {
			Notification.show(
					applicationContext.getMessage("campagne.error.delete.active", null, UI.getCurrent().getLocale()),
					Type.WARNING_MESSAGE);
			return;
		}

		if (compteMinimaRepository.countByCampagne(campagne) > 0) {
			Notification.show(
					applicationContext.getMessage("campagne.error.delete",
							new Object[] { CompteMinima.class.getSimpleName() }, UI.getCurrent().getLocale()),
					Type.WARNING_MESSAGE);
			return;
		}

		/* Verrou */
		if (!lockController.getLockOrNotify(campagne, null)) {
			return;
		}

		ConfirmWindow confirmWindow = new ConfirmWindow(
				applicationContext.getMessage("campagne.window.confirmDelete", new Object[] { campagne.getCodCamp() },
						UI.getCurrent().getLocale()),
				applicationContext.getMessage("campagne.window.confirmDeleteTitle", null, UI.getCurrent().getLocale()));
		confirmWindow.addBtnOuiListener(e -> {
			/* Contrôle que le client courant possède toujours le lock */
			if (lockController.getLockOrNotify(campagne, null)) {
				campagneRepository.delete(campagne);
				cacheController.reloadCampagneEnService(true);
				/* Suppression du lock */
				lockController.releaseLock(campagne);
			}
		});
		confirmWindow.addCloseListener(e -> {
			/* Suppression du lock */
			lockController.releaseLock(campagne);
		});
		UI.getCurrent().addWindow(confirmWindow);
	}

	/**
	 * Archive une campagne et active l'autre
	 */
	public void archiveCampagne() {
		logger.debug("Lancement du batch d'archivage de campagne");
		List<Campagne> listeCampagne = campagneRepository
				.findByDatActivatEffecCampIsNullAndDatActivatPrevCampIsNotNull();
		listeCampagne.forEach(campagne -> {
			if (campagne.getDatActivatPrevCamp().isBefore(LocalDateTime.now())) {
				logger.debug("Activation campagne : " + campagne);
				logger.debug("Archivage des candidatures pour la campagne : " + campagne.getCampagneArchiv());
				// On place les dates des formations dans les candidatures
				// candidatureController.archiveCandidatureDateFormation(campagne.getCampagneArchiv());
				List<Candidature> liste = candidatureRepository
						.findByCandidatCompteMinimaCampagne(campagne.getCampagneArchiv());
				Integer i = 0;
				Integer cpt = 0;
				for (Candidature candidature : liste) {
					candidature.setDatAnalyseForm(candidature.getFormation().getDatAnalyseForm());
					candidature.setDatConfirmForm(candidature.getFormation().getDatConfirmForm());
					candidature.setDatDebDepotForm(candidature.getFormation().getDatDebDepotForm());
					candidature.setDatFinDepotForm(candidature.getFormation().getDatFinDepotForm());
					candidature.setDatRetourForm(candidature.getFormation().getDatRetourForm());
					candidature.setDatPubliForm(candidature.getFormation().getDatPubliForm());
					candidature.setDatJuryForm(candidature.getFormation().getDatJuryForm());
					candidatureRepository.save(candidature);
					i++;
					cpt++;
					if (i.equals(1000)) {
						logger.debug("Archivage des candidatures : mise à jour des dates de formation pour " + cpt
								+ " candidatures ok");
						i = 0;
					}
				}

				campagne.setDatActivatEffecCamp(LocalDateTime.now());
				campagne.setTesCamp(true);
				campagne = campagneRepository.save(campagne);
				campagne.getCampagneArchiv().setDatArchivCamp(LocalDateTime.now());
				campagne.getCampagneArchiv().setTesCamp(false);
				campagneRepository.save(campagne.getCampagneArchiv());

				cacheController.reloadCampagneEnService(true);
				logger.debug("Activation campagne terminé : " + campagne);
			}
		});
		logger.debug("Fin batch d'archivage de campagne");
	}

	/**
	 * Enregistre la date de destruction de la campagne
	 * 
	 * @param campagne
	 * @return la campagne enregistrée
	 */
	public Campagne saveDateDestructionCampagne(Campagne campagne) {
		campagne.setDatDestructEffecCamp(LocalDateTime.now());
		campagne.setCompteMinimas(new ArrayList<CompteMinima>());
		return campagneRepository.save(campagne);
	}

	/**
	 * @param camp
	 * @return la date prévisionnelle de destruction de dossier
	 */
	public LocalDateTime getDateDestructionDossier(Campagne camp) {
		if (camp.getDatArchivCamp() != null) {
			return camp.getDatArchivCamp().plusDays(parametreController.getNbJourArchivage());
		}
		return null;
	}

	/**
	 * Verifie que le code de la campagne est unique
	 * 
	 * @param cod
	 * @param idCamp
	 * @return true si le code est unique
	 */
	public Boolean isCodCampUnique(String cod, Integer idCamp) {
		Campagne camp = campagneRepository.findByCodCamp(cod);
		if (camp == null) {
			return true;
		} else {
			if (camp.getIdCamp().equals(idCamp)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param campagne
	 * @return le libellé de la campagne
	 */
	public String getLibelleCampagne(Campagne campagne) {
		return getLibelleCampagne(campagne, null);
	}

	/**
	 * @param campagne
	 * @param codLangue
	 * @return le libellé de la campagne
	 */
	public String getLibelleCampagne(Campagne campagne, String codLangue) {
		if (campagne.getI18nLibCamp() != null) {
			if (codLangue != null) {
				return i18nController.getI18nTraduction(campagne.getI18nLibCamp(), codLangue);
			} else {
				return i18nController.getI18nTraduction(campagne.getI18nLibCamp());
			}
		} else {
			return campagne.getLibCamp();
		}
	}
}
