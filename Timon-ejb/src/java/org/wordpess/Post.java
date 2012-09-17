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
public class Post {
    public long id;
    public String type;
    public String slug;
    public String url;
    public String status;
    public String title;
    @SerializedName("title_plain")
    public String titlePlain;
    public String content;
    public String excerpt;
    public String date;
    public String modified;
    public List<Category> categories;
    public List<Comment> comments;
    public List<Attachment> atttachments;
    @SerializedName("comment_count")
    public long commentCount;
    @SerializedName("comment_status")
    public String commentStatus;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * @param slug the slug to set
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the titlePlain
     */
    public String getTitlePlain() {
        return titlePlain;
    }

    /**
     * @param titlePlain the titlePlain to set
     */
    public void setTitlePlain(String titlePlain) {
        this.titlePlain = titlePlain;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the excerpt
     */
    public String getExcerpt() {
        return excerpt;
    }

    /**
     * @param excerpt the excerpt to set
     */
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the modified
     */
    public String getModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(String modified) {
        this.modified = modified;
    }

    /**
     * @return the categories
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * @param categories the categories to set
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    /**
     * @return the comments
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    /**
     * @return the atttachments
     */
    public List<Attachment> getAtttachments() {
        return atttachments;
    }

    /**
     * @param atttachments the atttachments to set
     */
    public void setAtttachments(List<Attachment> atttachments) {
        this.atttachments = atttachments;
    }

    /**
     * @return the commentCount
     */
    public long getCommentCount() {
        return commentCount;
    }

    /**
     * @param commentCount the commentCount to set
     */
    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    /**
     * @return the commentStatus
     */
    public String getCommentStatus() {
        return commentStatus;
    }

    /**
     * @param commentStatus the commentStatus to set
     */
    public void setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
    }
}

