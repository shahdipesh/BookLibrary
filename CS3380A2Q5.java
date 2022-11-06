import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Scanner;

public class CS3380A2Q5 {
	static Connection connection;

	public static void main(String[] args) throws Exception {

		// startup sequence
		MyDatabase db = new MyDatabase();
		runConsole(db);

		System.out.println("Exiting...");
	}

	public static void runConsole(MyDatabase db) {

		Scanner console = new Scanner(System.in);
		System.out.print("Welcome! Type h for help. ");
		System.out.print("db > ");
		String line = console.nextLine();
		String[] parts;
		String arg = "";

		while (line != null && !line.equals("q")) {
			parts = line.split("\\s+");
			if (line.indexOf(" ") > 0)
				arg = line.substring(line.indexOf(" ")).trim();

			if (parts[0].equals("h"))
				printHelp();
			else if (parts[0].equals("mp")) {
				db.getMostPublishers();
			}

			else if (parts[0].equals("s")) {
				if (parts.length >= 2)
					db.nameSearch(arg);
				else
					System.out.println("Require an argument for this command");
			}

			else if (parts[0].equals("l")) {
				try {
					if (parts.length >= 2)
						db.lookupByID(arg);
					else
						System.out.println("Require an argument for this command");
				} catch (Exception e) {
					System.out.println("id must be an integer");
				}
			}

			else if (parts[0].equals("sell")) {
				try {
					if (parts.length >= 2)
						db.lookupWhoSells(arg);
					else
						System.out.println("Require an argument for this command");
				} catch (Exception e) {
					System.out.println("id must be an integer");
				}
			}

			else if (parts[0].equals("notsell")) {
				try {
					if (parts.length >= 2)
						db.whoDoesNotSell(arg);
					else
						System.out.println("Require an argument for this command");
				} catch (Exception e) {
					System.out.println("id must be an integer");
				}
			}

			else if (parts[0].equals("authors")) {

				db.top5Author();

			} else if (parts[0].equals("mc")) {
				db.mostCities();
			}

			else if (parts[0].equals("notread")) {
				db.ownBooks();
			}

			else if (parts[0].equals("all")) {
				db.readAll();
			}

			else if (parts[0].equals("mr")) {
				db.mostReadPerCountry();
			}

			else
				System.out.println("Read the help with h, or find help somewhere else.");

			System.out.print("db > ");
			line = console.nextLine();
		}

		console.close();
	}

	private static void printHelp() {
		System.out.println("Library database");
		System.out.println("Commands:");
		System.out.println("h - Get help");
		System.out.println("s <name> - Search for a name");
		System.out.println("l <id> - Search for a user by id");
		System.out.println("sell <author id> - Search for a stores that sell books by this id");
		System.out.println("notread - Books not read by its own author");
		System.out.println("all - Authors that have read all their own books");
		System.out.println("notsell <author id>  - list of stores that do not sell this author");
		System.out.println("mp - Authors with the most publishers");
		System.out.println("mc - Authors with books in the most cities");
		System.out.println("mr - Most read book by country");
		System.out.println("");

		System.out.println("q - Exit the program");

		System.out.println("---- end help ----- ");
	}

}

class MyDatabase {
	private Connection connection;

	public MyDatabase() {
		try {
			String url = "jdbc:sqlite:library.db";
			// create a connection to the database
			connection = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println("asdasda");
			e.printStackTrace(System.out);
		}

	}

