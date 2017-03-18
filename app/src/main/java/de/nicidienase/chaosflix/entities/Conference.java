package de.nicidienase.chaosflix.entities;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by felix on 11.03.17.
 */

public class Conference extends SugarRecord{
	String acronym;
	@SerializedName("aspect_ratio")
	String aspectRation;
	String title;
	String slug;
	@SerializedName("webgen_location")
	String webgenLocation;
	@SerializedName("schedule_url")
	String scheduleUrl;
	@SerializedName("logo_url")
	String logoUrl;
	@SerializedName("images_url")
	String imagesUrl;
	@SerializedName("recordings_url")
	String recordingsUrl;
	String url;
	@SerializedName("updated_at")
	String updatedAt;

	List<Event> events;

	public Conference() {
	}

	public Conference(String acronym, String aspectRation, String title, String slug,
					  String webgenLocation, String scheduleUrl, String logoUrl,
					  String imagesUrl, String recordingsUrl, String url,
					  String updatedAt, List<Event> events) {
		this.acronym = acronym;
		this.aspectRation = aspectRation;
		this.title = title;
		this.slug = slug;
		this.webgenLocation = webgenLocation;
		this.scheduleUrl = scheduleUrl;
		this.logoUrl = logoUrl;
		this.imagesUrl = imagesUrl;
		this.recordingsUrl = recordingsUrl;
		this.url = url;
		this.updatedAt = updatedAt;
		this.events = events;
	}

	public HashMap<String, List<Event>> getEventsByTags(){
		HashMap<String, List<Event>> result = new HashMap<>();
		for(Event event: this.getEvents()){
			for(String tag: event.getTags()){
				List<Event> list;
				if(result.keySet().contains(tag)){
					list = result.get(tag);
				} else {
					list = new LinkedList<>();
					result.put(tag,list);
				}
				list.add(event);
			}
		}
		return result;
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public String getAspectRation() {
		return aspectRation;
	}

	public void setAspectRation(String aspectRation) {
		this.aspectRation = aspectRation;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getWebgenLocation() {
		return webgenLocation;
	}

	public void setWebgenLocation(String webgenLocation) {
		this.webgenLocation = webgenLocation;
	}

	public String getScheduleUrl() {
		return scheduleUrl;
	}

	public void setScheduleUrl(String scheduleUrl) {
		this.scheduleUrl = scheduleUrl;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getImagesUrl() {
		return imagesUrl;
	}

	public void setImagesUrl(String imagesUrl) {
		this.imagesUrl = imagesUrl;
	}

	public String getRecordingsUrl() {
		return recordingsUrl;
	}

	public void setRecordingsUrl(String recordingsUrl) {
		this.recordingsUrl = recordingsUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}
}