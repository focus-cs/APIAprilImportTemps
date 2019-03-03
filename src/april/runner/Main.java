/*
 * © 2012 Sciforma. Tous droits réservés. 
 */
package april.runner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sciforma.psnext.api.DoubleDatedData;
import com.sciforma.psnext.api.Global;
import com.sciforma.psnext.api.PSException;
import com.sciforma.psnext.api.Project;
import com.sciforma.psnext.api.Resource;
import com.sciforma.psnext.api.Session;
import com.sciforma.psnext.api.Task;
import com.sciforma.psnext.api.Timesheet;
import com.sciforma.psnext.api.TimesheetAssignment;
import com.sciforma.psnext.api.User;

import fr.sciforma.psconnect.exception.BusinessException;
import fr.sciforma.psconnect.exception.TechnicalException;
import fr.sciforma.psconnect.input.CSVFileInputImpl;
import fr.sciforma.psconnect.input.LineFileInput;
import fr.sciforma.psconnect.manager.ProjectManager;
import fr.sciforma.psconnect.manager.ProjectManagerImpl;
import fr.sciforma.psconnect.manager.ResourceManager;
import fr.sciforma.psconnect.manager.ResourceManagerImpl;
import fr.sciforma.psconnect.manager.UserManager;
import fr.sciforma.psconnect.manager.UserManagerImpl;
import fr.sciforma.psconnect.service.range.DateRange;
import fr.sciforma.psconnect.service.range.WeekRange;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.pmw.tinylog.Logger;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Main {

    private final static String NUMBER = "1.8";

    private final static String PROGRAM = "importTimeSheet";

    private static ProjectManager publishedProjectManager;

    private static ProjectManager workingProjectManager;

    private static ResourceManager publishedResourceManager;

    private static UserManager userManager;

    /* #ELA-01 
     private static NamedParameterJdbcTemplate jdbcTemplateEasyvista;

     private static NamedParameterJdbcTemplate jdbcTemplateSciforma; 
     */
    private static Map<String, Map<DateRange, Map<String, Map<String, List<EasyVistaData>>>>> recordsByTaskIdByProjectIdByPeriodByResourceId = new HashMap<String, Map<DateRange, Map<String, Map<String, List<EasyVistaData>>>>>();

    private static DateRange fullPeriod;

    private static List<DateRange> periods;

    private static boolean store;

    public static Session mSession;

    //public static ApplicationContext ctx;
    private static String IP;
    private static String CONTEXTE;
    private static String USER;
    private static String PWD;

    private static String DATE_MASK;
    private static int NumDossier;
    private static int CentreDeCoutDossier;
    private static int ModeDeversement;
    private static int Intervenant;
    private static int LoginIntervenant;
    private static int TempsSaisi;
    private static int DateIntervention;

    /**
     * #ELA-01 private static String IP_EV; private static String USER_EV;
     * private static String PWD_EV;
     *
     * private static String IP_SCI; private static String USER_SCI; private
     * static String PWD_SCI;
    *
     */
    private static LineFileInput<String[]> lineFileInput;

    private static Properties properties;

    /**
     * @param args arguments attendus psconnect.properties et log4j.properties
     *
     * #ELA-01 19-02-19 Remplacer les requêtes SQL par la lecture d'un fichier
     * CSV
     */
    public static void main(String[] args) {
        Logger.info("[main][" + PROGRAM + "][V" + NUMBER + "] Demarrage de l'API: " + new Date());
        try {
            initialisation();
            connexion();
            try {
                chargementConfiguration();
            } catch (TechnicalException ex) {
                Logger.error(ex);
            } catch (BusinessException ex) {
                Logger.error(ex);
            }
            process();
            mSession.logout();
            Logger.info("[main][" + PROGRAM + "][V" + NUMBER + "] Fin de l'API: " + new Date());
        } catch (PSException ex) {
            Logger.error(ex);
        }
        System.exit(0);
    }

    private static void initialisation() {
        properties = new Properties();
        FileInputStream in;

        try {
            in = new FileInputStream(System.getProperty("user.dir") + System.getProperty("file.separator") + "conf" + System.getProperty("file.separator") + "psconnect.properties");
            properties.load(in);
            in.close();
        } catch (FileNotFoundException ex) {
            Logger.error("Erreur dans la lecture du fichier properties. ", ex);
            System.exit(-1);
        } catch (IOException ex) {
            Logger.error("Erreur dans la lecture du fichier properties. ", ex);
            System.exit(-1);
        } catch (NullPointerException ex) {
            Logger.error("Erreur dans la lecture du fichier properties. ", ex);
            System.exit(-1);
        }
        //ctx = new FileSystemXmlApplicationContext(System.getProperty("user.dir") + System.getProperty("file.separator") + "conf" + System.getProperty("file.separator") + "applicationContext.xml");
    }

    private static void connexion() {

        try {
            USER = properties.getProperty("sciforma.user");
            PWD = properties.getProperty("sciforma.pwd");
            IP = properties.getProperty("sciforma.ip");
            CONTEXTE = properties.getProperty("sciforma.ctx");

            Logger.info("Initialisation de la Session:" + new Date());
            String url = IP + "/" + CONTEXTE;
            Logger.info("URL: " + url);
            mSession = new Session(url);
            mSession.login(USER, PWD.toCharArray());
            Logger.info("Connecté: " + new Date() + " à l'instance " + CONTEXTE);
        } catch (PSException ex) {
            Logger.error("Erreur dans la connection de ... " + CONTEXTE, ex);
            Logger.error(ex);
            System.exit(-1);
        } catch (NullPointerException ex) {
            Logger.error("Erreur dans la connection de ... " + CONTEXTE, ex);
            System.exit(-1);
        }
    }

    private static void chargementConfiguration() throws TechnicalException, BusinessException {
        Logger.info("chargementConfiguration...");
        try {
            NumDossier = Integer.valueOf(properties.getProperty("file.NumDossier"));
        } catch (NumberFormatException exception) {
            Logger.error("La valeur n'est pas correctement renseignée.",exception);
            throw new BusinessException("La valeur n'est pas correctement renseignée.");
        }
        try {
            CentreDeCoutDossier = Integer.valueOf(properties.getProperty("file.CentreDeCoutDossier"));
        } catch (NumberFormatException exception) {
            Logger.error("La valeur n'est pas correctement renseignée.",exception);
            throw new BusinessException("La valeur n'est pas correctement renseignée.");
        }
        try {
            ModeDeversement = Integer.valueOf(properties.getProperty("file.ModeDeversement"));
        } catch (NumberFormatException exception) {
            Logger.error("La valeur n'est pas correctement renseignée.",exception);
            throw new BusinessException("La valeur n'est pas correctement renseignée.");
        }
        try {
            Intervenant = Integer.valueOf(properties.getProperty("file.Intervenant"));
        } catch (NumberFormatException exception) {
            Logger.error("La valeur n'est pas correctement renseignée.",exception);
            throw new BusinessException("La valeur n'est pas correctement renseignée.");
        }
        try {
            LoginIntervenant = Integer.valueOf(properties.getProperty("file.LoginIntervenant"));
        } catch (NumberFormatException exception) {
            Logger.error("La valeur n'est pas correctement renseignée.",exception);
            throw new BusinessException("La valeur n'est pas correctement renseignée.");
        }
        try {
            TempsSaisi = Integer.valueOf(properties.getProperty("file.TempsSaisi"));
        } catch (NumberFormatException exception) {
            Logger.error("La valeur n'est pas correctement renseignée.",exception);
            throw new BusinessException("La valeur n'est pas correctement renseignée.");
        }
        try {
            DateIntervention = Integer.valueOf(properties.getProperty("file.DateIntervention"));
        } catch (NumberFormatException exception) {
            Logger.error("La valeur n'est pas correctement renseignée.",exception);
            throw new BusinessException("La valeur n'est pas correctement renseignée.");
        }
        
        DATE_MASK = properties.getProperty("file.date.mask");

        /**
         * #ELA-01 try { Class.forName("net.sourceforge.jtds.jdbc.Driver"); }
         * catch (ClassNotFoundException e) { throw new TechnicalException(e,
         * "impossible de trouver le driver JDBC
         * <" + "net.sourceforge.jtds.jdbc.Driver" + "> dans le classpath."); }
         * USER_EV = properties.getProperty("easyvista.login"); PWD_EV =
         * properties.getProperty("easyvista.password"); IP_EV =
         * properties.getProperty("easyvista.url");
         *
         * SingleConnectionDataSource dataSourceEasyvista = new
         * SingleConnectionDataSource(IP_EV, USER_EV, PWD_EV, false);
         * dataSourceEasyvista.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
         *
         * USER_SCI = properties.getProperty("db.login"); PWD_SCI =
         * properties.getProperty("db.password"); IP_SCI =
         * properties.getProperty("db.url");
         *
         * SingleConnectionDataSource dataSourceSciforma = new
         * SingleConnectionDataSource(IP_SCI, USER_SCI, PWD_SCI, false);
         * dataSourceSciforma.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
         *
         * jdbcTemplateEasyvista = new
         * NamedParameterJdbcTemplate(dataSourceEasyvista);
         * Logger.info("Connexion à la base EASYVISTA");
         *
         * jdbcTemplateSciforma = new
         * NamedParameterJdbcTemplate(dataSourceSciforma);
         * Logger.info("Connexion à la base Sciforma");
        *
         */
        lineFileInput = new CSVFileInputImpl(properties.getProperty("import.file"));

        String moisAFacturer;
        try {
            moisAFacturer = new Global().getStringField("psnext.mois-a-facturer");
        } catch (PSException e) {
            String message = "ER.011 - Erreur lors de la récupération de la donnée : Impossible de récupérer psnext.mois-a-facturer";
            Logger.error(message, e);
            throw new TechnicalException(message);
        }

        if ((moisAFacturer == null) || (moisAFacturer.split("/").length != 2)) {
            String message = "ER.011 - Le format de la période <[Global].psnext.mois-a-facturer> <" + moisAFacturer + "> n'est pas correct.\nFormat attendu MM/AAAA avec MM le mois sur 2 digits et AAAA l'année sur 4 digits";
            Logger.error(message);
            throw new TechnicalException(message);
        }
        String[] moisAnneeSplit = moisAFacturer.split("/");

        int moisRef = 0;
        int anneeRef = 0;

        try {
            moisRef = Integer.parseInt(moisAnneeSplit[0]);
        } catch (NumberFormatException e) {
            String message = "ER.011 - Le format de la période <[Global].psnext.mois-a-facturer> <" + moisAFacturer + "> n'est pas correct.\nFormat attendu MM/AAAA avec MM le mois sur 2 digits et AAAA l'année sur 4 digits";
            Logger.error(message);
            throw new TechnicalException(message);
        }

        try {
            anneeRef = Integer.parseInt(moisAnneeSplit[1]);
        } catch (NumberFormatException e) {
            String message = "ER.011 - Le format de la période <[Global].psnext.mois-a-facturer> <" + moisAFacturer + "> n'est pas correct.\nFormat attendu MM/AAAA avec MM le mois sur 2 digits et AAAA l'année sur 4 digits";
            Logger.error(message);
            throw new TechnicalException(message);
        }

        if (anneeRef <= 2000 || anneeRef > 3000) {
            String message = "ER.011 - Le format de la période <[Global].psnext.mois-a-facturer> <" + moisAFacturer + "> n'est pas correct.\nFormat attendu MM/AAAA avec 2AAA l'année sur 4 digits >2000 ou <3000";
            Logger.error(message);
            throw new TechnicalException(message);
        }

        if ((moisRef <= 0) || (moisRef > 12)) {
            String message = "ER.011 - Le format de la période <[Global].psnext.mois-a-facturer> <" + moisAFacturer + "> n'est pas correct.\nFormat attendu MM/AAAA avec MM le mois sur 2 digits >1 et <13";
            Logger.error(message);
            throw new TechnicalException(message);
        }

        Calendar calendar = Calendar.getInstance();
        /* 
         *#ELA-01
         calendar.set(anneeRef, moisRef - 1, 1, 0, 0, 0);
         */
        calendar.set(anneeRef, moisRef - 1, 1, 0, 0, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        //calendar.add(Calendar.MONTH, -1);

        calendar.set(Calendar.MILLISECOND, 0);
        Date debut = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        Date fin = calendar.getTime();

        fullPeriod = new DateRange(debut, fin);

        periods = new WeekRange(fullPeriod).getDateRanges();

        Logger.info("Période de travail <" + fullPeriod + ">");

        Logger.info("Semaines à modifier <" + Arrays.deepToString(periods.toArray()) + ">");

        publishedProjectManager = new ProjectManagerImpl(mSession).withVersion(Project.VERSION_PUBLISHED).withUnchangedProjectList(true);

        workingProjectManager = new ProjectManagerImpl(mSession).withVersion(Project.VERSION_WORKING).withUnchangedProjectList(true);

        publishedResourceManager = new ResourceManagerImpl(mSession).withUsePublishedResources(true);

        userManager = new UserManagerImpl(mSession);

        // Activation hack to keep status.
        store = false;
    }

    protected static void process() throws TechnicalException {
        /*try {*/
        // suppression des acquitements de plus de XX jour
        /*
         *#ELA-01
         String nbJourPurgeAno = properties.getProperty("nbJourPurgeAno");
         Calendar calendar = Calendar.getInstance();
         calendar.add(Calendar.DATE, -Integer.parseInt(nbJourPurgeAno));
         */

        /*
         Global g = new Global();
         g.lock();
         List pl = mSession.getDataViewRowList("PSN_TPS_PASSE_ACQ", g);
         Iterator pit = pl.iterator();
         while (pit.hasNext()) {
         try {
         DataViewRow dv = (DataViewRow) pit.next();
         if (dv.getDateField("DateTraitement").before(calendar.getTime())) {
         dv.remove();
         }
         } catch (PSException ex) {
         Logger.error(ex);
         }
         }
         g.save(true);
         */
        /**
         * #ELA-01
         *
         * HashMap<String, Serializable> paramMap = new
         * HashMap<String, Serializable>(); paramMap.put("nbJourPurgeAno",
         * nbJourPurgeAno);
         *
         * jdbcTemplateSciforma.update((properties.getProperty("db.statement-purge")),
         * paramMap);
         *
         * Logger.info("table PSN_TPS_PASSE_ACQ : suppression des anomalies
         * datant de plus de <" + nbJourPurgeAno + "> jours. ");
         *
         * // récupération des données Logger.info("parcours de la table
         * E_TEMPS_PASSE de la base de données EASYVISTA...");
         *
         * HashMap<String, Object> parameters = new HashMap<String, Object>();
         *
         * parameters.put("debut", fullPeriod.getDateDebut());
         * parameters.put("fin", fullPeriod.getDateFin());
         */
        for (String[] line : lineFileInput.readAll()) {

            if (!(line.length >= 7)) {
                Logger.warn("la ligne <"
                        + Arrays.deepToString(line)
                        + "> n'a pas suffisament d'élements, attendu <7>, seulement <"
                        + line.length + ">");
                continue;
            }
            /**
             * SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
             * try { EasyVistaData record = new EasyVistaData(line[0], line[1],
             * line[2], line[3], line[4], Integer.parseInt(line[5]),
             * sdf.parse(line[6]));
             */
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_MASK);
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yy");
            try {
                EasyVistaData record = new EasyVistaData(line[NumDossier], line[CentreDeCoutDossier], line[ModeDeversement], line[Intervenant], line[LoginIntervenant], Integer.parseInt(line[TempsSaisi]), sdf2.parse(sdf2.format(sdf.parse(line[DateIntervention]))));

                if (!recordsByTaskIdByProjectIdByPeriodByResourceId.containsKey(record.getLoginIntervenant())) {
                    recordsByTaskIdByProjectIdByPeriodByResourceId.put(record.getLoginIntervenant(), new HashMap<DateRange, Map<String, Map<String, List<EasyVistaData>>>>());
                }

                DateRange period = null;
                for (DateRange dateRange : periods) {
                    if (dateRange.contains(record.getDateIntervention())) {
                        period = dateRange;
                    }
                }
                if (period == null) {
                    throw new TechnicalException(record.getDateIntervention() + " est en dehors de la période de traitement <" + fullPeriod + ">");
                }

                if (!recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).containsKey(period)) {
                    recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).put(period, new HashMap<String, Map<String, List<EasyVistaData>>>());
                }

                if (!recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).containsKey(record.getCentreDeCoutDossier())) {
                    recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).put(record.getCentreDeCoutDossier(), new HashMap<String, List<EasyVistaData>>());
                }

                if (!recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).get(record.getCentreDeCoutDossier()).containsKey(record.getModeDeversement())) {
                    recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).get(record.getCentreDeCoutDossier()).put(record.getModeDeversement(), new LinkedList<EasyVistaData>());
                }
                recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).get(record.getCentreDeCoutDossier()).get(record.getModeDeversement()).add(record);
            } catch (ParseException ex) {
                Logger.error(ex);
            }
        }
        /* #ELA-01
         jdbcTemplateEasyvista.query((properties.getProperty("db.statement-easyvista")), parameters, new RowCallbackHandler() {

         public void processRow(ResultSet result)
         throws SQLException {
         EasyVistaData record = new EasyVistaData(
         result.getString("NumDossier"), // NumDossier
         result.getString("CentreDeCoutDossier"), // CentreDeCoutDossier
         result.getString("ModeDeversement"), // ModeDeversement
         result.getString("Intervenant"), // Intervenant
         result.getString("LoginIntervenant"), // LoginIntervenant
         result.getInt("TempsSaisi"), // TempsSaisi
         result.getDate("DateIntervention") // DateIntervention
         );

         if (!recordsByTaskIdByProjectIdByPeriodByResourceId.containsKey(record.getLoginIntervenant())) {
         recordsByTaskIdByProjectIdByPeriodByResourceId.put(record.getLoginIntervenant(), new HashMap<DateRange, Map<String, Map<String, List<EasyVistaData>>>>());
         }

         DateRange period = null;
         for (DateRange dateRange : periods) {
         if (dateRange.contains(record.getDateIntervention())) {
         period = dateRange;
         }
         }
         if (period == null) {
         throw new TechnicalException(record.getDateIntervention() + " est en dehors de la période de traitement <" + fullPeriod + ">");
         }

         if (!recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).containsKey(period)) {
         recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).put(period, new HashMap<String, Map<String, List<EasyVistaData>>>());
         }

         if (!recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).containsKey(record.getCentreDeCoutDossier())) {
         recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).put(record.getCentreDeCoutDossier(), new HashMap<String, List<EasyVistaData>>());
         }

         if (!recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).get(record.getCentreDeCoutDossier()).containsKey(record.getModeDeversement())) {
         recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).get(record.getCentreDeCoutDossier()).put(record.getModeDeversement(), new LinkedList<EasyVistaData>());
         }
         recordsByTaskIdByProjectIdByPeriodByResourceId.get(record.getLoginIntervenant()).get(period).get(record.getCentreDeCoutDossier()).get(record.getModeDeversement()).add(record);
         }
         });
         */
        // import des temps des feuille de temps
        int total = recordsByTaskIdByProjectIdByPeriodByResourceId.keySet().size();
        int count = 0;
        for (Entry<String, Map<DateRange, Map<String, Map<String, List<EasyVistaData>>>>> recordsByTaskIdByProjectIdByPeriod : recordsByTaskIdByProjectIdByPeriodByResourceId.entrySet()) {

            String loginId = recordsByTaskIdByProjectIdByPeriod.getKey().toLowerCase();
            Logger.info("Traitement de la ressource <" + loginId + "> (" + ++count + "/" + total + ")");

            User user = userManager.findUserById(loginId);

            if (user == null) {
                String message = "ER.009 - l'utilisateur de login de connexion <" + loginId + "> n'existe pas";
                Logger.warn(message);

                rejetResourceRecords(recordsByTaskIdByProjectIdByPeriod.getValue(), message);
                continue;
            }
            try {
                if (!user.isResource()) {
                    String message = "ER.010 - l'utilisateur de login de connexion <" + loginId + "> n'est pas ressource";
                    Logger.warn(message);
                    rejetResourceRecords(recordsByTaskIdByProjectIdByPeriod.getValue(), message);
                    continue;
                }
            } catch (PSException e) {
                throw new TechnicalException(e, "Impossible de savoir si l'utilisateur <" + user + "> est une resource");
            }

            String userId;
            try {
                userId = user.getStringField("ID");
            } catch (PSException e) {
                throw new TechnicalException(e, "Impossible de lire ID sur user");
            }

            Resource resource = publishedResourceManager.findResourceById(userId);

            if (resource == null) {
                String message = "ER.010 - La resource <" + userId + ">  n'existe pas ou n'est pas publiée.";
                Logger.warn(message);
                rejetResourceRecords(recordsByTaskIdByProjectIdByPeriod.getValue(), message);
                continue;
            }
            for (DateRange period : periods) {
                Map<String, Map<String, List<EasyVistaData>>> recordsByTaskIdByProjectId = recordsByTaskIdByProjectIdByPeriod.getValue().get(period);

                boolean dataToClean = false;
                boolean dataToAdd = recordsByTaskIdByProjectId != null && recordsByTaskIdByProjectId.size() != 0;
                Timesheet timesheet = getTimesheet(resource, period);
                HashMap<String, Object> sacrifiedTimesheetAssignment = null;

                // timesheet must be modified
                try {
                    for (TimesheetAssignment timesheetAssignment : (List<TimesheetAssignment>) timesheet.getTimesheetAssignmentList()) {

                        if (timesheetAssignment.getBooleanField("estDeDeversement")) {
                            if (timesheetAssignment.getStatus() != Timesheet.STATUS_REWORK && timesheetAssignment.getStatus() != Timesheet.STATUS_WORKING && timesheetAssignment.getStatus() != Timesheet.STATUS_NONE) {
                                timesheetAssignment.rework();
                            }
                            dataToClean = true;
                        }
                    }
                    if (dataToAdd) {
                        if (timesheet.getStatus() != Timesheet.STATUS_REWORK && timesheet.getStatus() != Timesheet.STATUS_WORKING && timesheet.getStatus() != Timesheet.STATUS_NONE) {
                            if (timesheet.getTimesheetAssignmentList().isEmpty()) {
                                Logger.warn("La feuille de temps de la resource <" + loginId + "> pour la période <" + period + "> n'a pas de lignes de feuille de temps, elle est mise de <" + timesheet.getStatusName() + "> à <à reviser> pour être modifiable.");
                                timesheet.rework();
                            } else {
                                // XXX: impossible de conserver l'état d'une
                                // ligne de feuille de temps pour rendre
                                // modifiable la feuille de temps, la premier
                                // ligne estRecurrent est sacrifiée sinon la
                                // premiere ligne
                                for (TimesheetAssignment timesheetAssignment : (List<TimesheetAssignment>) timesheet.getTimesheetAssignmentList()) {
                                    if (timesheetAssignment.getStatus() != Timesheet.STATUS_REWORK
                                            && timesheet.getStatus() != Timesheet.STATUS_WORKING
                                            && timesheetAssignment.getStatus() != Timesheet.STATUS_NONE
                                            && timesheetAssignment.getBooleanField("estRecurrent")) {
                                        Logger.warn("La feuille de temps de la resource <" + loginId + "> pour la période <" + period + "> n'a pas de lignes de feuille de déversement, la ligne pour le projet <" + timesheetAssignment.getStringField("Project Id") + "> et l'activité <" + timesheetAssignment.getStringField("Id") + "> a due être mise de <" + timesheetAssignment.getStatusName() + "> à <à reviser> pour qu'elle soit modifiable.");

                                        sacrifiedTimesheetAssignment = new HashMap<String, Object>();
                                        sacrifiedTimesheetAssignment.put("projectId", timesheetAssignment.getDoubleField("Project IID"));
                                        sacrifiedTimesheetAssignment.put("resourceId", timesheetAssignment.getDoubleField("Resource IID"));
                                        sacrifiedTimesheetAssignment.put("taskId", timesheetAssignment.getDoubleField("Task IID"));

                                        store(sacrifiedTimesheetAssignment);

                                        timesheetAssignment.rework();

                                        // premier ligne trouvée
                                        break;
                                    }
                                }
                                if (sacrifiedTimesheetAssignment == null) {
                                    for (TimesheetAssignment timesheetAssignment : (List<TimesheetAssignment>) timesheet
                                            .getTimesheetAssignmentList()) {
                                        if (timesheetAssignment.getStatus() != Timesheet.STATUS_REWORK
                                                && timesheet.getStatus() != Timesheet.STATUS_WORKING
                                                && timesheetAssignment
                                                .getStatus() != Timesheet.STATUS_NONE) {
                                            Logger.warn("La feuille de temps de la resource <" + loginId + "> pour la période <" + period + "> n'a pas de lignes de feuille de déversement, la ligne pour le projet <" + timesheetAssignment.getStringField("Project Id") + "> et l'activité <" + timesheetAssignment.getStringField("Id") + "> a due être mise de <" + timesheetAssignment.getStatusName() + "> à <à reviser> pour qu'elle soit modifiable.");

                                            sacrifiedTimesheetAssignment = new HashMap<String, Object>();
                                            sacrifiedTimesheetAssignment.put("projectId", timesheetAssignment.getDoubleField("Project IID"));
                                            sacrifiedTimesheetAssignment.put("resourceId", timesheetAssignment.getDoubleField("Resource IID"));
                                            sacrifiedTimesheetAssignment.put("taskId", timesheetAssignment.getDoubleField("Task IID"));

                                            store(sacrifiedTimesheetAssignment);

                                            timesheetAssignment.rework();

                                            // premier ligne trouvé
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (PSException e) {
                    throw new TechnicalException(e, "Impossible de mettre la feuille de temps de la resource <" + loginId + "> sur la période <" + period + "> dans un état modifiable");
                }

                if (!dataToAdd && !dataToClean) {
                    // next period
                    continue;
                }

                // reload timesheet
                timesheet = getTimesheet(resource, period);

                Set<TimesheetAssignment> timesheetAssignmentsToClean = new HashSet<TimesheetAssignment>();

                for (TimesheetAssignment timesheetAssignment : (List<TimesheetAssignment>) timesheet.getTimesheetAssignmentList()) {
                    try {
                        if (!timesheetAssignmentsToClean.contains(timesheetAssignment) && timesheetAssignment.getBooleanField("estDeDeversement")) {
                            timesheetAssignmentsToClean.add(timesheetAssignment);
                        }
                    } catch (PSException e) {
                        throw new TechnicalException(e, "Impossible de lire estDeDeversement de la feuille de temps");
                    }
                }

                boolean needTimesheetSave = false;

                if (recordsByTaskIdByProjectId != null) {

                    for (Entry<String, Map<String, List<EasyVistaData>>> recordsByTaskId : recordsByTaskIdByProjectId.entrySet()) {
                        String projectId = recordsByTaskId.getKey();

                        Map<String, List<EasyVistaData>> recordsByTaskIdToAutoAffect = new HashMap<String, List<EasyVistaData>>();

                        for (Entry<String, List<EasyVistaData>> records : recordsByTaskId.getValue().entrySet()) {
                            String taskId = records.getKey();

                            if (records.getValue().isEmpty()) {
                                break;
                            }

                            boolean found = false;

                            for (TimesheetAssignment timesheetAssignment : (List<TimesheetAssignment>) timesheet.getTimesheetAssignmentList()) {
                                String timesheetTaskId;
                                try {
                                    timesheetTaskId = timesheetAssignment.getStringField("ID");
                                } catch (PSException e) {
                                    throw new TechnicalException(e, "Impossible de lire Id de la feuille de temps");
                                }
                                String timesheetProjectId;
                                try {
                                    timesheetProjectId = timesheetAssignment.getStringField("Project ID");
                                } catch (PSException e) {
                                    throw new TechnicalException(e, "Impossible de lire Project Id de la feuille de temps");
                                }
                                if (taskId.equals(timesheetTaskId) && projectId.equals(timesheetProjectId)) {
                                    found = true;

                                    timesheetAssignmentsToClean.remove(timesheetAssignment);

                                    needTimesheetSave = true;
                                    updateTimesheetAssignment(period, records.getValue(), timesheet, timesheetAssignment);
                                }

                            }
                            if (!found) {
                                recordsByTaskIdToAutoAffect.put(taskId, records.getValue());
                            }
                        }

                        if (!recordsByTaskIdToAutoAffect.isEmpty()) {
                            Project project = publishedProjectManager.findProjectById(projectId);
                            if (project == null) {

                                project = workingProjectManager.findProjectById(projectId);
                                String message;
                                if (project == null) {
                                    message = "ER.003 - Le projet <" + projectId + "> n'existe pas ";

                                } else {
                                    message = "ER.003 - Le projet <" + project + "> n'est pas publié ";
                                }
                                Logger.warn(message);

                                rejetProjectRecords(recordsByTaskIdToAutoAffect, message);

                                continue;
                            }
                            try {
                                project.open(true);
                            } catch (PSException e) {
                                throw new TechnicalException("Impossible d'ouvrir le projet <" + project + ">");
                            }
                            try {
                                if (project.getBooleanField("Closed")) {
                                    String message = "ER.004 - Le projet <" + project + "> est fermé.";
                                    Logger.warn(message);

                                    rejetProjectRecords(recordsByTaskIdToAutoAffect, message);
                                    continue;
                                }
                            } catch (PSException e) {
                                throw new TechnicalException("Impossible d'avoir l'information <Closed> sur le projet <" + project + ">");
                            }
                            try {
                                if (!project.getBooleanField("Active")) {
                                    String message = "ER.005 - Le projet <" + project + "> n'est pas actif.";
                                    Logger.warn(message);
                                    rejetProjectRecords(recordsByTaskIdToAutoAffect, message);
                                    continue;
                                }
                            } catch (PSException e) {
                                throw new TechnicalException("Impossible d'avoir l'information <Active> sur le projet <" + project + ">");
                            }

                            try {
                                for (Entry<String, List<EasyVistaData>> records : recordsByTaskIdToAutoAffect.entrySet()) {
                                    String taskId = records.getKey();

                                    if (records.getValue().isEmpty()) {
                                        break;
                                    }

                                    try {
                                        Task task = project.getTask(taskId);

                                        if (task == null) {
                                            String message = "ER.006 - La tâche <" + taskId + "> n'existe pas dans le projet <" + project + ">";
                                            Logger.warn(message);

                                            rejetTaskRecords(records.getValue(), message);

                                            // next task
                                            continue;
                                        }
                                        if (task.getBooleanField("is Parent")) {
                                            String message = "ER.007 - La tâche <" + task + "> est une activité parente <" + project + ">";

                                            Logger.warn(message);

                                            rejetTaskRecords(records.getValue(), message);

                                            // next task
                                            continue;
                                        }
                                        if (task.getBooleanField("Closed")) {
                                            String message = "ER.008 - La tâche <" + taskId + "> dans le projet <" + project + "> est fermé pour les feuilles de temps";

                                            Logger.warn(message);

                                            rejetTaskRecords(records.getValue(), message);

                                            // next task
                                            continue;
                                        }

                                        // to add a timesheetAssignment in any
                                        // case.
                                        TimesheetAssignment addAssignment = timesheet.addAssignment(task);

                                        updateTimesheetAssignment(period, records.getValue(), timesheet, addAssignment);

                                        needTimesheetSave = true;
                                    } catch (PSException e) {
                                        Logger.warn("Erreur à la fermeture du projet <" + project + ">", e);
                                    }
                                }
                            } finally {
                                try {
                                    project.close();
                                } catch (PSException e) {
                                    Logger.warn("Erreur à la fermeture du projet <" + project + ">", e);
                                }
                            }
                        }
                    }
                }

                for (TimesheetAssignment timesheetAssignment : timesheetAssignmentsToClean) {
                    try {
                        timesheetAssignment.clearDatedData("Actual Effort", period.getDateDebut(), period.getDateFin());

                        needTimesheetSave = true;
                    } catch (PSException e) {
                        throw new TechnicalException("impossible de mettre à zéro une ligne de feuille de temps");
                    }
                }

                try {
                    if (needTimesheetSave) {
                        timesheet.save();

                        if (sacrifiedTimesheetAssignment != null) {
                            unstore(sacrifiedTimesheetAssignment);
                        }
                    }
                } catch (PSException e) {
                    String message = "ER.012 - Problème à la sauvegarde de la feuille de temps de la ressource <" + loginId + "> sur la période <" + period + ">";
                    Logger.warn(e);
                    Logger.warn(message);

                    rejetPeriodRecords(recordsByTaskIdByProjectIdByPeriod.getValue().get(period), message);
                }
            }
            Logger.info("Fin de traitement de la ressource <" + loginId + "> (" + count + "/" + total + ")");
        }
        /*} catch (PSException ex) {
         Logger.error(ex);
         }*/
    }

    private static void unstore(HashMap<String, Object> sacrifiedTimesheetAssignment) {
        if (store) {
            //jdbcTemplateSciforma.update("UPDATE "+ schema+ ".[Timesheet] SET BinaryContents = :store WHERE ProjectID = :projectId AND TaskID = :taskId AND ResourceID = :resourceId",sacrifiedTimesheetAssignment);
        }

    }

    private static void store(HashMap<String, Object> sacrifiedTimesheetAssignment) {
        if (store) {
            //sacrifiedTimesheetAssignment.put("store",jdbcTemplateSciforma.queryForObject("SELECT BinaryContents FROM " + schema+ ".[Timesheet] WHERE ProjectID = :projectId AND TaskID = :taskId AND ResourceID = :resourceId",sacrifiedTimesheetAssignment,Object.class));
        }
    }

    private static Timesheet getTimesheet(Resource resource, DateRange period) {
        Timesheet timesheet;
        try {
            timesheet = mSession.getTimesheet(resource, period.getAverageDate());

        } catch (PSException e) {
            throw new TechnicalException(e, "Impossible de lire la feuille de temps de la ressouce <" + resource + ">");
        }
        return timesheet;
    }

    private static void updateTimesheetAssignment(DateRange period, List<EasyVistaData> records, Timesheet timesheet, TimesheetAssignment timesheetAssignment) {
        Map<Date, Double> sumActualsByDate = new LinkedHashMap<Date, Double>();

        for (Date date : period) {
            sumActualsByDate.put(date, 0.0);
        }
        for (EasyVistaData record : records) {
            Double actual = sumActualsByDate.get(record.getDateIntervention());
            if (actual == null) {
                actual = 0.0;
            }
            sumActualsByDate.put(record.getDateIntervention(), actual + record.getTempsSaisi());

            record.setDone(true);
        }

        List<DoubleDatedData> doubleDatedDatas = new LinkedList<DoubleDatedData>();
        for (Entry<Date, Double> actual : sumActualsByDate.entrySet()) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(actual.getKey());

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Date debut = calendar.getTime();

            calendar.add(Calendar.DATE, 1);
            Date fin = calendar.getTime();

            doubleDatedDatas.add(new DoubleDatedData(
                    1.0 * actual.getValue() / 60.0, debut, fin));
        }

        try {
            timesheetAssignment.updateDatedData("Actual Effort", doubleDatedDatas);

        } catch (PSException e) {
            try {
                Logger.error("Impossible de mettre à jour la charge réelle sur <" + timesheetAssignment.getStringField("Id") + " - " + timesheetAssignment.getStatusName() + "> sur le projet: " + timesheetAssignment.getStringField("Project Name") + " pour " + timesheetAssignment.getStringField("Name"));
                Logger.error(e);
                //throw new TechnicalException(e, "Impossible de mettre à jour la charge réelle sur <" + timesheetAssignment.getStringField("Id") + " - " + timesheetAssignment.getStatusName() + ">");
            } catch (PSException e1) {
                throw new TechnicalException(e1, "Impossible de mettre à jour la charge réelle sur <" + timesheetAssignment + ">");
            }
        }
    }

    private static void rejetResourceRecords(
            Map<DateRange, Map<String, Map<String, List<EasyVistaData>>>> recordsByTaskIdByProjectIdByPeriod, String message) {
        List<EasyVistaData> list = new LinkedList<EasyVistaData>();
        for (Map<String, Map<String, List<EasyVistaData>>> map : recordsByTaskIdByProjectIdByPeriod.values()) {
            for (Map<String, List<EasyVistaData>> map2 : map.values()) {
                for (List<EasyVistaData> easyVistaDatas : map2.values()) {
                    list.addAll(easyVistaDatas);
                }
            }
        }
        rejetTaskRecords(list, message);
    }

    private static void rejetPeriodRecords(
            Map<String, Map<String, List<EasyVistaData>>> map, String message) {
        List<EasyVistaData> list = new LinkedList<EasyVistaData>();
        if (map != null) {
            for (Map<String, List<EasyVistaData>> easyVistaDatas : map.values()) {

                for (List<EasyVistaData> easyVistaData : easyVistaDatas.values()) {
                    list.addAll(easyVistaData);
                }
            }
            rejetTaskRecords(list, message);
        }

    }

    private static void rejetProjectRecords(
            Map<String, List<EasyVistaData>> recordsByTaskIdToAutoAffect, String message) {
        List<EasyVistaData> list = new LinkedList<EasyVistaData>();
        for (List<EasyVistaData> easyVistaDatas : recordsByTaskIdToAutoAffect.values()) {
            list.addAll(easyVistaDatas);
        }
        rejetTaskRecords(list, message);
    }

    private static void rejetTaskRecords(List<EasyVistaData> recordsInPeriod, String message) {
        java.sql.Date date = new java.sql.Date(new Date().getTime());
        /*
         try {
            
         Global g = new Global();
         DataViewRow dv;
         g.lock();
         for (EasyVistaData easyVistaData : recordsInPeriod) {
         easyVistaData.setMessage(message);
         easyVistaData.setDateTraitement(date);
         easyVistaData.setDone(false);
                
         dv = new DataViewRow("PSN_TPS_PASSE_ACQ", g, DataViewRow.CREATE);
         dv.setDateField("DateTraitement", date);
         dv.setStringField("CodeProjet", easyVistaData.getCentreDeCoutDossier());
         dv.setStringField("CodeAct", easyVistaData.getModeDeversement());
         dv.setDateField("DateIntervention", easyVistaData.getDateIntervention());
         dv.setStringField("NumDossier", easyVistaData.getNumDossier());
         dv.setIntField("Charge", easyVistaData.getTempsSaisi());
         dv.setStringField("LoginIntervenant", easyVistaData.getLoginIntervenant());
         dv.setStringField("Intervenant", easyVistaData.getIntervenant());
         dv.setStringField("Message", message);
         }
         g.save(true);
            
            
         } catch (PSException ex) {
         Logger.error(ex);
         }
         */

        for (EasyVistaData easyVistaData : recordsInPeriod) {
            easyVistaData.setMessage(message);
            easyVistaData.setDateTraitement(date);
            easyVistaData.setDone(false);
        }
        /**
         * #ELA-01 try{
         * jdbcTemplateSciforma.batchUpdate((properties.getProperty("db.statement-rejet")),
         * SqlParameterSourceUtils.createBatch(recordsInPeriod.toArray()));
         * }catch(InvalidDataAccessApiUsageException e){ Logger.error("Erreur
         * sur le traitement de la requête: " +
         * properties.getProperty("db.statement-rejet"));
         * Logger.error(Arrays.toString(recordsInPeriod.toArray()));
         * Logger.error(e); }
         */
    }
}
