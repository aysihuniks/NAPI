package nesoi.network.api.utils;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.json.JSONObject;
import org.nandayo.DAPI.Util;


public class VersionChecker {
    public static String getVersion() {
        try {
            HttpRequest request = HttpRequest
                    .get("https://api.github.com/repos/nesoi/NConfig/releases/latest")
                    .header("Accept", "application/vnd.github.v3+json");
            HttpResponse response = request.send();

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.bodyText());
                return json.getString("tag_name");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
