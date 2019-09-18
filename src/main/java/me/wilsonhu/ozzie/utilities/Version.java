package me.wilsonhu.ozzie.utilities;

public class Version {
	private int major;
	private int minor;
	private int revision;
	private String version;
	
	public Version(String version) {
		this.setVersion(version);
		String[] number = version.split("\\.");
		this.setMajor(Integer.parseInt(number[0]));
		this.setMinor(Integer.parseInt(number[1]));
		this.setRevision(Integer.parseInt(number[2]));
	}
	
	public int getMajor() {
		return major;
	}
	public void setMajor(int major) {
		this.major = major;
	}
	public int getMinor() {
		return minor;
	}
	public void setMinor(int minor) {
		this.minor = minor;
	}
	public int getRevision() {
		return revision;
	}
	public void setRevision(int revision) {
		this.revision = revision;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	
}
