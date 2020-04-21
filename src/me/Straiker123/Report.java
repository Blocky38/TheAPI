package me.Straiker123;

public class Report {
	private String s,p,r,m, id;
	private long l;
	public Report(String id, String sender, String reported, String reason, String message, long time) {
		s=sender;
		this.id=id;
		p=reported;
		r=reason;
		m=message;
		l=time;
	}

	public String getSender() {
		return s;
	}
	public String getReported() {
		return p;
	}
	public String getReason() {
		return r;
	}

	public void setMessage(String newMessage) {
		LoaderClass.data.getConfig().set("report."+s+"."+id+".message",newMessage);
		m=newMessage;
	}
	public void setReason(String newReason) {
		LoaderClass.data.getConfig().set("report."+s+"."+id+".reason",newReason);
		r=newReason;
	}
	
	public String getMessage() {
		return m;
	}
	public long getTime() {
		return l;
	}
	
	public int getID() {
		return TheAPI.getStringUtils().getInt(id);
	}
	
	public boolean isSolved() {
		return LoaderClass.data.getConfig().getBoolean("report."+s+"."+id+".solved");
	}
	
	public void remove() {
		LoaderClass.data.getConfig().set("report."+s+"."+id, null);
	}
	
	public void setSolved(boolean solved) {
		LoaderClass.data.getConfig().set("report."+s+"."+id+".solved", solved);
	}
}
