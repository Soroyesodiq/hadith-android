package org.sonna.tools;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.*;


public class FindMissingPages {

    public static void main(String args[]) throws Exception {
        FindMissingPages finder = new FindMissingPages();
        finder.findMissingPages();
    }

//////////////////////////////////////////////////////////

    private Connection connection = null;
    private PreparedStatement statement1, statement2;
//    private int count = 0;

    public void findMissingPages() throws Exception {

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:books.sqlite");
            connection.setAutoCommit(false);
            System.out.print("Connection success.");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(-1);
        }

        Statement stmt = null;
        try {
            //Handle Books Table of titles only
            stmt = connection.createStatement();
//            //FTS4 with no diacritics
//            String createFts4 = "CREATE VIRTUAL TABLE IF NOT EXISTS pages USING fts4(" +
//                    "content='', book_code, parent_id, page_fts);";
//            stmt.executeUpdate(createFts4);
//
//            String createTable = "CREATE TABLE IF NOT EXISTS details (" +
//                    "page_id, book_code, parent_id, title, page);";
//            stmt.executeUpdate(createTable);

            //Ensure the book has no records
//            String sqlCheckExist = "SELECT * FROM details WHERE book_code = '" + bookCode + "'";
//            ResultSet rs = stmt.executeQuery( sqlCheckExist);
//            if (rs.next()) {
//                rs.close();
//                System.out.print("Existing records exit, overwrite is not allowed.");
//                System.exit(-1);
//            }
//
//            count = 0;

            //just display records
//            String sqlDisplay = "SELECT * FROM details where page_id = '0' ";
//            ResultSet displayRs = stmt.executeQuery( sqlCheckExist);
//            if (displayRs.next()) {
//                for(int i = 1 ; i <= 5; i++) {
//                    System.err.println(displayRs.getString(i));
//                }
//            }
//            displayRs.close();
//            System.out.println("\r\n" + count + " records of " + bookCode + ": is indexed");

        } finally
        {
            stmt.close();
            connection.commit();
            connection.close();
        }
    }




}
