import Helpers.FileHelper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Arriz {
    private static final String APPLICATION_NAME = "Arriz - Google Sheets API";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private static final String PROPERTIES_FILE_PATH = "arriz.properties";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    static Sheets googleSheetService;
    static {
        googleSheetService = buildGoogleSheetsService();
    }

    private static Properties props = new Properties();

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = Arriz.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    private static Sheets buildGoogleSheetsService() {
        // Builds a new authorized API client service.
        Sheets service = null;
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return service;
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    public static void main(String... args) {

        System.out.println("####### Application: Arriz Googlesheet Poller");
        System.out.println("####### Version: 1.0.0");
        System.out.println("####### Author: Andrew Harrison");
        System.out.println("####### Contact: andyrh3@gmail.com");
        System.out.println("####### Updated: 16 August 2018");
        System.out.println();
        System.out.println();

        try {
            //load a properties file from class path, inside static method
            props.load(Arriz.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_PATH));

        }catch (IOException ioe){
            ioe.printStackTrace();
        }

        final String spreadsheetId = props.getProperty("sheetId");
        final String csvFilePath = props.getProperty("csvFilePath");
        final int pollSecs = Integer.parseInt(props.getProperty("pollSeconds"));

        //final String range = "Sheet1!A1:E";
        final String range = "A1:C";
        //Quartz Scheduler START
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            // and start it off
            scheduler.start();

            //Create the job
            JobDetail job = newJob(ArrizJob.class)
                    .withIdentity("ArrisJob", "Arriz")
                    .usingJobData("spreadsheetId", spreadsheetId)
                    .usingJobData("csvFilePath", FileHelper.rootPath + csvFilePath)
                    .usingJobData("range", range)
                    .build();

            // Trigger the job to run now, and then every 2 mins
            Trigger trigger = newTrigger()
                    .withIdentity("ArrisJob", "Arriz")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(pollSecs)
                            .repeatForever())
                    .build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);

            //shut it down
            //scheduler.shutdown();
        } catch (SchedulerException se) {
            se.printStackTrace();
        }

    }
}