/*
 * © 2009 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.service.dto;

import java.util.Date;

import com.sciforma.psnext.api.BooleanDatedData;
import com.sciforma.psnext.api.DateDatedData;
import com.sciforma.psnext.api.DoubleDatedData;
import com.sciforma.psnext.api.IntDatedData;
import com.sciforma.psnext.api.LongDatedData;
import com.sciforma.psnext.api.StringDatedData;

import fr.sciforma.psconnect.service.dto.DatedDataDTO;

/**
 * class helper to assemble DatedData
 */
public class DatedDataDTOAssembler {

	private DatedDataDTOAssembler() {
	}

	public static DatedDataDTO<Double> assembleDoubleDatedData(
			DoubleDatedData doubleDatedData) {
		return new DatedDataDTO<Double>(doubleDatedData.getStart(),
				doubleDatedData.getFinish(), doubleDatedData.getData());
	}

	public static DatedDataDTO<String> assembleStringDatedData(
			StringDatedData stringDatedData) {
		return new DatedDataDTO<String>(stringDatedData.getStart(),
				stringDatedData.getFinish(), stringDatedData.getData());
	}

	public static DatedDataDTO<Date> assembleDateDatedData(
			DateDatedData dateDatedData) {
		return new DatedDataDTO<Date>(dateDatedData.getStart(), dateDatedData
				.getFinish(), dateDatedData.getData());
	}

	public static DatedDataDTO<Boolean> assembleBooleanDatedData(
			BooleanDatedData booleanDatedData) {
		return new DatedDataDTO<Boolean>(booleanDatedData.getStart(),
				booleanDatedData.getFinish(), booleanDatedData.getData());
	}

	public static DatedDataDTO<Integer> assembleIntegerDatedData(
			IntDatedData integerDatedData) {
		return new DatedDataDTO<Integer>(integerDatedData.getStart(),
				integerDatedData.getFinish(), integerDatedData.getData());
	}

	public static DatedDataDTO<Long> assembleLongDatedData(
			LongDatedData integerDatedData) {
		return new DatedDataDTO<Long>(integerDatedData.getStart(),
				integerDatedData.getFinish(), integerDatedData.getData());
	}

}
