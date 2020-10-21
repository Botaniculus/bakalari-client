package cz.botaniculus.bakalari;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.*;


public class Bakal {
    final private String baseURL = "https://www.gvp.cz/info";
    private URL targetURL;
    private String data;
    private String got;
    private String output;
    private String accessToken;
    private String refreshToken;

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
        /*JSONObject obj = new JSONObject(output);
        JSONArray Days = obj.getJSONArray("Days");
        JSONArray Atoms = Days.getJSONArray(0);
        JSONObject objAtoms = Atoms.getJSONObject(0);
        String[] id = new String[objAtoms.length()];
        String[] theme = new String[objAtoms.length()];
        for(int i=0; i< Atoms.length(); i++){
            id[i]=objAtoms.getString("HourId");
            theme[i]=objAtoms.getString("Theme");

        }
        for(int i = 0; i< Atoms.length(); i++){
            System.out.println(id[i]+" " + theme[i]);
        }*/
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
