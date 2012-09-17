/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wordpess;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 *
 * @author alfonso
 */
public class RecentPosts {    
    public String status;
    public int count;
    @SerializedName("count_total")
    public int countTotal;
    public int pages;
    public List<Post> posts;
}
