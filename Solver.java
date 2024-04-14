import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
 
public class Solver {
 
    //tells which squares on the cube swap with each other after a turn is made
    private final static int[][] FRONTSWAP = {{0, 1, 13, 12}, {11, 6, 15, 20}, {10, 17, 14, 3}};
    private final static int[][] RIGHTSWAP = {{1, 9, 19, 15}, {13, 10, 4, 16}, {6, 7, 18, 17}};

    final static HashMap<Integer, String> MOVES = getMoves();
    final static long[] sixPow = getSixPows();

    //inner class that represents the current configuration as well as the previous move made
    static class Move{
 
        int moveID;
        Move previous;
        long config;
     
        Move(long config, int moveID, Move previous){
            this.config = config;
            this.previous = previous;
            this.moveID = moveID;
        }
    }

    //solves the cube using a BFS search in order to ensure that the solution found is the one that requires the least amount of moves
    static Move solveCube(long start, long solution){
        Queue<Move> queue = new LinkedList<>();
        Set<Long> reached = new HashSet<>(3674160);
        queue.offer(new Move(start, -3, null));
        while(!queue.isEmpty()){
            Move move = queue.poll();
            if(move.config == solution) return move;
            for(int i = 0; i < 3; i++){
                if(move.moveID / 3 == i) continue;
                long con, conCCW, conCW, con2;
                con = conCW = conCCW = con2 = move.config;
                if(i == 0){
                    long six4 = sixPow[4], six6 = sixPow[6], six8 = sixPow[8], six9 = sixPow[9], six10 = sixPow[10], six11 = sixPow[11], six12 = sixPow[12];
                    long delta = con - con % six12;
                    conCW = delta + con % six6 * 36 + con % six8 / six6 + con % six11 / six8 * six9 + con % six12 / six11 * six8;
                    conCCW = delta + con % six8 / 36 + con % sixPow[2] * six6 + con % six12 / six8 / 6 * six8 + con % six9 / six8 * six11;
                    con2 = delta + con % six4 * 1296 + con % six8 / six4 + con % six10 / six8 * six10 + con % six12 / six10 * six8;
                }
                else{
                    for(int[] swaps : (i == 1 ? RIGHTSWAP : FRONTSWAP)){
                        for(int j = 0; j < 4; j++){
                            long currColor = con % sixPow[swaps[j]+1] / sixPow[swaps[j]], order = sixPow[swaps[j]];
                            conCW = conCW + currColor * (sixPow[swaps[(j+1) % 4]] - order);
                            conCCW = conCCW + currColor * (sixPow[swaps[j == 0 ? 3 : j-1]] - order);
                            con2 = con2 + currColor * (sixPow[swaps[(j+2) % 4]] - order);
                        }
                    }
                }
                if(conCW == solution) return new Move(conCW, i*3, move);
                else if(!reached.contains(conCW)){
                    queue.offer(new Move(conCW, i*3, move));
                    reached.add(conCW);
                }
                if(conCCW == solution) return new Move(conCCW, i*3+1, move);
                else if(!reached.contains(conCCW)){
                    queue.offer(new Move(conCCW, i*3+1, move));
                    reached.add(conCCW);
                }
                if(con2 == solution) return new Move(con2, i*3+2, move);
                else if(!reached.contains(con2)){
                    queue.offer(new Move(con2, i*3+2, move));
                    reached.add(con2);
                }
            }
        }
        return null;
    }
 
    //fills the cache with the powers of 6 from 0-21 stored as an associative array so that they do not have to constantly be recalculated 
    static long[] getSixPows(){
        long[] sixpows = new long[22];
        for(int i = 0; i < 22; i++) sixpows[i] = (long) Math.pow(6, i);
        return sixpows;
    }

    //fills the moves map with integer keys cooresponding to a particular move 
    static HashMap<Integer, String> getMoves(){
        HashMap<Integer, String> moves = new HashMap<>();
        moves.put(0, "Rotate the top face 90 degrees clockwise");
        moves.put(1, "Rotate the top face 90 degrees counter-clockwise");
        moves.put(2, "Rotate the top face 180 degrees");
        moves.put(3, "Rotate the right face 90 degrees clockwise");
        moves.put(4, "Rotate the right face 90 degrees counter-clockwise");
        moves.put(5, "Rotate the right face 180 degrees");
        moves.put(6, "Rotate the front face 90 degrees clockwise");
        moves.put(7, "Rotate the front face 90 degrees counter-clockwise");
        moves.put(8, "Rotate the front face 180 degrees");
        return moves;
    }
}