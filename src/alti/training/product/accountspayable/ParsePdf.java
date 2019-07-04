package alti.training.product.accountspayable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ParsePdf extends PdfRead {
	protected void readPdf(Connection dbConnection, String fileName) throws IOException, SQLException {
		PDDocument document = null;
		Scanner sc = new Scanner(System.in);
		try {
			document = PDDocument.load(new File("D:/" + fileName));
			PDFTextStripper pdfStripper = new PDFTextStripper();

			int pageCounter = 1;
			int invoiceCount = 1;
			for (int j = 0; j < 13; j++) {
				pdfStripper.setStartPage(pageCounter);
				pdfStripper.setEndPage(pageCounter);
				String pages = pdfStripper.getText(document);
				if (pages.contains("Invoice No") && pages.contains("Net Order Total")) {
					String orderNumber = "";
					String invoiceNumber = "";
					String invoiceDate = "";
					String customerPo = "";
					String address = "";
					String total = "";
					String[] lines = pages.split("\r\n|\r|\n");
					invoiceCount++;
					for (int i = 0; i < lines.length; i++) {
						switch (lines[i]) {
						case "Order No":
							orderNumber = lines[++i];
							break;
							
						case "Invoice No":
							invoiceNumber = lines[++i];
							break;

						case "Invoice Date":
							invoiceDate = lines[++i];
							break;

						case "Customer P.O.":
							customerPo = lines[++i].replaceAll("[^0-9]", "");
							break;

						case "Sold To":
							address = lines[++i] + lines[++i] + lines[++i];
							break;

						case "Total Invoice":
							if (invoiceCount == 4)
								i = i + 3;
							else
								i = i + 2;
							total = lines[++i].replaceAll("[,$]", "");
							break;

						case "CREDIT":
							total = lines[++i].replaceAll("[,$]", "");
							break;
						}

					}
				
					 String query = " insert into invoice_details "
					 		+ "(order_number, invoice_number,customer_po, invoice_date, sold_to,"
					 		+ "total_invoice, status)" + " values (?, ?, ?, ?, ?, ?, ?)";

					 PreparedStatement preparedStmt =
					 dbConnection.prepareStatement(query);
					 preparedStmt.setObject(1,orderNumber.trim());
					 preparedStmt.setObject(2,invoiceNumber.trim());
					 preparedStmt.setObject(3,customerPo.trim());
					 preparedStmt.setObject(4,invoiceDate.trim());
					 preparedStmt.setObject(5,address.trim());
					 preparedStmt.setObject(6,total.trim());
					 preparedStmt.setObject(7,"Unapproved");
					 preparedStmt.execute();

				}
				pageCounter++;
			}

			 int choice =0;
			 while(choice<4) {
			 System.out.println("1. Approve 2.Display 3.exit");
			
			 choice = sc.nextInt();
			 switch (choice) {
			 case 1:
			 System.out.println("enter the invoice number to be approved");
			 String number = sc.next();
			 updateStatus(dbConnection, number);
			 sendEmail();
			 break;
			 case 2:
			 display(dbConnection);
			
			 break;
			 case 3:
			 System.out.println("Exit");
			 System.exit(0);
			 break;
			 default:
			 System.out.println("Unapproved");
			 System.exit(0);
			 }
			 }
		} catch (Exception pdfException) {
			System.out.println("Error Occured");
			System.out.println(pdfException);
			System.exit(0);

		} finally {
			if (document != null) {
				document.close();
			}
			sc.close();
			dbConnection.close();
		}
	}

	private static void updateStatus(Connection dbConnection, String invoiceNumber) throws SQLException {
		try {
			String query1 = "select status from invoice_details where invoice_number = ?";
			PreparedStatement preparedSt = dbConnection.prepareStatement(query1);
			preparedSt.setString(1, invoiceNumber);
			ResultSet rs = preparedSt.executeQuery();
			rs.next();

			if (rs.getString("status").equals("Approved")) {
				System.out.println(" This Invoice is already approved");
			} else {
				String query = "update invoice_details set status = ? where invoice_number = ?";
				PreparedStatement preparedStmt = dbConnection.prepareStatement(query);
				preparedStmt.setString(1, "Approved");
				preparedStmt.setString(2, invoiceNumber);
				preparedStmt.executeUpdate();
			}
		} catch (Exception e) {
			System.out.println("Error in approving invoice. Your invoice might be wrong.");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void display(Connection dbConnection) throws SQLException {
		try {

			String query = "Select * from invoice_details";
			PreparedStatement preparedStmt = dbConnection.prepareStatement(query);
			ResultSet resultSet = preparedStmt.executeQuery();
			
			while (resultSet.next()) {
				System.out.println("[Invoice Number:" + resultSet.getString("invoice_number").trim());
				System.out.println("Order Number:" + resultSet.getString("order_number").trim());
				System.out.println("Invoice Date:" + resultSet.getString("invoice_date").trim());
				System.out.println("Customer Po:" + resultSet.getString("customer_po").trim());
				System.out.println("Address:" + resultSet.getString("sold_to").trim());
				System.out.println("Total Invoice:" + resultSet.getString("total_invoice").trim());
				System.out.println("Status:" + resultSet.getString("status").trim()+"]");
				System.out.println(" ");
			}
		} catch (Exception e) {
			System.out.println("Error in displaying");
			e.printStackTrace();
			System.exit(0);
		}

	}

}
