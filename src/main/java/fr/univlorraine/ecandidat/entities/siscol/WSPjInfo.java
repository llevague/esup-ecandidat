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
package fr.univlorraine.ecandidat.entities.siscol;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.univlorraine.ecandidat.services.siscol.SiScolRestUtils.StringBooleanDeserializer;
import lombok.Data;

/**
 * The persistent class for the WSPjInfo WS Service
 * 
 */
@Data
public class WSPjInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String codAnu;
	private String codTpj;
	private String libTpj;
	private String nomFic;
	@JsonDeserialize(using=StringBooleanDeserializer.class)
	private Boolean temDemPJ;
	private String stuPj;
	private String mtfRefus;
	private String cmtMtfRefus;
	private String datDemPj;
	private String datRecPj;
	private String datRefus;
	private String datVal;
	private String datExp;	
	private String daaPreTra;
}