/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dean Maloney 14140306
 * I used the following code as a template: 
 * 
 * http://cs.lmu.edu/~ray/notes/javanetexamples/
 * 

 */

package jmtttserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

class TicTacToeServer extends Thread {

        /**
         * Constructs a handler thread for a given socket and mark
         * initializes the stream fields, displays the first two
         * welcoming messages.
         */
        public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(4444);
        System.out.println("Tic Tac Toe Server is Running");
        try {
            while (true) {
                TicTacToeMatch match = new TicTacToeMatch();
                TicTacToeMatch.TicTacToePlayer playerX = match.new TicTacToePlayer(listener.accept(), 'X');
                TicTacToeMatch.TicTacToePlayer playerO = match.new TicTacToePlayer(listener.accept(), 'O');
                playerX.setOpponent(playerO);
                playerO.setOpponent(playerX); 
                match.currentPlayer = playerX;
                playerX.start();
                playerO.start();
                System.out.println("Match");
            }
        } finally {
            listener.close();
        }
    }
        
        
}
class TicTacToeMatch {
    


   

    /**
     * The current player.
     */
    TicTacToePlayer currentPlayer;

    
    
    public void tellOpponent(String msg, TicTacToePlayer p)
    {
        p.opponent.report(msg);
    }
    
    public String getOppUsername(TicTacToePlayer p)
    {
        return p.opponent.getUsername();
    }
    
    public void makeMove(int location, TicTacToePlayer player) {
        if(currentPlayer == player)
            currentPlayer = currentPlayer.opponent;
            currentPlayer.otherPlayerMoved(location);
        }
    class TicTacToePlayer extends Thread {
    
     char mark;
        TicTacToePlayer opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        String username, oppUsername;
    
    public TicTacToePlayer(Socket socket, char mark) throws Exception {
        
    
            try {
                input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                
                output.println("WELCOME " + mark);
                
                output.println("MESSAGE Waiting for opponent...");
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }

        /**
         * Accepts notification of who the opponent is.
         */
        public void setOpponent(TicTacToePlayer opponent) {
            this.opponent = opponent;
        }
        
        public String getUsername()
        {
            System.out.println(username);
            return username;
        }

        /**
         * Handles the otherPlayerMoved message.
         */
        public void otherPlayerMoved(int location) {
            output.println("OPPONENT_MOVED " + location);
        }
        
        public void report(String msg)
        {
            output.println(msg);
        }

        /**
         * The run method of this thread.
         */
        public void run() {
            // The thread is only started after everyone connects.
            output.println("MESSAGE All players connected!");
            boolean newUser = true;
            // Tell the first player that it is her turn.
            if (mark == 'X') {
                output.println("MESSAGE Your move!");
            }
            // Repeatedly get commands from the client and process them.
            while (true)  try {
                // The thread is only started after everyone connects.
                output.println("MESSAGE All players connected!");

                // Tell the first player that it is her turn.
                if (mark == 'X') {
                    output.println("MESSAGE Your move!");
                }

                // Repeatedly get commands from the client and process them.
                while (true) {
                    String command = input.readLine();
                    
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        makeMove(location, this);
                    }
                    //Tell your opponent that you forfeited
                    else if(command.startsWith("FORFEIT"))
                    {
                        tellOpponent("FORFEIT", this);
                    }
                    //Get the opponent's username
                    else if(command.startsWith("GETOPP"))
                    {
                        oppUsername = getOppUsername(this);
                        output.println("OPPUSER " + oppUsername);
                    }
                    //Get records based on your username if it exists in the database
                    else if(command.startsWith("USERNAME"))
                    {
                        username = command.substring(9);
                        String uFromFile = username.toLowerCase();
                        boolean found = false;
                        Scanner sc = new Scanner(new FileReader("Usernames.txt"));
			while (sc.hasNext() && found == false) {
				String u = sc.nextLine();
				String uA[] = u.split(",");
				String uTemp = uA[0].toLowerCase();
				if (uFromFile.equals(uTemp)) {
                                        String wins = uA[1];
                                        String draws = uA[2];
                                        String losses = uA[3];
					output.println("RECORDS " + uA[1] + "," + uA[2] + "," + uA[3]);
                                        newUser = false;
					found = true;
				}
			}
			if (found == false)
				output.println("NEWUSER");
			sc.close();
                    }
                    
                    //Overwrites/Adds new records for a user upon quitting
                    else if (command.startsWith("QUIT") || command.startsWith("LEFT")) {
                        String uA[] = command.substring(5).split(",");
                        
                                String newUsername = uA[0];
				String wins = uA[1];
                                String draws = uA[2];
                                String losses = uA[3];  
                                if(newUser == true)
                                {
                                 BufferedWriter x = new BufferedWriter(new FileWriter("Usernames.txt", true));

						x.write(newUsername + "," + wins + "," + draws + "," + losses);
						x.newLine();
						x.close();
                                }
                                else
                                {
                                    
                            	ArrayList<String> users = new ArrayList<String>();
                                Scanner sc = new Scanner(new FileReader ("Usernames.txt"));
                                boolean found = false;
                                while (sc.hasNext()) {
                                        String temp = sc.nextLine();
                                        users.add(temp);
                                }
                                
                                for(int c = 0; c < users.size()&& found == false; c++)
                                {
                                    String[] temp = (users.get(c)).split(",");
                                    
                                    
                                    if(uA[0].toLowerCase().equals(temp[0].toLowerCase()))
                                    {
                                        temp[1] = uA[1];
                                        temp[2] = uA[2];
                                        temp[3] = uA[3];
                                        users.set(c, temp[0]  +"," + temp[1] + "," + temp[2] + "," + temp[3]);
                                        found = true;
                                    }
                                    
                                }
                                BufferedWriter x = new BufferedWriter(new FileWriter("Usernames.txt"));
                                int counter = 0;
                                while (counter < users.size()) {
                                        x.write(users.get(counter));
                                        x.newLine();
                                        counter++;
                                }
                                x.close();
                                }
                            if(command.startsWith("QUIT"))
                            tellOpponent("QUIT", this);
                    }
                }
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            } finally {
                try {
                    socket.close();
                } 
                catch (IOException e) {
                }
            }
    }
}
}




/**
 *
 * @author Dean
 */
