package cz.botaniculus.bakalari;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

import static jdk.nashorn.internal.objects.NativeString.trim;


public class Bakal {
    final private String baseURL = "https://www.gvp.cz/info";
    private URL targetURL;
    private String data;
    private String got;
    private String output;
    private String accessToken;
    private String refreshToken;

    private int[] baseSubjectId;
    private String[] baseSubjectAbbrev;
    private String[] dayOfWeek;


    public void login(String username, String password, boolean refresh) throws IOException {
        if(!refresh) {
            data = "client_id=ANDR&grant_type=password&username=" + username + "&password=" + password;
        }
        else if(refresh){
            data = "client_id=ANDR&grant_type=refresh_token&refresh_token="+refreshToken;
        }
        targetURL=new URL(baseURL+"/api/login");
        got=this.bakalari(targetURL, "POST", data, null);
        JSONObject obj = new JSONObject(got);
        accessToken = obj.getString("access_token");
        refreshToken = obj.getString("refresh_token");
    }

    public String userInfo() throws IOException {
        targetURL=new URL(baseURL+"/api/3/user");
        got=this.bakalari(targetURL, "GET", null, accessToken);

        JSONObject obj = new JSONObject(got);
        String FullName = obj.getString("FullName");
        return FullName;
    }
    public String timetable(int day, int month, int year) throws IOException {
        targetURL=new URL(baseURL+"/api/3/timetable/actual?date="+year+"-"+month+"-"+day);
        got=this.bakalari(targetURL, "GET", null, accessToken);
        dayOfWeek= new String[]{"Pondělí", "Úterý", "Středa", "Čtvrtek", "Pátek"};
        JSONObject obj = new JSONObject(got);
        //JSONArray hours = obj.getJSONArray("Hours");
        JSONArray days = obj.getJSONArray("Days");
        JSONArray subjects = obj.getJSONArray("Subjects");
        baseSubjectAbbrev=new String[subjects.length()];
        baseSubjectId=new int[subjects.length()];
        for(int h=0; h<subjects.length(); h++){
            JSONObject sub = subjects.getJSONObject(h);
            baseSubjectAbbrev[h]=sub.getString("Abbrev");
            baseSubjectId[h] = Integer.parseInt(trim(sub.get("Id").toString()));
        }

        for(int i=0; i<(days.length()); i++){
            JSONObject den = days.getJSONObject(i);
            JSONArray atoms = den.getJSONArray("Atoms");

            System.out.print(dayOfWeek[(den.getInt("DayOfWeek")-1)]);
            System.out.print(" "+ den.getString("Date")+"\n");
            for(int f=0; f<atoms.length(); f++){
                JSONObject hodina = atoms.getJSONObject(f);
                int hourId = hodina.getInt("HourId");
                String subjectString = trim(hodina.get("SubjectId")).toString();
                int subjectId=0;
                if(subjectString!="null"){
                    subjectId = Integer.parseInt(subjectString);
                }
                int indexOfSubject=0;
                for(int j=0; j<baseSubjectId.length; j++){
                    if(subjectId==baseSubjectId[j]){
                        indexOfSubject=j;
                    }
                }
                String subjectAbbrev=baseSubjectAbbrev[indexOfSubject];

                JSONObject changeIs = null;
                String description="";
                String change = hodina.get("Change").toString();
                if (change != "null") {
                    changeIs = hodina.getJSONObject("Change");
                    description = changeIs.get("Description").toString();
                }
                String theme = hodina.get("Theme").toString();
                System.out.println((hourId-2) + ": " + subjectAbbrev + " | " + theme);
                if (description!="")
                    System.out.println(" (" + description + ")");

            }
            System.out.println();
        }
            return got;
    }
    private String bakalari(URL target, String method, String data, String token) throws IOException {
        output=null;
        HttpURLConnection conn = (HttpURLConnection)target.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "UTF-8");
        if(token!=null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setRequestMethod(method);
        conn.setUseCaches(false);
        if(data!=null){
            conn.setRequestProperty("Content-Length", "" + Integer.toString(data.length()));
            try(OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream())) {
                out.write(data);
            }
        }
        try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
            String currentLine;
            while((currentLine = in.readLine()) != null){
                output+=currentLine;
            }
        }
        output=output.substring(4);
        return output;
    }
}
