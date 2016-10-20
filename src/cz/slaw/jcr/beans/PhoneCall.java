package cz.slaw.jcr.beans;

public class PhoneCall {

	private boolean incoming;
	private String number;
	
	public PhoneCall(String number,boolean incoming) {
		this.number=number;
		this.incoming=incoming;
	}
	
	public boolean isIncoming() {
		return incoming;
	}
	public void setIncoming(boolean incoming) {
		this.incoming = incoming;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	
	
}
