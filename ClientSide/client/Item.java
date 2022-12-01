/*
 * EE422C Final Project submission by
 * Jason Lin
 * jjl3777
 * 17625
 * Slip day used: 1
 * Fall 2022
 */

package client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

public class Item {
	private SimpleStringProperty name;
	private SimpleStringProperty desc;
	private Double bid;
	private Double buyNow;
	private SimpleStringProperty bidder;
	private LocalDateTime time;
	private Image img;
	private boolean notSold;
	private boolean inTime;
	
	public ArrayList<String> history;

	public boolean isOpen() {
		return notSold && inTime;
	}

	public String getStatus() {
		if (isOpen())
			return "Open";
		return "Closed";
	}
	
	public boolean isSold() {
		return !notSold;
	}

	public Item(String n, String d, Double b, Double bn, String br, LocalDateTime t) {
		name = new SimpleStringProperty(n);
		desc = new SimpleStringProperty(d);
		bid = b;
		buyNow = bn;
		bidder = new SimpleStringProperty(br);
		time = t;
		img = null;
		history = new ArrayList<>();
		update();
	}

	public void update() {
		notSold = bid < buyNow;
		inTime = LocalDateTime.now().isBefore(time);
	}
	
	public boolean sold() {
		return bid >= buyNow;
	}

	public String getName() {
		return name.get();
	}

	public void setName(SimpleStringProperty name) {
		this.name = name;
	}

	public String getDesc() {
		return desc.get();
	}

	public void setDesc(SimpleStringProperty desc) {
		this.desc = desc;
	}

	public String getBid() {
		return String.format("%,.2f", bid);
	}

	public Double getBidD() {
		return bid;
	}

	public void setBid(Double bid) {
		this.bid = bid;
	}

	public String getBuyNow() {
		return String.format("%,.2f", buyNow);
	}

	public void setBuyNow(Double buyNow) {
		this.buyNow = buyNow;
	}

	public String getBidder() {
		return bidder.get();
	}

	public void setBidder(String bidder) {
		this.bidder = new SimpleStringProperty(bidder);
	}

	public String getTime() {
		return time.format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"));
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public Image getImg() {
		return img;
	}

	public void setImg(Image img) {
		this.img = img;
	}
}
