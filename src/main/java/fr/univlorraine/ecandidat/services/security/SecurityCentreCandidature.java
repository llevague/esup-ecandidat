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
package fr.univlorraine.ecandidat.services.security;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import fr.univlorraine.ecandidat.entities.ecandidat.CentreCandidature;
import fr.univlorraine.ecandidat.entities.ecandidat.DroitProfilFonc;

/**
 * La classe de centre candidature d'un user
 * @author Kevin Hergalant
 *
 */
@Data
public class SecurityCentreCandidature implements Serializable {
	
	/**serialVersionUID**/
	private static final long serialVersionUID = -6390831536479919293L;
	
	private Integer idCtrCand;
	private String libCtrCand;
	private String codCGE;
	private Boolean isAdmin;
	private List<DroitProfilFonc> listFonctionnalite;
	private Boolean isGestAllCommission;
	private List<Integer> listeIdCommission;

	public SecurityCentreCandidature(CentreCandidature centre, List<DroitProfilFonc> listFonctionnalite, String codCGE, Boolean isAdmin, Boolean isGestAllCommission, List<Integer> listeIdCommission) {
		this.idCtrCand = centre.getIdCtrCand();
		this.libCtrCand = centre.getLibCtrCand();
		this.listFonctionnalite = listFonctionnalite;
		this.codCGE = codCGE;
		this.isAdmin = isAdmin;
		this.isGestAllCommission = isGestAllCommission;
		this.listeIdCommission = listeIdCommission;
	}
}
