package alti.training.product.accountspayable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

public class PdfRead {

	final static String userName = "xxxxxxxxxx@gmail.com";// change accordingly
	final static String password = "xxxxxxxxxx";// change accordingly

	public static void main(String args[]) throws IOException {
		Connection dbConnection = dbConnect("jdbc:mysql://localhost:3306/accounts_payable", "root", "");
		final String fileName = downloadAttachments();
		parsePdfAndStore(dbConnection, fileName);
	}

	private static Connection dbConnect(String db_connect_string, String db_userid, String db_password) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(db_connect_string, db_userid, db_password);
			// System.out.println("Connected to Database");
			return conn;
		} catch (Exception dbException) {
			System.out.println(dbException);
			System.out.println("Database Connection Failed");
			System.exit(0);
			return null;
		}
	}

	private static String downloadAttachments() {
		String pop3Host = "pop.gmail.com";// change accordingly
		String mailStoreType = "pop3";
		ReceiveEmailWithAttachment download = new ReceiveEmailWithAttachment();
		// call receiveEmail
		return (download.receiveEmail(pop3Host, mailStoreType, userName, password));
	}

	private static void parsePdfAndStore(Connection dbConnection, String fileName) throws IOException {
		ParsePdf parse = new ParsePdf();
		try {
			parse.readPdf(dbConnection, fileName);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	protected static void sendEmail() {
		SendEmailNotification sendMail = new SendEmailNotification();
		sendMail.send(userName, password, userName, "Accounts Payable", "Your Invoice is approved.");
		// sendMail.send("from","password","to", "Sub", "Content")
	}
}