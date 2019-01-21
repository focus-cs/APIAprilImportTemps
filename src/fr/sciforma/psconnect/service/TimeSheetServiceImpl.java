/*
 * © 2008 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.time.DateUtils;

import com.sciforma.psnext.api.AccessException;
import com.sciforma.psnext.api.DataFormatException;
import com.sciforma.psnext.api.DatedData;
import com.sciforma.psnext.api.DoubleDatedData;
import com.sciforma.psnext.api.InvalidDataException;
import com.sciforma.psnext.api.PSException;
import com.sciforma.psnext.api.Project;
import com.sciforma.psnext.api.Resource;
import com.sciforma.psnext.api.Session;
import com.sciforma.psnext.api.Task;
import com.sciforma.psnext.api.Timesheet;
import com.sciforma.psnext.api.TimesheetAssignment;

import fr.sciforma.psconnect.exception.BusinessException;
import fr.sciforma.psconnect.exception.TechnicalException;
import fr.sciforma.psconnect.manager.ProjectManager;
import fr.sciforma.psconnect.manager.ProjectManagerImpl;
import fr.sciforma.psconnect.manager.ResourceManager;
import fr.sciforma.psconnect.manager.ResourceManagerImpl;
import fr.sciforma.psconnect.manager.SystemDataManager;

/**
 * implementation du timesheet service
 */
public class TimeSheetServiceImpl implements TimeSheetService {

	private ResourceManager publishedResourceManager;

	private ProjectManager publishedProjectManager;

	private Session session;

	/**
	 * @param systemDataManager
	 * @param category
	 * @throws BusinessException
	 * @throws TechnicalException
	 */
	public TimeSheetServiceImpl(Session session,
			ResourceManager resourceManager, ProjectManager projectManager,
			SystemDataManager systemDataManager) throws BusinessException,
			TechnicalException {
		this.publishedResourceManager = resourceManager;
		this.publishedProjectManager = projectManager;
		this.session = session;
	}

	public TimeSheetServiceImpl(Session session) {
		this.session = session;
		this.publishedResourceManager = new ResourceManagerImpl(session)
				.withUsePublishedResources(true);
		this.publishedProjectManager = new ProjectManagerImpl(session)
				.withVersion(Project.VERSION_PUBLISHED);
	}

