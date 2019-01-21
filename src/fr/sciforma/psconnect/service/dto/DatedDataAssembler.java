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
 * class helper to create DatedData
 */
public class DatedDataAssembler {

	private DatedDataAssembler() {
	}

	public static DoubleDatedData assembleDoubleDatedData(
			DatedDataDTO<Double> datedDataDTO) {
		return new DoubleDatedData(datedDataDTO.getValue(), datedDataDTO
				.getStart(), datedDataDTO.getFinish());
	}

	public static IntDatedData assembleIntDatedData(
			DatedDataDTO<Integer> datedDataDTO) {
		return new IntDatedData(datedDataDTO.getValue(), datedDataDTO
				.getStart(), datedDataDTO.getFinish());
	}

	public static LongDatedData assembleLongDatedData(
			DatedDataDTO<Long> datedDataDTO) {
		return new LongDatedData(datedDataDTO.getValue(), datedDataDTO
				.getStart(), datedDataDTO.getFinish());
	}

	public static BooleanDatedData assembleBooleanDatedData(
			DatedDataDTO<Boolean> datedDataDTO) {
		return new BooleanDatedData(datedDataDTO.getValue(), datedDataDTO
				.getStart(), datedDataDTO.getFinish());
	}

	public static StringDatedData assembleStringDatedData(
			DatedDataDTO<String> datedDataDTO) {
		return new StringDatedData(datedDataDTO.getValue(), datedDataDTO
				.getStart(), datedDataDTO.getFinish());
	}

	public static DateDatedData assembleDateDatedData(
			DatedDataDTO<Date> datedDataDTO) {
		return new DateDatedData(datedDataDTO.getValue(), datedDataDTO
				.getStart(), datedDataDTO.getFinish());
	}
}
