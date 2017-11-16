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
package fr.univlorraine.ecandidat.utils.bean.mail;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

import lombok.Data;

@Data
public class MailBean implements Serializable{

	/**serialVersionUID**/
	private static final long serialVersionUID = -7146699521220262510L;

	/** Renvoie la valeur de la propriété du bean
	 * @param property
	 * @return la valeur de la propriété 
	 */
	public String getValueProperty(String property){
		try {			
			String valueProperty = BeanUtils.getProperty(this, property);
			if (valueProperty!=null){
				return valueProperty.toString();
			}else{
				return "";
			}
		} catch (Exception e) {
			return "";
		}
	}
}