	public void nameSearch(String name) {
		//match for a name from database using prepared statement
		try {
			String sql = "SELECT * FROM people WHERE first LIKE ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1,  name + "%");
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				System.out.print("first name: "+result.getString("first")+ ", ");
				System.out.print("last name: "+result.getString("last") + "\n");

			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	public void lookupByID(String id) {

		try {
			Statement statement = connection.createStatement();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM people WHERE id = ?");
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				// read the result set

				System.out.print("first name = " + rs.getString("first")+ ", ");

				String aid = rs.getString("aid");
				if (aid!=null) {
					System.out.print("last name = " + rs.getString("last")+ ", ");
					System.out.print("aid = " + rs.getString("cid")+"\n");
				}
				else{
					System.out.print("last name = " + rs.getString("last")+ "\n");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	public void getMostPublishers() {

		try {
			Statement statement = connection.createStatement();

			PreparedStatement ps = connection.prepareStatement("" +
					"Select people.first,people.last,count(distinct publishers.pid) as numPublishers " +
					"from people join books on people.aid = books.aid " +
					"join publishers on books.pid=publishers.pid " +
					"group by people.id " +
					"order by numPublishers " +
					"desc limit 5");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				System.out.print("first name = " + rs.getString("first")+ ", ");
				System.out.print("last name = " + rs.getString("last")+ ", ");
				System.out.print("numPublishers = " + rs.getInt("numPublishers")+ "\n");

			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}


	}

	public void lookupWhoSells(String id) {
		//join store,sells,publishers,books,authors
		try {
			Statement statement = connection.createStatement();

			PreparedStatement ps = connection.prepareStatement("Select store.name,count(distinct books.bid) as numBooksSold from " +
					"store join sells on store.id=sells.sid" +
					" join publishers on publishers.pid=sells.pid " +
					"join books on publishers.pid = books.pid " +
					"join people on people.aid=books.aid " +
					"where people.aid = ? group by store.id");
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				System.out.print("store name = " + rs.getString("name")+ ", ");
				System.out.print("noOfBooksOnSale = " + rs.getInt("numBooksSold")+ "\n");
			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
		
	}

	public void whoDoesNotSell(String id) {
		//join store,sells,publishers,books,authors
		try {
			Statement statement = connection.createStatement();

			PreparedStatement ps = connection.prepareStatement("" +
					"Select distinct name from store " +
					"except " +
					"Select distinct store.name from store " +
					"join sells on store.id = sells.sid " +
					"join publishers on publishers.pid = sells.pid " +
					"natural join books where books.aid=? ");
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				System.out.print("store name = " + rs.getString("name")+ "\n");

			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}

	}

	public void top5Author() {

		
	}

	public void mostReadPerCountry() {
		try {
			Statement statement = connection.createStatement();

			PreparedStatement ps = connection.prepareStatement("" +
					"Select * from ( " +
					"Select country,books.title from people natural join city " +
					"join read on people.id=read.id " +
					"join books on read.bid=books.bid " +
					"group by country,books.bid order by count(*) desc )" +
					"group by country");

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				System.out.print("country = " + rs.getString("country")+ ", ");
				System.out.print("title = " + rs.getString("title")+ "\n");


			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
		
	}

	public void ownBooks() {
		//Books not read by its own author
		try {
			Statement statement = connection.createStatement();

			PreparedStatement ps = connection.prepareStatement("" +
					"Select people.id,people.first,people.last,books.bid,books.title from " +
					"people join books on people.aid=books.aid" +
					" except" +
					" Select people.id,people.first,people.last,books.bid,books.title from " +
					"people join read on people.id=books.bid join books on read.bid=books.bid");
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				// read the result set
				System.out.print("first name = " + rs.getString("first")+ ", ");
				System.out.print("last name = " + rs.getString("last")+ ", ");
				System.out.print("book title = " + rs.getString("title")+ "\n");

			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	public void readAll() {
		//Authors that have read all their own books
		try {
			Statement statement = connection.createStatement();
			PreparedStatement ps = connection.prepareStatement("" +
					"with notRead as (Select people.id,books.bid from people,books where people.aid is not null" +
					"except " +
					"select people.id,books.bid from people join read on people.id=read.bid join books on read.bid=books.bid)"+
					"Select distinct first,last from people where id not in(select id from notRead)"+
					"");

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				// read the result set
				System.out.print("firstName = " + rs.getString("first")+ ", ");
				System.out.print("lastName = " + rs.getString("last")+ "\n");

			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}

		
	}

	public void mostCities() {
		//Authors that have read all their own books
		try {
			Statement statement = connection.createStatement();
			PreparedStatement ps = connection.prepareStatement("" +
					"Select people.first,people.last,count(distinct store.cid) as numCities " +
					"from people join books on people.aid=books.aid " +
					"join publishers on publishers.pid=books.pid " +
					"join sells on sells.pid = publishers.pid " +
					"join store on store.id=sells.sid " +
					"group by people.id " +
					"order by numCities desc limit 5");

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				// read the result set
				System.out.print("firstName = " + rs.getString("first") + ", ");
				System.out.print("lastName = " + rs.getString("last") + ", ");
				System.out.print("numCities = " + rs.getInt("numCities") + "\n");

			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
		
	}

}
