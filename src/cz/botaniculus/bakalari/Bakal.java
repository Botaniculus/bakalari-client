package cz.botaniculus.bakalari;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.*;

import static jdk.nashorn.internal.objects.NativeString.trim;


public class Bakal {
    private String baseURL;
    private URL targetURL;
    private String data;
    private String got;
    private String output;
    private String accessToken;
    private String refreshToken;

    private int[] baseSubjectId;
    private String[] baseSubjectAbbrev;
    private String[] dayOfWeek;

    public Bakal(String baseURL){
        this.baseURL=baseURL;
    }
    public void login(String username, String password, boolean refresh) throws IOException {
        if(!refresh) {
            data = "client_id=ANDR&grant_type=password&username=" + username + "&password=" + password;
        }
        else if(refresh){
            data = "client_id=ANDR&grant_type=refresh_token&refresh_token="+refreshToken;
        }
        targetURL=new URL(baseURL+"/api/login");

        //-----login--------------------------------------------------
        try{
            got=this.request(targetURL, "POST", data, null);
        }
        catch (IOException e){
            System.out.println("Wrong login or no internet!");
            Main.main(null);
        }

        //-----Process JSON output---------------------------
        JSONObject obj = new JSONObject(got);
        accessToken = obj.getString("access_token");
        refreshToken = obj.getString("refresh_token");

    }

    public String userInfo() throws IOException {
        targetURL=new URL(baseURL+"/api/3/user");
        got=this.request(targetURL, "GET", null, accessToken);

        //-----Process JSON output-------------------------
        JSONObject obj = new JSONObject(got);
        String FullName = obj.getString("FullName");

        return FullName;
    }

    public String timetable(int day, int month, int year) throws IOException {
        dayOfWeek= new String[]{"Pondělí", "Úterý", "Středa", "Čtvrtek", "Pátek"};
        targetURL=new URL(baseURL+"/api/3/timetable/actual?date="+year+"-"+month+"-"+day);
        got=this.request(targetURL, "GET", null, accessToken);
        JSONObject obj = new JSONObject(got);
        //JSONArray hours = obj.getJSONArray("Hours");

        //-----Subjects----------------------------------------
        JSONArray subjects = obj.getJSONArray("Subjects");
        baseSubjectAbbrev=new String[subjects.length()];
        baseSubjectId=new int[subjects.length()];
        for(int h=0; h<subjects.length(); h++){
            JSONObject sub = subjects.getJSONObject(h);
            baseSubjectAbbrev[h]=sub.getString("Abbrev");
            baseSubjectId[h] = Integer.parseInt(trim(sub.get("Id").toString()));
        }
        //-------------------

        //-----Days---------------------------------------------------------------------------
        JSONArray days = obj.getJSONArray("Days");
        for(int i=0; i<(days.length()); i++){
            JSONObject den = days.getJSONObject(i);

            //-----Day of week and Date-----
            System.out.print(dayOfWeek[(den.getInt("DayOfWeek")-1)]);
            System.out.print(" "+ den.getString("Date")+"\n");

            //-----Lessons----------------
            JSONArray atoms = den.getJSONArray("Atoms");
            for(int f=0; f<atoms.length(); f++){
                JSONObject lesson = atoms.getJSONObject(f);

                int hourId = lesson.getInt("HourId");

                //-----Get subject and find its abbrevation-----
                String subjectIdString = trim(lesson.get("SubjectId")).toString();
                int subjectId=0;
                if(subjectIdString!="null"){
                    subjectId = Integer.parseInt(subjectIdString);
                }
                int indexOfSubject=0;
                for(int j=0; j<baseSubjectId.length; j++){
                    if(subjectId==baseSubjectId[j]){
                        indexOfSubject=j;
                    }
                }
                String subjectAbbrev=baseSubjectAbbrev[indexOfSubject];
                //-----------------------------------------------

                //-----Get info about changes in timetable-----
                JSONObject changeIs = null;
                String changeDescription="";
                String change = lesson.get("Change").toString();
                if (change != "null") {
                    changeIs = lesson.getJSONObject("Change");
                    changeDescription = changeIs.get("Description").toString();
                }
                //-----------------------------------------------

                //---Get theme of lesson---
                String theme = lesson.get("Theme").toString();

                //---Print result---
                System.out.println((hourId-2) + ": " + subjectAbbrev + " | " + theme);

                //---If there is some change in timetable, print it---
                if (changeDescription!="")
                    System.out.println(" (" + changeDescription + ")");

            }
            System.out.println();
        }

        return got;
    }
    private String request(URL target, String method, String data, String token) throws IOException {
        //clear output
        output=null;

        //-----Http request--------------------------------------------------
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

        //-----Read input stream-------------------------------------
        try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
            String currentLine;
            while((currentLine = in.readLine()) != null){
                output+=currentLine;
            }
        }

        //---remove "null" on the start of output JSON
        output=output.substring(4);

        return output;
    }
}
