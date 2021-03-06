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
package fr.univlorraine.ecandidat.entities.ecandidat;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * The persistent class for the candidat_cursus_pro database table.
 * 
 */
@Entity
@Table(name="candidat_cursus_pro")
@Data @EqualsAndHashCode(of="idCursusPro")
@ToString(exclude={"candidat"})
public class CandidatCursusPro implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_cursus_pro", nullable=false)
	private Integer idCursusPro;

	@Column(name="objectif_cursus_pro", length=500)
	@Size(max = 500) 
	private String objectifCursusPro;

	@Column(name="duree_cursus_pro", nullable=false, length=20)
	@Size(max = 20) 
	@NotNull
	private String dureeCursusPro;

	@Column(name="intitule_cursus_pro", nullable=false, length=50)
	@Size(max = 50)
	@NotNull
	private String intituleCursusPro;

	@Column(name="organisme_cursus_pro", nullable=false, length=50)
	@Size(max = 50) 
	@NotNull
	private String organismeCursusPro;

	@Column(name="annee_cursus_pro", nullable=false)
	@NotNull
	private Integer anneeCursusPro;

	//bi-directional many-to-one association to Candidat
	@ManyToOne
	@JoinColumn(name="id_candidat", nullable=false)
	@NotNull
	private Candidat candidat;
}