package gov.pnnl.emsl.my;

import java.lang.String;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;

public class MyEMSLConfig {
	IniPreferences prefs;

	public MyEMSLConfig() throws IOException {
		Ini config = new Ini(new File("/etc/myemsl/general.ini"));
		prefs = new IniPreferences(config);
	}

	public MyEMSLConfig(String filename) throws IOException {
		Ini config = new Ini(new File(filename));
		prefs = new IniPreferences(config);
	}

	public String services() {
		String services_path = prefs.node("client").get("services", "myemsl/services");
		return this.baseurl() + "/" + services_path;
	}

	public String baseurl() {
		String proto = prefs.node("client").get("proto", "https");
		return proto + "://" + this.server();
	}

	public String finishurl() {
		String finish_path = prefs.node("client").get("finish", "myemsl/cgi-bin/finish");
		return "/"+finish_path;
	}

	public String preallocurl() {
		String prealloc_path = prefs.node("client").get("prealloc", "myemsl/cgi-bin/preallocate");
		return this.baseurl() + "/" + prealloc_path;
	}

	public String logouturl() {
		String baseurl = this.baseurl();
		String logout_path = prefs.node("client").get("logout", "myemsl/logout");
		return baseurl + "/" + logout_path;
	}

	public String loginurl() {
		String baseurl = this.baseurl();
		String login_path = prefs.node("client").get("login", "myemsl/auth");
		return baseurl + "/" + login_path;
	}

	public String itemurl() {
		return this.baseurl() + "/myemsl/item/foo/bar";
	}

	public String itemauthurl() {
		return this.baseurl() + "/myemsl/itemauth";
	}

	public String queryurl() {
		String proto = prefs.node("client").get("proto", "https");
		String qserver = prefs.node("client").get("query_server", "a3.my.emsl.pnl.gov");
		return proto + "://"+qserver+"/myemsl/query";
	}

	public String server() {
		return prefs.node("client").get("server", "my.emsl.pnl.gov");
	}
}
