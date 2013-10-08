package org.mbs3.java.exchange;
import microsoft.exchange.webservices.data.*;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ExchangeDailyAgenda extends Options {

	// go away viral ocean
    static {
	      System.setProperty("org.apache.commons.logging.Log",
	                         "org.apache.commons.logging.impl.NoOpLog");
	   }

	
	private static final long serialVersionUID = -7151126027903768973L;
	private CommandLine line;

	public static void main(String[] args) throws Exception {
		new ExchangeDailyAgenda().init(args).go();
	}

	public ExchangeDailyAgenda init(String args[]) throws Exception {
		Options options = this;

		options.addOption("v", "verbose", false, "be extra verbose");
		options.addOption("h", "help", false, "print help information");

		Option oUser = new Option("u", "user", true, "email address"); 
		oUser.setRequired(true);
		options.addOption(oUser);

		Option oPass = new Option("p", "pass", true, "password");
		oPass.setRequired(true);
		options.addOption(oPass);

		Option oURL = new Option("e", "url", true, "EWS url"); 
		oURL.setRequired(false);
		options.addOption(oURL);

		CommandLineParser parser = new GnuParser();
		line = parser.parse(this, args);

		if(isVerbose()) {
			System.out.println("Output will be verbose as requested");
			System.out.println("Required program options: " + getRequiredOptions());
		}

		// dump all of our options in verbose mode
		if(isVerbose() && getOptions().size() > 0) {
			System.out.println("The following options were set:");	
			for(Option opt : getLine().getOptions()) {
				System.out.println(opt + " = " + (opt.hasArg() ? Arrays.asList(opt.getValues()) : true));
			}
		}

		if(getLine().hasOption("help")) { 
			throw new ParseException("Help requested");
		}

		return this;
	}

	public void go() throws Exception {
		ExchangeService service = new ExchangeService();

		String user = getUser();

		ExchangeCredentials credentials = new WebCredentials(user,getPass());
		service.setCredentials(credentials);
		
		service.setUrl(new URI(getUrl()));

		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, start.getActualMinimum(Calendar.HOUR_OF_DAY));
		start.set(Calendar.MINUTE, start.getActualMinimum(Calendar.MINUTE));

		Calendar end = Calendar.getInstance();
		end.set(Calendar.HOUR_OF_DAY, end.getActualMaximum(Calendar.HOUR_OF_DAY));
		end.set(Calendar.MINUTE, end.getActualMaximum(Calendar.MINUTE));

		CalendarFolder calendar = CalendarFolder.bind(service, WellKnownFolderName.Calendar);
		FindItemsResults<Appointment> items = calendar.findAppointments(new CalendarView(start.getTime(), end.getTime()));

		SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMMMM d yyyy");
		String agendaStr = "Daily agenda for " + dayFormat.format(start.getTime());
		
		System.out.println("Subject: " + agendaStr);
		System.out.println(agendaStr);
		System.out.println();
		
		SimpleDateFormat appointmentFormat = new SimpleDateFormat("hh:mm a");

		for(Appointment appt: items.getItems()) {
			displayEvent(appt, appointmentFormat);
		}
	}
	
	public void displayEvent(Appointment appt, SimpleDateFormat appointmentFormat) {
		try {
			Date start = appt.getStart();
			Calendar ph = Calendar.getInstance();
			TimeZone tz = TimeZone.getDefault();
			
			ph.setTimeInMillis(start.getTime() + tz.getOffset(start.getTime()));
			System.out.println(appointmentFormat.format(ph.getTime()) + ": " + appt.getSubject());
			
			if(appt.getLocation() != null) {
				if(!appt.getLocation().trim().startsWith("@"))
					System.out.print("@ ");
				System.out.println(appt.getLocation());
			}
		} 
		catch (Exception ex) {
			System.out.println("Could not display the rest of this event, " + ex.getLocalizedMessage());
		}
		System.out.println();
	}

	public boolean isVerbose() {
		boolean verbose = getLine().hasOption("verbose");
		return verbose;
	}

	public String getUser() {
		String user = getLine().getOptionValue("user", "unknown");
		return user;
	}

	public String getPass() {
		String pass = getLine().getOptionValue("pass", "unknown");
		return pass;
	}

	public boolean isDiscover() {
		boolean discover = getLine().hasOption("discover");
		return discover;
	}

	public String getUrl() {
		String url = getLine().getOptionValue("url", "unknown");
		return url;
	}

	public CommandLine getLine() {
		return line;
	}




}
