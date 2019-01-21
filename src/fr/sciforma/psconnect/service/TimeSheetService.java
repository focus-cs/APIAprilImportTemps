/*
 * © 2008 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.service;

import java.util.Date;
import java.util.Map;

import fr.sciforma.psconnect.exception.BusinessException;
import fr.sciforma.psconnect.exception.TechnicalException;

/**
 * TimeSheet service
 * 
 */
public interface TimeSheetService {

	enum DATA {
		UPDATE, ADD, UPDATE_CLEAN, ADD_CLEAN
	}

	enum MODE {
		AUTO_AFFECTATION
	}

	void bulkActualEffortTimesheet(
			String resourceId,
			Map<String, Map<String, Map<Date, Double>>> projectCodeTaskCodeDateActualEffortMap)
			throws BusinessException, TechnicalException;

	Map<String, Map<String, Map<Date, Double>>> readPlannedEffortTimesheet(
			String resourceId, Date start, Date finish)
			throws BusinessException, TechnicalException;

	Map<String, Map<String, Map<Date, Double>>> readActualEffortTimesheet(
			String resourceId, Date start, Date finish)
			throws BusinessException, TechnicalException;

}
