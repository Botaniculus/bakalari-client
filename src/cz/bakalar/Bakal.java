package cz.bakalar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Bakal {
    private String[] tokens;
    private String baseURL = "https://www.gvp.cz/info";
    private URL targetURL;
    private String data;

    private int accessToken=0;
    private int refreshToken=3;
////////////////////////////////////////////////////////////////////////////////////
    public String[] login(String username, String password) throws IOException {
        data = "client_id=ANDR&grant_type=password&username=" + username + "&password=" + password;
        targetURL=new URL(baseURL+"/api/login");

        String output = "";
        HttpURLConnection conn = (HttpURLConnection)targetURL.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Content-Length", "" + Integer.toString(data.length()));
        conn.setUseCaches(false);
        try(OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream())) {
            out.write(data);
        }
        try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String currentLine;
            while((currentLine = in.readLine()) != null) {
                output+=currentLine;
            }
        }
        output = output.replace("null", "");
        output = output.replace("{", "");
        output = output.replace("}", "");
        String[] outputAr=output.split(",");
        tokens =new String[outputAr.length];
        for (int i = 0; i < outputAr.length; i++){
            outputAr[i]=outputAr[i].replace("\"", "");
            String[] slup = outputAr[i].split(":");
            tokens[i]=slup[1];
        }

        return tokens;
    }
/////////////////////////////////////////////////////////////////////////////////////////

    public String timetable() throws IOException {
        System.out.println(tokens[accessToken]);
        String output = "";
        targetURL=new URL(baseURL+"/api/3/timetable/actual?date=2020-10-20");
        HttpURLConnection conn = (HttpURLConnection)targetURL.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "UTF-8");

        conn.setRequestProperty("Authorization","Bearer "+ tokens[accessToken]);
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);

        try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
            String currentLine;
            while((currentLine = in.readLine()) != null){
                output+=currentLine;
            }
        }
        return output;
    }
}
