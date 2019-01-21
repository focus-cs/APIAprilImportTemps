/*
 * © 2011 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.service.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sciforma.psnext.api.DataFormatException;
import com.sciforma.psnext.api.DatedData;
import com.sciforma.psnext.api.FieldAccessor;
import com.sciforma.psnext.api.PSException;

import fr.sciforma.psconnect.exception.TechnicalException;
import org.pmw.tinylog.Logger;

/**
 * Classe outil pour transformer un FieldAccessor en une map d'objet
 * serialisable.
 */
public class MapSerializableAssembler {


	private MapSerializableAssembler() {
		// classe outil
	};

	/**
	 * créer une map de clé et valeur sérialisée à partir d'un objet PSNext
	 * héritant de FieldAccessor
	 * 
	 * @param fieldAccessor
	 * @param fieldnames
	 * @return a map<String, Serializable>
	 */
	public static Map<String, Serializable> createFieldAccessorToMap(
			FieldAccessor fieldAccessor, String[] fieldnames) {
		Map<String, Serializable> map = new LinkedHashMap<String, Serializable>();
		for (String fieldname : fieldnames) {
			try {
				Object value = fieldAccessor.getValue(fieldname);

				map.put(fieldname,
						toSerializableValue(fieldAccessor, fieldname, value));

			} catch (PSException e) {
				Logger.error(e.getMessage(), e);
				throw new TechnicalException(e, "Technical error on category <"
						+ fieldAccessor.getCategoryName() + "> field: <"
						+ fieldname + "> cause: <" + e.getMessage() + ">");
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	private static Serializable toSerializableValue(
			FieldAccessor fieldAccessor, String key, Object value)
			throws DataFormatException, PSException {
		if (value instanceof List) {
			// list kind
			return (Serializable) value;
		}

		List datedData = new LinkedList();

		// XXX: missing isDatedData

		try {
			datedData = fieldAccessor.getDatedData(key, DatedData.DAY);
		} catch (PSException e) {
			Logger.info("<" + key + "> n'est pas une valeur datée.");
		}

		if (value instanceof Double) {
			Double real = (Double) value;
			if (real == 0) {
				// unpredictable kind Simple or Distr, so nop;
				return null;
			}
			if (datedData.isEmpty()) {
				// simple kind;
				return real;
			}
			// distr kind
			return DatedDataExtractDTOAssembler.transferDoubleDatedData(
					datedData, new DatedDataExtractDTO<Double>(real));
		}

		if (value instanceof String) {
			String texte = (String) value;

			if (texte.length() == 0) {
				// unpredictable kind Simple or Distr, so nop;
				return null;
			}
			if (datedData.isEmpty()) {
				// simple kind;
				return texte;
			}
			// distr kind
			return DatedDataExtractDTOAssembler.transferStringDatedData(
					datedData, new DatedDataExtractDTO<String>(texte));
		}

		if (value instanceof Integer) {
			Integer integer = (Integer) value;
			if (integer == 0) {
				// unpredictable kind Simple or Distr, so nop;
				return null;
			}
			if (datedData.isEmpty()) {
				// simple kind;
				return integer;
			}
			// distr kind
			return DatedDataExtractDTOAssembler.transferIntegerDatedData(
					datedData, new DatedDataExtractDTO<Integer>(integer));
		}

		if (value instanceof Date) {
			Date date = (Date) value;
			if (date.getTime() == -1) {
				// unpredictable kind Simple or Distr, so nop;
				return null;
			}
			if (datedData.isEmpty()) {
				// simple kind;
				return date;
			}
			// distr kind
			return DatedDataExtractDTOAssembler.transferDateDatedData(
					datedData, new DatedDataExtractDTO<Date>(date));
		}

		if (value instanceof Boolean) {
			Boolean bool = (Boolean) value;
			if (!bool) {
				// unpredictable kind Simple or Distr, so nop;
				return null;
			}
			if (datedData.isEmpty()) {
				// simple kind;
				return bool;
			}
			// distr kind
			return DatedDataExtractDTOAssembler.transferBooleanDatedData(
					datedData, new DatedDataExtractDTO<Boolean>(bool));
		}

		Logger.error("Category <" + fieldAccessor.getCategoryName() + "> field: <"
				+ key + "> is not known datedData datatype value: <" + value
				+ ">");
		throw new TechnicalException("Category <"
				+ fieldAccessor.getCategoryName() + "> field: <" + key
				+ "> is not known datedData datatype value: <" + value + ">");
	}
}
