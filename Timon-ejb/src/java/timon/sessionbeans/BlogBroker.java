/*
 *   __    __ _ _     _   ___           _   _     _             __  __
 *  / / /\ \ (_) | __(_) / _ \__ _ _ __| |_(_) __| | ___   /\/\ \ \/ /
 *  \ \/  \/ / | |/ /| |/ /_)/ _` | '__| __| |/ _` |/ _ \ /    \ \  / 
 *   \  /\  /| |   < | / ___/ (_| | |  | |_| | (_| | (_) / /\/\ \/  \ 
 *    \/  \/ |_|_|\_\|_\/    \__,_|_|   \__|_|\__,_|\___/\/    \/_/\_\
 *                                              
 *                                              
 *  
 * Wikipartido de Mexico
 * VER ARCHIVO DE LiCENCIA
 * 
 * 
 */
package timon.sessionbeans;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.wordpess.Post;
import org.wordpess.RecentPosts;

/**
 *
 * @author alfonso
 */
@Stateless
public class BlogBroker implements Serializable {

    private static final Logger mrlog = Logger.getLogger(BlogBroker.class.getName());

    public List<Post> getRecentPosts() {
        String url = "http://blog.wikipartido.mx/?json=get_recent_posts";
        String json = httpRequester(url);
        Gson gson = new Gson();
        RecentPosts recentPosts = gson.fromJson(json, RecentPosts.class);
        return recentPosts.posts;
    }

    public String httpRequester(String url) {

        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        InputStream instream = null;
        StringWriter w = new StringWriter();
        try {
            HttpResponse response = client.execute(httpget);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.out.println("Hubo un error");
                return null;
            }

            HttpEntity entity = response.getEntity();
            instream = entity.getContent();
            IOUtils.copy(instream, w);
            //Reader reader = new InputStreamReader(entity.getContent());
        } catch (IOException ex) {
            mrlog.log(Level.SEVERE, null, ex);
        } finally {
            try {
                instream.close();
            } catch (IOException ex) {
                mrlog.log(Level.SEVERE, null, ex);
            }
        }
        return w.toString();
    }
}