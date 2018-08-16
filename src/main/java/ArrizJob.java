import Helpers.FileHelper;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.quartz.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ArrizJob implements Job {

    public ArrizJob (){}

    @Override
    public void execute(JobExecutionContext context) {
        //JobKey key = context.getJobDetail().getKey();
        JobDataMap dataMap = context.getMergedJobDataMap();  // Note the difference from the previous example
        ValueRange response = null;
        try {
            response = Arriz.googleSheetService.spreadsheets().values()
                    .get(dataMap.getString("spreadsheetId"), dataMap.getString("range"))
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert response != null;
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            String csvStr = values.stream().map(row -> row.stream().map(col -> col.toString()).collect(Collectors.joining(","))).collect(Collectors.joining("\n"));
            //System.out.println("Data Found!!!!");
            /*for (List row : values) {
                // Print columns A,B and C, which correspond to indices 0 - 2.
                System.out.printf("%s, %s, %s\n", row.get(0), row.get(1), row.get(2));
            }*/
            //Write to local CSV file
            FileHelper.writeStringToFile(csvStr, new File(dataMap.getString("csvFilePath")));
            System.out.println("Success - wrote data to " + dataMap.getString("csvFilePath") + " at " + LocalDateTime.now());
        }
        //System.err.println("Hello!  HelloJob is executing.");
    }
}