	public void bulkActualEffortTimesheet(
			String resourceId,
			Map<String, Map<String, Map<Date, Double>>> projectCodeTaskCodeDateActualEffortMap)
			throws BusinessException, TechnicalException {
		Resource resource = this.publishedResourceManager
				.findResourceById(resourceId);

		if (resource == null) {
			throw new BusinessException(null, "Published resource <"
					+ resourceId + "> not exists.");
		}

		try {
			if (!resource.isUser()) {
				throw new BusinessException(null, "Published resource <"
						+ resourceId + "> is not a user.");
			}
		} catch (PSException e) {
			throw new TechnicalException(e,
					"Impossible to check the resource is a user");
		}    

		try {
			if (!"ACTIVE".equals(resource.getStringField("Status"))) {
				throw new BusinessException(null, "Published resource <"
						+ resourceId + "> is not active.");
			}
		} catch (PSException e) {
			throw new TechnicalException(e,
					"Impossible to check the resource is active");
		}

		for (String projectCode : projectCodeTaskCodeDateActualEffortMap
				.keySet()) {
			Project project = publishedProjectManager
					.findProjectById(projectCode);

			if (project == null) {
				throw new BusinessException(null, "Published project <"
						+ projectCode + "> not exists.");
			}

			try {
				if (project.getBooleanField("closed")) {
					throw new BusinessException(null, "Published project <"
							+ projectCode + "> not exists.");
				}
			} catch (PSException e) {
				throw new TechnicalException(e,
						"Impossible to check the project is not closed");
			}
			
			try {
				if (!project.getBooleanField("active")) {
					throw new BusinessException(null, "Published project <"
							+ projectCode + "> not active.");
				}
			} catch (PSException e) {
				throw new TechnicalException(e,
						"Impossible to check the project is active");
			}
		}

		List<Date> dates = new LinkedList<Date>();
		for (Map<String, Map<Date, Double>> map : projectCodeTaskCodeDateActualEffortMap
				.values()) {
			for (Map<Date, Double> map2 : map.values()) {
				dates.addAll(map2.keySet());
			}
		}

		Collections.sort(dates);

		if (dates.isEmpty()) {
			return;
		}

		Date begining = dates.get(0);
		Date endPeriod = dates.get(dates.size() - 1);

		try {
			Timesheet timesheet = this.session.getTimesheet(resource, begining,
					endPeriod);

			// renseigner l'existant.
			for (TimesheetAssignment timesheetAssignment : (List<TimesheetAssignment>) timesheet
					.getTimesheetAssignmentList()) {

				String projectId = timesheetAssignment
						.getStringField("Project Id");
				String taskId = timesheetAssignment.getStringField("ID");

				if (projectCodeTaskCodeDateActualEffortMap
						.containsKey(projectId)
						&& projectCodeTaskCodeDateActualEffortMap
								.get(projectId).containsKey(taskId)) {
					Map<String, Map<Date, Double>> taskCodeDateActualEffortMap = projectCodeTaskCodeDateActualEffortMap
							.get(projectId);

					List<DoubleDatedData> doubleDatedDatas = (List<DoubleDatedData>) timesheetAssignment
							.getDatedData("Actual Effort", DatedData.DAY,
									begining, endPeriod);

					updateActuals(taskCodeDateActualEffortMap,
							timesheetAssignment, projectId, taskId,
							doubleDatedDatas);
				}
			}

			// auto-affectation
			for (Entry<String, Map<String, Map<Date, Double>>> projectCodeTaskCodeDateActualEffortEntry : projectCodeTaskCodeDateActualEffortMap
					.entrySet()) {
				String projectCode = projectCodeTaskCodeDateActualEffortEntry
						.getKey();
				Project project = publishedProjectManager
						.findProjectById(projectCode);

				project.open(true);
				try {
					for (Task task : (List<Task>) project.getTaskOutlineList()) {
						String taskId = task.getStringField("ID");
						if (projectCodeTaskCodeDateActualEffortEntry.getValue()
								.containsKey(taskId)) {
							TimesheetAssignment timesheetAssignment = timesheet
									.addAssignment(task);
							updateActuals(
									projectCodeTaskCodeDateActualEffortEntry
											.getValue(),
									timesheetAssignment, projectCode, taskId,
									new LinkedList<DoubleDatedData>());
						}
					}
					for (Entry<String, Map<Date, Double>> taskCodeDateActualEffortEntry : projectCodeTaskCodeDateActualEffortEntry
							.getValue().entrySet()) {
						System.out.println("task not traitée"
								+ taskCodeDateActualEffortEntry.getKey());
					}
				} finally {
					project.close();
				}
			}

			timesheet.save();
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateActuals(
			Map<String, Map<Date, Double>> taskCodeDateActualEffortMap,
			TimesheetAssignment timesheetAssignment, String projectId,
			String taskId, List<DoubleDatedData> doubleDatedDatas)
			throws DataFormatException, PSException {
		Map<Date, Double> dateDoubleMap = taskCodeDateActualEffortMap
				.get(taskId);

		for (DoubleDatedData doubleDatedData : doubleDatedDatas) {
			if (dateDoubleMap.containsKey(doubleDatedData.getStart())) {
				doubleDatedData.setData(dateDoubleMap.get(doubleDatedData
						.getStart()));

				dateDoubleMap.remove(doubleDatedData.getStart());
			}
		}

		for (Entry<Date, Double> entry : dateDoubleMap.entrySet()) {
			doubleDatedDatas.add(new DoubleDatedData(entry.getValue(), entry
					.getKey(), DateUtils.addDays(entry.getKey(), 1)));
		}

		timesheetAssignment.updateDatedData("Actual Effort", doubleDatedDatas);

		taskCodeDateActualEffortMap.remove(taskId);
	}

	public Map<String, Map<String, Map<Date, Double>>> readActualEffortTimesheet(
			String resourceId, Date start, Date finish)
			throws BusinessException, TechnicalException {
		return readDistFieldTimesheet(resourceId, start, finish,
				"Actual Effort");
	}

	public Map<String, Map<String, Map<Date, Double>>> readPlannedEffortTimesheet(
			String resourceId, Date start, Date finish)
			throws BusinessException, TechnicalException {
		return readDistFieldTimesheet(resourceId, start, finish,
				"Planned Effort");
	}

	private Map<String, Map<String, Map<Date, Double>>> readDistFieldTimesheet(
			String resourceId, Date start, Date finish, String fieldName)
			throws BusinessException {
		Map<String, Map<String, Map<Date, Double>>> projectCodeTaskCodeDateDoubleMap = new HashMap<String, Map<String, Map<Date, Double>>>();

		Resource resource = this.publishedResourceManager
				.findResourceById(resourceId);

		if (resource == null) {
			throw new BusinessException(null, "Published resource <"
					+ resourceId + "> not exists.");
		}

		try {
			Timesheet timesheet = session.getTimesheet(resource, start, finish);

			for (TimesheetAssignment timesheetAssignment : (List<TimesheetAssignment>) timesheet
					.getTimesheetAssignmentList()) {
				String projectId = timesheetAssignment
						.getStringField("Project Id");
				String taskId = timesheetAssignment.getStringField("ID");

				Map<String, Map<Date, Double>> taskCodeDateDoubleMap = projectCodeTaskCodeDateDoubleMap
						.get(projectId);
				if (taskCodeDateDoubleMap == null) {
					taskCodeDateDoubleMap = new HashMap<String, Map<Date, Double>>();
					projectCodeTaskCodeDateDoubleMap.put(projectId,
							taskCodeDateDoubleMap);
				}

				List<DoubleDatedData> doubleDatedDatas = timesheetAssignment
						.getDatedData(fieldName, DatedData.DAY, start, finish);
				HashMap<Date, Double> values = new HashMap<Date, Double>();
				for (DoubleDatedData doubleDatedData : doubleDatedDatas) {
					values.put(doubleDatedData.getStart(),
							doubleDatedData.getData());
				}

				taskCodeDateDoubleMap.put(taskId, values);
			}

		} catch (PSException e) {
			throw new TechnicalException(e);
		}
		return projectCodeTaskCodeDateDoubleMap;
	}
}
