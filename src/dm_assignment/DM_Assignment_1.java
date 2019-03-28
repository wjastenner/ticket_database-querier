package dm_assignment;

import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class DM_Assignment_1 {

    private static Connection con = null;
    private static Statement stmt = null;
    private static ResultSet rs = null;
    private static Scanner scanner;
    private static PrintWriter out;

    public static void main(String[] args) throws IOException, SQLException {

        try {
            String inputFile = args[0];
            String outputFile = args[1];

            scanner = new Scanner(new FileReader(inputFile));
            out = new PrintWriter(new FileWriter(outputFile), true);
            boolean done = false;

            String database = "projects";
            String schema = "pierian_games";
            String username = "postgres";
            String password = "password";

            connect(database, schema);

            do {
                char task = scanner.nextLine().charAt(0);
                switch (task) {
                    case 'A':
                        insertSpectator();
                        break;
                    case 'B':
                        insertEvent();
                        break;
                    case 'C':
                        deleteSpectator();
                        break;
                    case 'D':
                        deleteEvent();
                        break;
                    case 'E':
                        issueTicket();
                        break;
                    case 'P':
                        travelQuery();
                        break;
                    case 'Q':
                        totalTicketsPerEvent();
                        break;
                    case 'R':
                        totalTicketsSpecificEvent();
                        break;
                    case 'S':
                        spectatorItinerary();
                        break;
                    case 'T':
                        ticketDetails();
                        break;
                    case 'V':
                        cancelledEventTickets();
                        break;
                    case 'X':
                        System.out.println("X. End of requests");
                        out.println("X. End of requests");
                        done = true;
                        break;
                    case 'Z':
                        emptyDatabase();
                        break;
                }
            } while (!done);
        } catch (Exception e) {
            System.out.println("Issue caught in main class");
            System.out.println(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static void connect(String database, String schema) {
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/" + database);
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String setSearchPath = "SET SEARCH_PATH TO " + database + ", " + schema + ";";
            stmt.execute(setSearchPath);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Issue connecting to the database 1");
            System.out.println(e.getMessage());
        }
    }

    static void connect(String database, String schema, String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/" + database, username, password);
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String setSearchPath = "SET SEARCH_PATH TO " + database + ", " + schema + ";";
            stmt.execute(setSearchPath);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Issue connecting to the database");
            System.out.println(e.getMessage());
        }
    }

    static void insertSpectator() {
        try {
            String sNo = scanner.nextLine();
            String sName = "'" + scanner.nextLine() + "'";
            String sEmail = "'" + scanner.nextLine() + "'";
            String taskA = "INSERT INTO spectator VALUES (" + sNo + ", " + sName + ", " + sEmail + ");";
            stmt.executeUpdate(taskA);
            System.out.println("A. Spectator insert successful");
            out.println("A. Spectator insert successful");
        } catch (SQLException e) {
            System.out.println("A. Spectator insert unsuccessful");
            System.out.println(e.getMessage());
            out.println("A. Spectator insert unsuccessful");
        }
    } // insert complete

    static void insertEvent() {
        try {
            String eCode = "'" + scanner.nextLine() + "'";
            String eDesc = "'" + scanner.nextLine() + "'";
            String eLocation = "'" + scanner.nextLine() + "'";
            String eDate = "'" + scanner.nextLine() + "'";
            String eTime = "'" + scanner.nextLine() + "'";
            String eMax = scanner.nextLine();
            String taskB = "INSERT INTO event VALUES (" + eCode + ", " + eDesc + ", " + eLocation + ", " + eDate + ", " + eTime + ", " + eMax + ");";
            stmt.executeUpdate(taskB);
            System.out.println("B. Event insert successful");
            out.println("B. Event insert successful");
        } catch (Exception e) {
            System.out.println("B. Event insert unsuccessful");
            System.out.println(e.getMessage());
            out.println("B. Event insert unsuccessful");
        }
    } // insert complete

    static void deleteSpectator() {
        try {
            String sNo = scanner.nextLine();
            String sNoQuery = "SELECT delete_spectator(" + sNo + ");";
            rs = stmt.executeQuery(sNoQuery);
            System.out.println("C. Spectator successfully deleted");
            out.println("C. Spectator successfully deleted");
        } catch (Exception e) {
            System.out.println("C. Spectator deletion unsuccessful");
            System.out.println(e.getMessage());
            out.println("C. Spectator deletion unsuccessful");
        }
    } // delete specific complete IMPROVED

    static void deleteEvent() {
        try {
            String eCode = "'" + scanner.nextLine() + "'";
            String deleteQuery = "SELECT delete_event(" + eCode + ");";
            rs = stmt.executeQuery(deleteQuery);
            System.out.println("D. Event successfully deleted");
            out.println("D. Event successfully deleted");
        } catch (Exception e) {
            System.out.println("D. Event deletion unsuccessful");
            System.out.println(e.getMessage());
            out.println("D. Event deletion unsuccessful");
        }
    } // delete specific complete IMPROVED

    static void issueTicket() {
        try {
            String eCode = "'" + scanner.nextLine() + "'";
            String sNo = scanner.nextLine();
            String taskE = "SELECT insert_ticket(" + eCode + ", " + sNo + ");";
            stmt.executeQuery(taskE);
            System.out.println("E. Ticket issue successful");
            out.println("E. Ticket issue successful");
        } catch (Exception e) {
            System.out.println("E. Ticket issue unsuccessful");
            System.out.println(e.getMessage());
            out.println("E. Ticket issue unsuccessful");
        }
    } // insert complete IMPROVED

    static void travelQuery() {
        try {
            String taskP = "SELECT * FROM travel_query();";
            rs = stmt.executeQuery(taskP);

            if (rs.next()) {
                System.out.println("P. Travel query successful");
                out.println("P. Travel query successful");
                System.out.println("");
                out.println("");

                System.out.println(String.format("%-15s %-15s %-15s%n", "elocation", "edate", "tickets_issued"));
                out.println(String.format("%-15s %-15s %-15s%n", "elocation", "edate", "tickets_issued"));
                rs.previous();
                while (rs.next()) {
                    String eLocation = rs.getString("event_location");
                    String eDate = rs.getString("event_date");
                    String ticketsIssued = rs.getString("total_tickets");
                    System.out.println(String.format("%-15s %-15s %-15s%n", eLocation, eDate, ticketsIssued));
                    out.println(String.format("%-15s %-15s %-15s%n", eLocation, eDate, ticketsIssued));
                }
                out.println("");
            } else {
                System.out.println("P. Travel query successful. No events found");
                out.println("P. Travel query successful. No events found");
            }
        } catch (Exception e) {
            System.out.println("P. Travel query unsuccessful");
            System.out.println(e.getMessage());
        }
    } // query complete IMPROVED

    static void totalTicketsPerEvent() {
        try {
            String taskQ = "SELECT * FROM tickets_per_event()";
            rs = stmt.executeQuery(taskQ);

            if (rs.next()) {
                System.out.println("Q. Total tickets per event query successful");
                out.println("Q. Total tickets per event query successful");
                System.out.println("");
                out.println("");

                System.out.println(String.format("%-15s %-15s %-15s%n", "ecode", "edesc", "tickets_issued"));
                out.println(String.format("%-15s %-15s %-15s%n", "ecode", "edesc", "tickets_issued"));
                rs.previous();

                while (rs.next()) {
                    String eCode = rs.getString("ecode");
                    String eDesc = rs.getString("edesc");
                    String ticketsIssued = rs.getString("tickets_issued");
                    System.out.println(String.format("%-15s %-15s %-15s%n", eCode, eDesc, ticketsIssued));
                    out.println(String.format("%-15s %-15s %-15s%n", eCode, eDesc, ticketsIssued));
                }

            } else {
                System.out.println("Q. Total tickets per event query successful. No events found");
                out.println("Q. Total tickets per event query successful. No events found");
            }
        } catch (Exception e) {
            System.out.println("Q. Total tickets per event query unsuccessful");
            out.println("Q. Total tickets per event query unsuccessful");
            System.out.println(e.getMessage());
        }
    } // query complete IMPROVED

    static void totalTicketsSpecificEvent() {
        try {
            String eCodeSpecific = "'" + scanner.nextLine() + "'";
            String taskR = "SELECT * FROM tickets_specific_event(" + eCodeSpecific + ");";
            rs = stmt.executeQuery(taskR);

            if (rs.next()) {
                System.out.println("R. Total tickets specific event query successful");
                out.println("R. Total tickets specific event query successful");

                System.out.println("");
                out.println("");

                System.out.println(String.format("%-15s %-15s %-15s%n", "ecode", "edesc", "tickets_issued"));
                out.println(String.format("%-15s %-15s %-15s%n", "ecode", "edesc", "tickets_issued"));

                String eCode = rs.getString("eventCode");
                String eDesc = rs.getString("eventDesc");
                String ticketsIssued = rs.getString("ticketsIssued");

                System.out.println(String.format("%-15s %-15s %-15s%n", eCode, eDesc, ticketsIssued));
                out.println(String.format("%-15s %-15s %-15s%n", eCode, eDesc, ticketsIssued));
            }

        } catch (Exception e) {
            System.out.println("R. Total tickets specific event query unsuccessful.");
            out.println("R. Total tickets specific event query unsuccessful.");
            System.out.println(e.getMessage());
            System.out.println("");
            out.println("");
        }
    } // query specific complete IMPROVED

    static void spectatorItinerary() {
        try {
            String sNo = "'" + scanner.nextLine() + "'";
            String taskR = "SELECT * FROM spectator_itinerary(" + sNo + ");";
            rs = stmt.executeQuery(taskR);

            if (rs.next()) {
                System.out.println("S. Spectator itinerary query successful");
                out.println("S. Spectator itinerary query successful");

                System.out.println("");
                out.println("");

                System.out.println(String.format("%-15s %-15s %-15s %-15s %-15s%n", "sname", "edate", "elocation", "etime", "edesc"));
                out.println(String.format("%-15s %-15s %-15sn %-15s %-15s%n", "sname", "edate", "elocation", "etime", "edesc"));

                rs.previous();

                while (rs.next()) {
                    String sName = rs.getString("spectatorName");
                    String eDate = rs.getString("eventDate");
                    String eLocation = rs.getString("eventLocation");
                    String eTime = rs.getString("eventTime");
                    String eDesc = rs.getString("eventDesc");
                    System.out.println(String.format("%-15s %-15s %-15s %-15s %-15s%n", sName, eDate, eLocation, eTime, eDesc));
                    out.println(String.format("%-15s %-15s %-15sn %-15s %-15s%n", sName, eDate, eLocation, eTime, eDesc));
                }

            } else {
                System.out.println("S. Spectator itinerary query unsuccessful. The spectator has not bought any tickets.");
                out.println("S. Spectator itinerary query unsuccessful. The spectator has not bought any tickets.");
            }

        } catch (Exception e) {
            System.out.println("S. Total tickets specific event query unsuccessful.");
            out.println("S. Total tickets specific event query unsuccessful.");
            System.out.println(e.getMessage());
            System.out.println("");
        }
    } // query specific complete IMPROVED

    static void ticketDetails() {
        try {
            String tNo = scanner.nextLine();

            String taskT = "SELECT * FROM ticket_details(" + tNo + ");";

            rs = stmt.executeQuery(taskT);

            System.out.println("T. Ticket details query successful");
            out.println("T. Ticket details query successful");

            System.out.println("");
            out.println("");

            if (rs.next()) {

                System.out.println(String.format("%-15s %-15s %-15s%n", "sname", "ecode", "ticket_status"));
                out.println(String.format("%-15s %-15s %-15s%n", "sname", "ecode", "ticket_status"));

                String sName = rs.getString("spectatorName");
                String eCode = rs.getString("eventCode");
                String ticketStatus = rs.getString("ticketStatus");

                System.out.println(String.format("%-15s %-15s %-15s%n", sName, eCode, ticketStatus));
                out.println(String.format("%-15s %-15s %-15s%n", sName, eCode, ticketStatus));
            }

        } catch (Exception e) {
            System.out.println("T. Ticket details query successful");
            out.println("T. Ticket details query successful");
            System.out.println(e.getMessage());
            System.out.println("");
        }
    } // query specific complete IMPROVED

    static void cancelledEventTickets() {
        try {
            String eCodeSpecific = "'" + scanner.nextLine() + "'";
            String taskV = "SELECT * FROM cancelled_event_ticket_details(" + eCodeSpecific + ")";
            rs = stmt.executeQuery(taskV);
            System.out.println("V. Cancelled event tickets query successful");
            out.println("V. Cancelled event tickets query successful");
            System.out.println("");
            out.println("");
            System.out.println(String.format("%-5s %-7s %-5s %-30s %-20s%n", "tno", "ecode", "sno", "cdate", "cuser"));
            out.println(String.format("%-5s %-7s %-5s %-30s %-20s%n", "tno", "ecode", "sno", "cdate", "cuser"));

            while (rs.next()) {
                String tNo = rs.getString("ticketNumber");
                String eCode = rs.getString("eventCode");
                String sNo = rs.getString("spectatorNumber");
                String cDate = rs.getString("cancelledDate");
                String cUser = rs.getString("currentUser");
                System.out.println(String.format("%-5s %-7s %-5s %-30s %-20s%n", tNo, eCode, sNo, cDate, cUser));
                out.println(String.format("%-5s %-7s %-5s %-30s %-20s%n", tNo, eCode, sNo, cDate, cUser));
            }

        } catch (Exception e) {
            System.out.println("V. Cancelled event tickets query unsuccessful");
            out.println("V. Cancelled event tickets query unsuccessful");
            System.out.println(e.getMessage());
            System.out.println("");
        }
    } // query specific IMPROVED

    static void emptyDatabase() {
        try {
            String taskZ = "TRUNCATE event, spectator, ticket, cancel;";
            stmt.execute(taskZ);
            System.out.println("Z. Database empty successful");
            out.println("Z. Database empty successful");
        } catch (Exception e) {
            System.out.println("Z. Database empty unsuccessful");
            System.out.println(e.getMessage());
            out.println("Z. Database empty unsuccessful");
        }
    }
}
