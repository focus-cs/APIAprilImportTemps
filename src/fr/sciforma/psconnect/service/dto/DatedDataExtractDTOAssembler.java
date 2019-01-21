/*
 * © 2009 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.service.dto;

import java.util.Date;
import java.util.List;

import com.sciforma.psnext.api.BooleanDatedData;
import com.sciforma.psnext.api.DateDatedData;
import com.sciforma.psnext.api.DoubleDatedData;
import com.sciforma.psnext.api.IntDatedData;
import com.sciforma.psnext.api.LongDatedData;
import com.sciforma.psnext.api.StringDatedData;

import fr.sciforma.psconnect.service.dto.DatedDataExtractDTO;

/**
 * class helper to assemble List<DatedData>
 */
public class DatedDataExtractDTOAssembler {

	private DatedDataExtractDTOAssembler() {
	}

	public static DatedDataExtractDTO<Double> transferDoubleDatedData(
			List<DoubleDatedData> doubleDatedDatas,
			DatedDataExtractDTO<Double> datedDataExtractDTO) {
		for (DoubleDatedData doubleDatedData : doubleDatedDatas) {
			datedDataExtractDTO.getDatedDatas().add(
					DatedDataDTOAssembler
							.assembleDoubleDatedData(doubleDatedData));
		}
		return datedDataExtractDTO;
	}

	public static DatedDataExtractDTO<Integer> transferIntegerDatedData(
			List<IntDatedData> integerDatedDatas,
			DatedDataExtractDTO<Integer> datedDataExtractDTO) {
		for (IntDatedData integerDatedData : integerDatedDatas) {
			datedDataExtractDTO.getDatedDatas().add(
					DatedDataDTOAssembler
							.assembleIntegerDatedData(integerDatedData));
		}
		return datedDataExtractDTO;
	}

	public static DatedDataExtractDTO<Long> transferLongDatedData(
			List<LongDatedData> longDatedDatas,
			DatedDataExtractDTO<Long> datedDataExtractDTO) {
		for (LongDatedData longDatedData : longDatedDatas) {
			datedDataExtractDTO.getDatedDatas().add(
					DatedDataDTOAssembler.assembleLongDatedData(longDatedData));
		}
		return datedDataExtractDTO;
	}

	public static DatedDataExtractDTO<Date> transferDateDatedData(
			List<DateDatedData> dateDatedDatas,
			DatedDataExtractDTO<Date> datedDataExtractDTO) {
		for (DateDatedData dateDatedData : dateDatedDatas) {
			datedDataExtractDTO.getDatedDatas().add(
					DatedDataDTOAssembler.assembleDateDatedData(dateDatedData));
		}
		return datedDataExtractDTO;
	}

	public static DatedDataExtractDTO<String> transferStringDatedData(
			List<StringDatedData> stringDatedDatas,
			DatedDataExtractDTO<String> datedDataExtractDTO) {
		for (StringDatedData stringDatedData : stringDatedDatas) {
			datedDataExtractDTO.getDatedDatas().add(
					DatedDataDTOAssembler
							.assembleStringDatedData(stringDatedData));
		}
		return datedDataExtractDTO;
	}

	public static DatedDataExtractDTO<Boolean> transferBooleanDatedData(
			List<BooleanDatedData> booleanDatedDatas,
			DatedDataExtractDTO<Boolean> datedDataExtractDTO) {
		for (BooleanDatedData booleanDatedData : booleanDatedDatas) {
			datedDataExtractDTO.getDatedDatas().add(
					DatedDataDTOAssembler
							.assembleBooleanDatedData(booleanDatedData));
		}
		return datedDataExtractDTO;
	}

}
